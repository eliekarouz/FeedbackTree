package com.feedbacktree.flow.core

import com.feedbacktree.flow.utils.collect
import com.feedbacktree.flow.utils.logInfo
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
    val flowOutput: FlowOutput<Ouput>?
}

abstract class Flow<Input, State, Event, Output, Screen>(
    private val reduce: (State, Event) -> State,
    private val scheduler: Scheduler = AndroidSchedulers.mainThread(),
    val feedbacks: List<Feedback<State, Event>>
) : IFlow<Input, Output> where State : StateCompletable<Output> {

    internal val childrenOutputPublishSubject = PublishSubject.create<Event>()

    private val publishSubjectEvents = PublishSubject.create<Event>()

    private val outputPublishSubject = PublishSubject.create<FlowOutput<Output>>()

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
                osc.source.skip(1).subscribe { screenChangedPublishSubject.onNext(Unit) }
            ),
            events = listOf(
                childrenOutputPublishSubject,
                publishSubjectEvents
            )
        )
    }

    abstract fun initialState(input: Input): State

    override fun run(input: Input): Observable<FlowOutput<Output>> {
        if (active.value == true) {
            error("Attempting to start a flow that is already running")
        }
        val reducerWithLog: (State, Event) -> State = { state, event ->
            logInfo("$className: $event")
            reduce(state, event)
        }
        val system = Observables.system(
            initialState = initialState(input),
            reduce = reducerWithLog,
            scheduler = scheduler,
            scheduledFeedback = feedbacks + listOf(backdoorFeedback())
        )

        val stateEncodedOuput = system
            .collect { state -> state.flowOutput }

        return Observable.merge(stateEncodedOuput, outputPublishSubject)
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

    fun send(event: Event) {
        publishSubjectEvents.onNext(event)
    }

    fun abort() {
        outputPublishSubject.onNext(aborted())
    }

    fun complete(result: Output) {
        outputPublishSubject.onNext(completed(result))
    }
}