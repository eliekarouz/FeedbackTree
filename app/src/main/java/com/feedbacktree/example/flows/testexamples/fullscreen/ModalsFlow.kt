package com.feedbacktree.example.flows.testexamples.fullscreen

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.StepperFactory
import com.feedbacktree.flow.core.advance
import com.feedbacktree.flow.core.endFlow
import com.feedbacktree.flow.ui.core.BackStackScreen
import com.feedbacktree.flow.ui.core.modals.FullScreenModal
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.core.modals.ViewModal

// testing ViewModal compatibility
val ModalsFlow = Flow<Unit, State, Event, Unit, Modal>(
    initialState = { State.ShowingHelloWorld },
    stepper = stepper(),
    feedbacks = listOf()
) { state, context ->
    return@Flow when (state) {
        State.ShowingHelloWorld -> ViewModal(
            content = HelloWorldScreen(context.sink)
        )
        State.ShowingBackstack -> FullScreenModal(
            content = BackStackScreen(HelloWorldScreen(context.sink))
        )

    }

}

sealed class State {
    object ShowingHelloWorld : State()
    object ShowingBackstack : State()
}

sealed class Event {
    object BackPressed : Event()
}

@Suppress("UNUSED_PARAMETER")
fun stepper() = StepperFactory.create<State, Event, Unit>() {
    state<State.ShowingHelloWorld> {
        on<Event.BackPressed> {
            State.ShowingBackstack.advance()
        }
    }
    state<State.ShowingBackstack> {
        on<Event.BackPressed> {
            endFlow()
        }
    }
}