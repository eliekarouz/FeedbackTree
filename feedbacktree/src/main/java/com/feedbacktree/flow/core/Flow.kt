/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import com.feedbacktree.flow.utils.logInfo
import com.feedbacktree.flow.utils.mapNotNull
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

// TODO Remove these global variables
private val viewModelChangedPublishSubject = PublishSubject.create<Unit>()
internal val newViewModelTrigger: Observable<Unit> = viewModelChangedPublishSubject

abstract class Flow<InputT : Any, StateT : Any, EventT : Any, OutputT : Any, ViewModelT>(
    private val stepper: (StateT, EventT) -> Step<StateT, OutputT>,
    private val scheduler: Scheduler = AndroidSchedulers.mainThread(),
    val feedbacks: List<Feedback<StateT, EventT>>
) : IFlow<InputT, OutputT> {

    private val enterStatePublishSubject = PublishSubject.create<FlowEvent<StateT, EventT>>()

    private val publishSubjectEvents = PublishSubject.create<EventT>()

    private val outputPublishSubject = PublishSubject.create<OutputT>()

    private val className = javaClass.simpleName

    /**
     * You need first to [run] the state machine first.
     * In other terms, if the flow isn't running, [attachFeedbacks] will not run it.
     */
    private var flowState = BehaviorSubject.create<FlowState<StateT, OutputT>>()

    val currentState: StateT?
        get() = flowState.value?.state

    private var active = BehaviorSubject.create<Boolean>()

    private fun backdoorFeedback(): Feedback<FlowState<StateT, OutputT>, FlowEvent<StateT, EventT>> =
        bindWithScheduler { osc ->
            return@bindWithScheduler Bindings(
                subscriptions = listOf(
                    osc.source.subscribe { flowState.onNext(it) },
                    // Why skip(1)
                    // We skip the first element because all flows are started inside the render method.
                    // We already know the initial state and we have rendered it.
                    // There is no need to render again.
                    // skip(1) guarantees that we don't have re-entrancy: we start children flows inside the [render] method,
                    // we should not request another render pass while we rendering.
                    osc.source.skip(1).subscribe { state ->
                        // We will only trigger a rendering pass when the flow doesn't emit an output and ends.
                        // The output will be propagated synchronously to the parent flows. Once a parent/grandparent captures the output,
                        // its state will be updated and it's at that time that we will trigger the rendering pass.
                        if (state.flowOutput == null) {
                            viewModelChangedPublishSubject.onNext(Unit)
                        }
                    },
                    // Observables.system in RxFeedback adds an observeOn(scheduler) before returning the stream of states.
                    // This means the if we collect the flow output directly from the system, the output will be delayed.
                    // We are using this feedback loop to do that because we don't have an observerOn(scheduler) and the state is emitted instantly
                    // as soon as it goes out of the stepper.
                    osc.source.mapNotNull { it.flowOutput }.subscribe { flowOutput ->
                        outputPublishSubject.onNext(flowOutput)
                    }
                ),
                events = listOf(
                    publishSubjectEvents.map<FlowEvent<StateT, EventT>> { event ->
                        FlowEvent.StandardEvent(
                            event
                        )
                    }
                )
            )
        }

    private fun childrenResultEventFeedback(): Feedback<FlowState<StateT, OutputT>, FlowEvent<StateT, EventT>> =
        {
        // This is a custom a feedback that actually forwards the events immediately.
        // This will only be used to forward outputs from the children flows into this flow.
        // No need to add observeOn(scheduler) because all the flows will be running on the main-thread.
        // This guarantees to have no re-entrancy because the child flow will complete due to some event and
        // we will be processing the output and updating the parents(grandparents) states synchronously.
        enterStatePublishSubject
    }

    abstract fun initialState(input: InputT): StateT

    override fun run(input: InputT): Observable<OutputT> {
        if (active.value == true) {
            error("Attempting to start a flow that is already running")
        }
        val stepperWithLog: (StateT, EventT) -> Step<StateT, OutputT> = { state, event ->
            logInfo("$className: $event")
            stepper(state, event)
        }
        val wrappedFeedbacks = feedbacks.map { feedback ->
            wrapFeedback<StateT, EventT, OutputT>(feedback)
        } + backdoorFeedback()

        val system = Observables.system(
            initialState = FlowState<StateT, OutputT>(
                state = initialState(input),
                flowOutput = null
            ),
            reduce = wrapStepper(stepperWithLog),
            scheduler = scheduler,
            scheduledFeedback = wrappedFeedbacks + childrenResultEventFeedback()
        )

        val stateEncodedOutput = system.flatMap {
            Observable.empty<OutputT>()
        }

        // Although we are collecting the output directly from the outputPublishSubject,
        // we are using Observable.merge just to bound the lifetime of the system to the lifecycle of the returned
        // observable.
        return Observable.merge(stateEncodedOutput, outputPublishSubject)
            .doOnSubscribe { active.onNext(true) }
            .doFinally { active.onNext(false) }
            .take(1)
    }

    abstract fun render(state: StateT, context: RenderingContext): ViewModelT

    protected fun sink(): Sink<EventT> {
        val lastState = flowState.value
        return Sink(
            flowHasCompleted = lastState?.flowOutput != null,
            eventSink = ::send
        )
    }

    protected fun <E : Any> sink(transform: (E) -> EventT?): Sink<E> {
        val lastState = flowState.value
        return Sink(
            flowHasCompleted = lastState?.flowOutput != null,
            eventSink = { e ->
                transform(e)?.let { send(it) }
            }
        )
    }

    protected fun send(event: EventT) {
        publishSubjectEvents.onNext(event)
    }

    protected fun enterState(state: StateT) {
        enterStatePublishSubject.onNext(FlowEvent.EnterStateEvent(state))
    }

    protected fun complete(result: OutputT) {
        outputPublishSubject.onNext(result)
    }
}

