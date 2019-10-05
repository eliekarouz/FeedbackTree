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
private val screenChangedPublishSubject = PublishSubject.create<Unit>()
internal val screenChanged: Observable<Unit> = screenChangedPublishSubject


typealias Feedback<State, Event> = (ObservableSchedulerContext<State>) -> Observable<Event>

interface StateCompletable<Ouput> {
    val flowOutput: Ouput?
}

abstract class Flow<Input, State, Event, Output, Screen>(
    private val reduce: (State, Event) -> State,
    private val scheduler: Scheduler = AndroidSchedulers.mainThread(),
    val feedbacks: List<Feedback<State, Event>>
) : IFlow<Input, Output> where State : StateCompletable<Output> {

    internal val enterStatePublishSubject = PublishSubject.create<FlowEvent<State, Event>>()

    private val publishSubjectEvents = PublishSubject.create<Event>()

    private val outputPublishSubject = PublishSubject.create<Output>()

    private val className = javaClass.simpleName

    /**
     * This prevents [attachFeedbacks] from starting the workflow by mistake.
     * You need first to [run] the state machine first.
     * In other terms, if the flow isn't running, [attachFeedbacks] will not run it.
     */
    var state = BehaviorSubject.create<State>()
    private var active = BehaviorSubject.create<Boolean>()

    private fun backdoorFeedback(): Feedback<State, Event> = bind { osc ->
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
                    // We will only trigger a rendering screen when the flow doesn't emit an output and ends.
                    // The output will be propagated synchronously to the parent flows. Once a parent/grandparent captures the output,
                    // its state will be updated and it's at that time that we will trigger the rendering pass.
                    if (state.flowOutput == null) {
                        screenChangedPublishSubject.onNext(Unit)
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

    private fun childrenResultEventFeedback(): Feedback<State, FlowEvent<State, Event>> = {
        // This is a custom a feedback that actually forwards the events immediately.
        // This will only be used to forward outputs from the children flows into this flow.
        // No need to add observeOn(scheduler) because all the flows will be running on the main-thread.
        // This guarantees to have no re-entrancy because the child flow will complete due to some event and
        // we will be processing the output and updating the parents(grandparents) states synchronously.
        enterStatePublishSubject
    }

    abstract fun initialState(input: Input): State

    override fun run(input: Input): Observable<Output> {
        if (active.value == true) {
            error("Attempting to start a flow that is already running")
        }
        val reducerWithLog: (State, Event) -> State = { state, event ->
            logInfo("$className: $event")
            reduce(state, event)
        }
        val wrappedFeedbacks = (feedbacks + backdoorFeedback())
            .map { feedback ->
                wrapFeedback<State, Event>(feedback)
            }

        val system = Observables.system(
            initialState = initialState(input),
            reduce = wrapReduce(reducerWithLog),
            scheduler = scheduler,
            scheduledFeedback = wrappedFeedbacks + childrenResultEventFeedback()
        )

        val stateEncodedOutput = system.flatMap {
            Observable.empty<Output>()
        }

        // Although we are collecting the output directly from the outputPublishSubject,
        // we are using Observable.merge just to bound the lifetime of the system to the lifecycle of the returned
        // observable.
        return Observable.merge(stateEncodedOutput, outputPublishSubject)
            .doOnSubscribe { active.onNext(true) }
            .doFinally { active.onNext(false) }
            .take(1)
    }

    abstract fun render(state: State, context: RenderingContext): Screen

    fun attachFeedbacks(feedbacks: List<Feedback<State, Event>>): Disposable {
        val events = feedbacks.map {
            val feedbackState = active.switchMap { isActive ->
                if (isActive) {
                    // Push states as long as the flow is didn't complete.
                    state.filter { it.flowOutput == null }
                } else Observable.empty<State>()
            }
            val observableSchedulerContext = ObservableSchedulerContext(feedbackState, scheduler)
            it(observableSchedulerContext)
        }
        return Observable.merge(events).subscribe { publishSubjectEvents.onNext(it) }
    }

    fun attachFeedbacks(vararg feedbacks: Feedback<State, Event>): Disposable =
        attachFeedbacks(feedbacks.toList())

    protected fun send(event: Event) {
        publishSubjectEvents.onNext(event)
    }

    protected fun enterState(state: State) {
        enterStatePublishSubject.onNext(FlowEvent.EnterStateEvent(state))
    }

    protected fun complete(result: Output) {
        outputPublishSubject.onNext(result)
    }
}

internal sealed class FlowEvent<State, Event> {
    internal data class StandardEvent<State, Event>(val event: Event) : FlowEvent<State, Event>()
    internal data class EnterStateEvent<State, Event>(val state: State) : FlowEvent<State, Event>()
}


private fun <State, Event> wrapReduce(reduce: (State, Event) -> State): (State, FlowEvent<State, Event>) -> State {
    return { state, flowEvent ->
        when (flowEvent) {
            is FlowEvent.StandardEvent -> reduce(state, flowEvent.event)
            is FlowEvent.EnterStateEvent -> flowEvent.state
        }
    }
}


private fun <State, Event> wrapFeedback(feedback: Feedback<State, Event>): Feedback<State, FlowEvent<State, Event>> {
    return { soc ->
        val observableEvents = feedback(soc)
        observableEvents.map { event ->
            FlowEvent.StandardEvent<State, Event>(event)
        }
    }
}