/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.login

import com.feedbacktree.example.flows.fingerprint.FingerprintFlow
import com.feedbacktree.flow.core.*
import com.feedbacktree.flow.ui.core.modals.ModalContainerScreen

object LoginFlow : Flow<Unit, State, Event, Unit, ModalContainerScreen<*, *>>(
    stepper = ::reduce,
    feedbacks = listOf()
) {
    override fun initialState(input: Unit): State = State()

    // This more clear to start with the architecture: LoginViewModel(state, onEvent = { event -> send(event) })
    // A shorted version would be LoginViewModel(state, onEvent = ::send)
    override fun render(state: State, context: RenderingContext): ModalContainerScreen<*, *> {
        val loginScreen = LoginViewModel(state, sink())
        val fingerprintScreen = context.renderChild(FingerprintFlow, onResult = {})
        return ModalContainerScreen(loginScreen, listOf())
    }
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

fun reduce(state: State, event: Event): Step<State, Unit> {
    return when (event) {
        is Event.EnteredEmail -> state.copy(email = event.email).advance()
        is Event.EnteredPassword -> state.copy(password = event.password).advance()
        Event.ClickedLogin -> endFlowWith(Unit)
        is Event.ReceivedLogInResponse -> endFlowWith(Unit)
    }
}