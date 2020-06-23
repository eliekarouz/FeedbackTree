/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.fingerprint

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.Step
import com.feedbacktree.flow.core.advance
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.core.modals.asModal

class EnterFingerprintViewModel(val sink: (Event) -> Unit)

val FingerprintFlow = Flow<Unit, State, Event, Unit, Modal>(
    initialState = { State.AskingForFingerprint() },
    stepper = ::stepper,
    feedbacks = listOf()
) { state, context ->
    EnterFingerprintViewModel(context.sink).asModal()
}

sealed class State {
    data class AskingForFingerprint(val x: Int = 0) : State()
}

sealed class Event {

}

fun stepper(state: State, event: Event): Step<State, Unit> {
    return state.advance()
}