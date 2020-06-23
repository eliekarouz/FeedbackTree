package com.feedbacktree.example.flows.testexamples.fullscreen

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.Step
import com.feedbacktree.flow.core.endFlow
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.core.modals.ViewModal

val ModalsFlow = Flow<Unit, State, Event, Unit, Modal>(
    initialState = { State() },
    stepper = ::stepper,
    feedbacks = listOf()
) { state, context ->
    ViewModal(
        content = HelloWorldViewModel(context.sink)
    )
}

class State

sealed class Event {
    object BackPressed : Event()
}

@Suppress("UNUSED_PARAMETER")
fun stepper(state: State, event: Event): Step<State, Unit> {
    return when (event) {
        Event.BackPressed -> endFlow()
    }
}