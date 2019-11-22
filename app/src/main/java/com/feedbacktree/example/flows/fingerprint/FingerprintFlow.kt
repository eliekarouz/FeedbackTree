/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.fingerprint

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.RenderingContext
import com.feedbacktree.flow.core.Sink
import com.feedbacktree.flow.core.StateCompletable
import com.feedbacktree.flow.ui.core.modals.FullscreenModal
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.core.modals.ModalContainerScreen
import com.feedbacktree.flow.ui.core.modals.asViewModal
import com.feedbacktree.flow.ui.views.ViewModel

class EnterFingerprintViewModel(override val sink: Sink<Event>) : ViewModel<Event>

object FingerprintFlow : Flow<Unit, State, Event, Unit, Modal>(
    reduce = ::reduce,
    feedbacks = listOf()
) {
    override fun initialState(input: Unit): State {
        return State.AskingForFingerprint()
    }

    override fun render(state: State, context: RenderingContext): Modal {
//        val fingerPrintAlert = AlertModal(title = "",
//            message = "",
//            buttons = mapOf(AlertModal.Button.POSITIVE to "Cancel"),
//            onEvent = { event ->
//
//            }).withView(EnterFingerprintViewModel())
        return FullscreenModal(
            ModalContainerScreen(
                EnterFingerprintViewModel(sink()),
                EnterFingerprintViewModel(sink()).asViewModal()
            )
        )
    }
}

sealed class State : StateCompletable<Unit> {
    override val flowOutput: Unit? = null

    data class AskingForFingerprint(val x: Int = 0) : State()
}

sealed class Event {

}

fun reduce(state: State, event: Event): State {
    return state
}