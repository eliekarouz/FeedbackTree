/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.root

import com.feedbacktree.example.flows.counter.CounterFlow
import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.Step
import com.feedbacktree.flow.core.advance

val RootFlow = Flow<Unit, State, Event, Nothing, Any>(
    initialState = { State() },
    stepper = ::reduce,
    feedbacks = listOf()
) { state, context ->
    when (state.selectedDemo) {
        Demo.CounterExample -> context.renderChild(CounterFlow, onResult = {
            context.sendEvent(Event.DemoCompleted)
        })
        null -> DemoScreen(state, context.sink)
    }
}

data class State(
    val demoOptions: List<Demo> = listOf(
        Demo.CounterExample
    ),
    val selectedDemo: Demo? = null
)

enum class Demo {
    CounterExample;

    val title: String
        get() = when (this) {
            CounterExample -> "Counter"
        }
}

sealed class Event {
    object DemoCompleted : Event()
    data class SelectedDemo(val demo: Demo) : Event()
}

fun reduce(state: State, event: Event): Step<State, Nothing> {
    return when (event) {
        Event.DemoCompleted -> state.copy(selectedDemo = null).advance()
        is Event.SelectedDemo -> state.copy(selectedDemo = event.demo).advance()
    }
}