/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.login

import com.feedbacktree.flow.core.*
import com.feedbacktree.tutorials.managers.AuthenticationManager
import io.reactivex.Observable

val LoginFlow = Flow<String, State, Event, LoginFlowOutput, LoginScreen>(
    id = "LoginFlow",
    initialState = { lastEmailUsed -> State(email = lastEmailUsed) },
    stepper = { state, event ->
        when (event) {
            is Event.EnteredEmail -> state.copy(email = event.email).advance()
            is Event.EnteredPassword -> state.copy(password = event.password).advance()
            Event.ClickedLogin -> state.copy(isLoggingIn = true).advance()
            is Event.ReceivedLogInResponse -> {
                if (event.success) {
                    endFlowWith(LoginFlowOutput.Success(state.email))
                } else {
                    state.copy(isLoggingIn = false).advance()
                }
            }

            Event.BackPressed -> endFlowWith(LoginFlowOutput.Aborted)
        }
    },
    feedbacks = listOf(loginFeedback())
) { state, context ->
    return@Flow LoginScreen(state, context.sink)
}

data class State(
    val email: String = "",
    val password: String = "",
    val isLoggingIn: Boolean = false,
)

sealed class Event {
    data class EnteredEmail(val email: String) : Event()
    data class EnteredPassword(val password: String) : Event()
    object ClickedLogin : Event()
    data class ReceivedLogInResponse(val success: Boolean) : Event()

    object BackPressed : Event()
}

sealed class LoginFlowOutput {
    object Aborted : LoginFlowOutput()
    data class Success(val email: String) : LoginFlowOutput()
}

data class LoginScreen(
    val state: State,
    val sink: (Event) -> Unit
) {
    val emailText: String
        get() = state.email

    val passwordText: String
        get() = state.password

    val loginButtonTitle: String
        get() = if (state.isLoggingIn) "Signing In" else "Sign In"

    val isLoginButtonEnabled: Boolean
        get() = state.email.isNotEmpty() && state.password.isNotEmpty()
}

// Feedbacks

private data class LoginQuery(
    val email: String,
    val password: String
)

@Suppress("RemoveExplicitTypeArguments") // For Readability
private fun loginFeedback(): Feedback<State, Event> = react<State, LoginQuery, Event>(
    query = { state ->
        if (state.isLoggingIn) {
            LoginQuery(email = state.email, password = state.password)
        } else {
            null
        }
    },
    effects = { queryResult ->
        val authenticationSuccess: Observable<Boolean> = AuthenticationManager.login(
            email = queryResult.email,
            password = queryResult.password
        )
        authenticationSuccess.map { loginSucceeded ->
            Event.ReceivedLogInResponse(loginSucceeded)
        }
    }
)
