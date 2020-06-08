/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.fingerprint

import com.feedbacktree.flow.core.*
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.core.modals.asModal
import com.feedbacktree.flow.ui.views.ViewModel

class EnterFingerprintViewModel(override val sink: Sink<Event>) :
    ViewModel<Event>

object FingerprintFlow : Flow<Unit, State, Event, Unit, Modal>(
    stepper = ::reduce,
    feedbacks = listOf()
) {
    override fun initialState(input: Unit): State {
        return State.AskingForFingerprint()
    }

    override fun render(state: State, context: RenderingContext): Modal {
        return EnterFingerprintViewModel(sink()).asModal()
    }
}

sealed class State {
    data class AskingForFingerprint(val x: Int = 0) : State()
}

sealed class Event {

}

fun reduce(state: State, event: Event): Step<State, Unit> {
    return state.advance()
}