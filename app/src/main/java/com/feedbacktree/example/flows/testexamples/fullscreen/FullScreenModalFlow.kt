package com.feedbacktree.example.flows.testexamples.fullscreen

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.RenderingContext
import com.feedbacktree.flow.core.StateCompletable
import com.feedbacktree.flow.ui.core.modals.FullScreenModal
import com.feedbacktree.flow.ui.core.modals.Modal

object FullScreenModalFlow : Flow<Unit, State, Event, Unit, Modal>(
    reduce = ::reduce,
    feedbacks = listOf()
) {
    override fun initialState(input: Unit) =
        State()

    override fun render(state: State, context: RenderingContext): Modal {
        return FullScreenModal(content = HelloWorldViewModel(sink()))
    }
}

data class State(override val flowOutput: Unit? = null) : StateCompletable<Unit>

sealed class Event {
    object BackPressed : Event()
}

fun reduce(state: State, event: Event): State {
    return when (event) {
        Event.BackPressed -> state.copy(flowOutput = Unit)
    }
}