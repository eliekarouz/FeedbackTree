package com.feedbacktree.example.flows.login

import com.feedbacktree.example.flows.login.LoginFlow.reduce
import com.feedbacktree.flow.*

object LoginFlow : Flow<Unit, LoginFlow.State, LoginFlow.Event, Unit, LoginScreen>(
    reduce = ::reduce,
    feedbacks = listOf()
) {
    override fun initialState(input: Unit): State = State()

    // This more clear to start with the architecture: LoginScreen(state, onEvent = { event -> send(event) })
    // A shorted version would be LoginScreen(state, onEvent = ::send)
    override fun render(state: State, context: RenderingContext): LoginScreen {
        return LoginScreen(state, onEvent = { event -> send(event) })
    }

    data class State(
        val email: String = "",
        val password: String = "",
        val isLoggingIn: Boolean = false,
        override val flowResult: FlowResult<Unit>? = null
    ) : StateCompletable<Unit>

    sealed class Event {
        data class EnteredEmail(val email: String) : Event()
        data class EnteredPassword(val password: String) : Event()
        object ClickedLogin : Event()
        data class ReceivedLogInResponse(val success: Boolean) : Event()
    }

    fun reduce(state: State, event: Event): State {
        return when (event) {
            is Event.EnteredEmail -> state.copy(email = event.email)
            is Event.EnteredPassword -> state.copy(password = event.password)
            LoginFlow.Event.ClickedLogin -> state.copy(isLoggingIn = true)
            is Event.ReceivedLogInResponse -> state.copy(flowResult = completed(Unit))
        }
    }
}