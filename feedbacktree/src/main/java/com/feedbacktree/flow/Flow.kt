package com.feedbacktree.flow

import asOptional
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.notests.rxfeedback.*
import org.notests.rxfeedback.Optional
import java.util.*

private val screenChangedPublishSubject = PublishSubject.create<Unit>()
val screenChanged: Observable<Unit> = screenChangedPublishSubject


typealias Feedback<State, Event> = (ObservableSchedulerContext<State>) -> Observable<Event>

sealed class FlowResult<out Result> {
    object Aborted : FlowResult<Nothing>()
    data class Completed<out Result>(val result: Result) : FlowResult<Result>()
}

interface StateCompletable<Result> {
    val flowResult: FlowResult<Result>?
}

fun <Result> completed(result: Result): FlowResult<Result> = FlowResult.Completed(result)
fun <Result> aborted(): FlowResult<Result> = FlowResult.Aborted

abstract class Flow<State, Event, Result, Rendering>(
    val initialState: State,
    private val reduce: (State, Event) -> State,
    private val scheduler: Scheduler,
    val feedbacks: List<Feedback<State, Event>>
) where State : StateCompletable<Result> {

    internal val childrenResultPublishSubject = PublishSubject.create<Event>()

    var key = UUID.randomUUID().toString()

    private val publishSubjectEvents = PublishSubject.create<Event>()

    private val abortedResultPublishSubject = PublishSubject.create<FlowResult<Result>>()

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
                childrenResultPublishSubject,
                publishSubjectEvents
            )
        )
    }

    fun run(): Observable<FlowResult<Result>> {
        if (active.value == true) {
            error("Attempting to start a flow that is already running")
        }
        val system = Observables.system(
            initialState = initialState,
            reduce = reduce,
            scheduler = scheduler,
            scheduledFeedback = feedbacks + listOf(backdoorFeedback())
        )

        val completedResult = system
            .map { childState -> childState.flowResult.asOptional }
            .filter { it is Optional.Some }
            .map { it as Optional.Some; it.data }
            .doOnSubscribe { active.onNext(true) }
            .doFinally { active.onNext(false) }
        return Observable.merge(completedResult, abortedResultPublishSubject)
            .take(1)
    }

    abstract fun render(state: State, context: RenderingContext): Rendering

    fun attachFeedbacks(feedbacks: List<Feedback<State, Event>>): Disposable {
        val events = feedbacks.map {
            val feedbackState = active.switchMap { isActive ->
                if (isActive) {
                    state
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
        abortedResultPublishSubject.onNext(aborted())
    }
}