/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.counter

import com.feedbacktree.flow.core.*

val CounterFlow = Flow<Unit, State, Event, Unit, Any>(
    initialState = { State(counter = 0) },
    stepper = ::stepper,
    feedbacks = listOf()
) { state, context ->
    CounterScreen(state, context.sink)
}

data class State(
    val counter: Int
)


sealed class Event {
    object Increment : Event()
    object Decrement : Event()
    object BackPressed: Event()
}

fun stepper(state: State, event: Event): Step<State, Unit> {
    return when (event) {
        Event.Increment -> state.copy(counter = state.counter + 1).advance()
        Event.Decrement -> state.copy(counter = state.counter - 1).advance()
        Event.BackPressed -> endFlow()
    }
}