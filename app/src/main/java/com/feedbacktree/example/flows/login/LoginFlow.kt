/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.login

import com.feedbacktree.example.flows.fingerprint.FingerprintFlow
import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.Step
import com.feedbacktree.flow.core.advance
import com.feedbacktree.flow.core.endFlowWith
import com.feedbacktree.flow.ui.core.modals.ModalContainerScreen

val LoginFlow = Flow<Unit, State, Event, Unit, ModalContainerScreen<*, *>>(
    initialState = { State() },
    stepper = ::stepper,
    feedbacks = listOf()
) { state, context ->
    // This more clear to start with the architecture: LoginViewModel(state, onEvent = { event -> send(event) })
    // A shorted version would be LoginViewModel(state, onEvent = ::send)
    val loginScreen = LoginViewModel(state, context.sink)
    val fingerprintScreen = context.renderChild(FingerprintFlow, onResult = {})
    ModalContainerScreen(
        loginScreen,
        listOf()
    )
}

data class State(
    val email: String = "",
    val password: String = "",
    val isLoggingIn: Boolean = false
)

sealed class Event {
    data class EnteredEmail(val email: String) : Event()
    data class EnteredPassword(val password: String) : Event()
    object ClickedLogin : Event()
    data class ReceivedLogInResponse(val success: Boolean) : Event()
}

fun stepper(state: State, event: Event): Step<State, Unit> {
    return when (event) {
        is Event.EnteredEmail -> state.copy(email = event.email).advance()
        is Event.EnteredPassword -> state.copy(password = event.password).advance()
        Event.ClickedLogin -> endFlowWith(Unit)
        is Event.ReceivedLogInResponse -> endFlowWith(
            Unit
        )
    }
}