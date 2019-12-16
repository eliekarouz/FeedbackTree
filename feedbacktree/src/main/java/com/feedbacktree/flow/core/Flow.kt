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
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.notests.rxfeedback.*

// TODO Remove these global variables
private val viewModelChangedPublishSubject = PublishSubject.create<Unit>()
internal val newViewModelTrigger: Observable<Unit> = viewModelChangedPublishSubject


typealias Feedback<StateT, EventT> = (ObservableSchedulerContext<StateT>) -> Observable<EventT>

interface StateCompletable<OutputT> {
    val flowOutput: OutputT?
}

abstract class Flow<InputT, StateT, EventT, OutputT, ViewModelT>(
    private val reduce: (StateT, EventT) -> StateT,
    private val scheduler: Scheduler = AndroidSchedulers.mainThread(),
    val feedbacks: List<Feedback<StateT, EventT>>
) : IFlow<InputT, OutputT> where StateT : StateCompletable<OutputT> {

    private val enterStatePublishSubject = PublishSubject.create<FlowEvent<StateT, EventT>>()

    private val publishSubjectEvents = PublishSubject.create<EventT>()

    private val outputPublishSubject = PublishSubject.create<OutputT>()

    private val className = javaClass.simpleName

    /**
     * This prevents [attachFeedbacks] from starting the workflow by mistake.
     * You need first to [run] the state machine first.
     * In other terms, if the flow isn't running, [attachFeedbacks] will not run it.
     */
    var state = BehaviorSubject.create<StateT>()
    private var active = BehaviorSubject.create<Boolean>()

    private fun backdoorFeedback(): Feedback<StateT, EventT> = bind { osc ->
        return@bind Bindings(
            subscriptions = listOf(
                osc.source.subscribe { state.onNext(it) },
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
                // as soon as it goes out of the reducer.
                osc.source.mapNotNull { it.flowOutput }.subscribe { flowOutput ->
                    outputPublishSubject.onNext(flowOutput)
                }
            ),
            events = listOf(
                publishSubjectEvents
            )
        )
    }

    private fun childrenResultEventFeedback(): Feedback<StateT, FlowEvent<StateT, EventT>> = {
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
        val reducerWithLog: (StateT, EventT) -> StateT = { state, event ->
            logInfo("$className: $event")
            reduce(state, event)
        }
        val wrappedFeedbacks = (feedbacks + backdoorFeedback())
            .map { feedback ->
                wrapFeedback<StateT, EventT>(feedback)
            }

        val system = Observables.system(
            initialState = initialState(input),
            reduce = wrapReduce(reducerWithLog),
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

    fun attachFeedbacks(feedbacks: List<Feedback<StateT, EventT>>): Disposable {
        val events = feedbacks.map {
            val feedbackState = active.switchMap { isActive ->
                if (isActive) {
                    // Push states as long as the flow is didn't complete.
                    state.filter { it.flowOutput == null }
                } else Observable.empty<StateT>()
            }
            val observableSchedulerContext = ObservableSchedulerContext(feedbackState, scheduler)
            it(observableSchedulerContext)
        }
        return Observable.merge(events).subscribe { publishSubjectEvents.onNext(it) }
    }

    fun attachFeedbacks(vararg feedbacks: Feedback<StateT, EventT>): Disposable =
        attachFeedbacks(feedbacks.toList())

    protected fun sink(): Sink<EventT> {
        val lastState = state.value
        return Sink(
            flowHasCompleted = lastState?.flowOutput != null,
            eventSink = ::send
        )
    }

    protected fun <E> sink(transform: (E) -> EventT?): Sink<E> {
        val lastState = state.value
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

internal sealed class FlowEvent<StateT, EventT> {
    internal data class StandardEvent<StateT, EventT>(val event: EventT) :
        FlowEvent<StateT, EventT>()

    internal data class EnterStateEvent<StateT, EventT>(val state: StateT) :
        FlowEvent<StateT, EventT>()
}


private fun <StateT, EventT> wrapReduce(reduce: (StateT, EventT) -> StateT): (StateT, FlowEvent<StateT, EventT>) -> StateT {
    return { state, flowEvent ->
        when (flowEvent) {
            is FlowEvent.StandardEvent -> reduce(state, flowEvent.event)
            is FlowEvent.EnterStateEvent -> flowEvent.state
        }
    }
}


private fun <StateT, EventT> wrapFeedback(feedback: Feedback<StateT, EventT>): Feedback<StateT, FlowEvent<StateT, EventT>> {
    return { soc ->
        val observableEvents = feedback(soc)
        observableEvents.map { event ->
            FlowEvent.StandardEvent<StateT, EventT>(event)
        }
    }
}