private data class FlowState<StateT, OutputT>(
    internal val state: StateT,
    internal val flowOutput: OutputT?
)


internal sealed class FlowEvent<StateT, EventT> {
    internal data class StandardEvent<StateT, EventT>(val event: EventT) :
        FlowEvent<StateT, EventT>()

    internal data class EnterStateEvent<StateT, EventT>(val state: StateT) :
        FlowEvent<StateT, EventT>()
}


private fun <StateT : Any, EventT : Any, OutputT : Any> wrapStepper(reduce: (StateT, EventT) -> Step<StateT, OutputT>)
        : (FlowState<StateT, OutputT>, FlowEvent<StateT, EventT>) -> FlowState<StateT, OutputT> {
    return { flowState, flowEvent ->
        when (flowEvent) {
            is FlowEvent.StandardEvent -> {
                when (val stateOutputT = reduce(flowState.state, flowEvent.event)) {
                    is Step.State -> flowState.copy(state = stateOutputT.state)
                    is Step.Output -> flowState.copy(flowOutput = stateOutputT.output)
                }
            }
            is FlowEvent.EnterStateEvent -> flowState.copy(state = flowEvent.state)
        }
    }
}


private fun <StateT : Any, EventT : Any, OutputT : Any> wrapFeedback(feedback: Feedback<StateT, EventT>): Feedback<FlowState<StateT, OutputT>, FlowEvent<StateT, EventT>> {
    return { flowStateObservableSchedulerContext ->
        val stateObservableSchedulerContext = ObservableSchedulerContext(
            source = flowStateObservableSchedulerContext.source.map { it.state },
            scheduler = flowStateObservableSchedulerContext.scheduler
        )
        val observableEvents = feedback(stateObservableSchedulerContext)
        observableEvents.map { event ->
            FlowEvent.StandardEvent<StateT, EventT>(event)
        }
    }
}