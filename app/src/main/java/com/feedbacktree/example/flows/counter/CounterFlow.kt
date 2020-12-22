/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.counter

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.advance
import com.feedbacktree.flow.core.endFlow

val CounterFlow = Flow<Unit, State, Event, Unit, CounterScreen>(
    initialState = { State(counter = 0) },
    stepper = { state, event ->
        when (event) {
            Event.Increment -> state.copy(
                counter = state.counter + 1
            ).advance()
            Event.Decrement -> state.copy(
                counter = state.counter - 1
            ).advance()
            Event.BackPressed -> endFlow()
        }
    },
    feedbacks = listOf(),
    render = { state, context ->
        CounterScreen(state, context.sink)
    }
)

data class State(
    val counter: Int
)

sealed class Event {
    object Increment : Event()
    object Decrement : Event()
    object BackPressed : Event()
}

data class CounterScreen(
    private val state: State,
    val sink: (Event) -> Unit
) {
    val counterText: String = state.counter.toString()
}