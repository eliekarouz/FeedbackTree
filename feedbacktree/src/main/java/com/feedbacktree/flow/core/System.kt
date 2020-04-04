/*
 * Created by eliek on 4/4/2020
 * Copyright (c) 2020 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.ReplaySubject

/**
 * Tuple of observable sequence and corresponding scheduler context on which that observable sequence receives elements.
 */
data class ObservableSchedulerContext<Element>(
    val source: Observable<Element>,
    val scheduler: Scheduler
)

class Observables {
    companion object
}

typealias Feedback<State, Event> = (ObservableSchedulerContext<State>) -> Observable<Event>

/**
 * System simulation will be started upon subscription and stopped after subscription is disposed.
 *
 * System state is represented as a [State] parameter.
 * Events are represented by [Event] parameter.
 *
 * @param initialState Initial state of the system.
 * @param reduce Calculates new system state from existing state and a transition event (system integrator, reducer).
 * @param scheduledFeedback Feedback loops that produce events depending on current system state.
 * @return Current state of the system.
 */
fun <State, Event> Observables.Companion.system(
    initialState: State,
    reduce: (State, Event) -> State,
    scheduler: Scheduler,
    scheduledFeedback: Iterable<Feedback<State, Event>>
): Observable<State> =
    Observable.defer {
        val replaySubject = ReplaySubject.createWithSize<State>(1)

        val events: Observable<Event> = Observable.merge(scheduledFeedback.map { feedback ->
            val state = ObservableSchedulerContext(replaySubject, scheduler)
            feedback(state)
        })

        events.scan(initialState, reduce)
            .doOnNext { output ->
                replaySubject.onNext(output)
            }
            .observeOn(scheduler)
    }

/**
 * System simulation will be started upon subscription and stopped after subscription is disposed.
 *
 * System state is represented as a [State] parameter.
 * Events are represented by [Event] parameter.
 *
 * @param initialState Initial state of the system.
 * @param reduce Calculates new system state from existing state and a transition event (system integrator, reducer).
 * @param scheduledFeedback Feedback loops that produce events depending on current system state.
 * @return Current state of the system.
 */
fun <State, Event> Observables.Companion.system(
    initialState: State,
    reduce: (State, Event) -> State,
    scheduler: Scheduler,
    vararg scheduledFeedback: Feedback<State, Event>
): Observable<State> =
    Observables.system(initialState, reduce, scheduler, scheduledFeedback.toList())