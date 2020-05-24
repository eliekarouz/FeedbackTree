package com.feedbacktree.example.flows.testexamples.fullscreen

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.RenderingContext
import com.feedbacktree.flow.core.StateCompletable
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.core.modals.ViewModal

object ModalsFlow : Flow<Unit, State, Event, Unit, Modal>(
    reduce = ::reduce,
    feedbacks = listOf()
) {
    override fun initialState(input: Unit) =
        State()

    override fun render(state: State, context: RenderingContext): Modal {
        return ViewModal(content = HelloWorldViewModel(sink()))
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