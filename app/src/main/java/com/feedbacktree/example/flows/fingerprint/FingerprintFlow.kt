package com.feedbacktree.example.flows.fingerprint

import com.feedbacktree.example.flows.fingerprint.FingerprintFlow.reduce
import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.FlowOutput
import com.feedbacktree.flow.core.RenderingContext
import com.feedbacktree.flow.core.StateCompletable
import com.feedbacktree.flow.ui.core.modals.AlertModal
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.core.modals.withView

class EnterFingerprintScreen

object FingerprintFlow : Flow<Unit, FingerprintFlow.State, FingerprintFlow.Event, Unit, Modal>(
    reduce = ::reduce,
    feedbacks = listOf()
) {
    override fun initialState(input: Unit): State {
        return FingerprintFlow.State.AskingForFingerprint()
    }

    override fun render(state: State, context: RenderingContext): Modal {
        return AlertModal(title = "",
            message = "",
            buttons = mapOf(AlertModal.Button.POSITIVE to "Cancel"),
            onEvent = { event ->

            })
            .withView(EnterFingerprintScreen())
    }

    sealed class State : StateCompletable<Unit> {
        override val flowOutput: FlowOutput<Unit>? = null

        data class AskingForFingerprint(val x: Int = 0) : State()
    }

    sealed class Event {

    }

    fun reduce(state: State, event: Event): State {
        return state
    }
}