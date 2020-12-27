/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.login

import com.feedbacktree.flow.core.*
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

val LoginFlow = Flow<String, State, Event, String, LoginScreen>(
    initialState = { lastEmailUsed -> State(email = lastEmailUsed) },
    stepper = { state, event ->
        when (event) {
            is Event.EnteredEmail -> state.copy(email = event.email).advance()
            is Event.EnteredPassword -> state.copy(password = event.password).advance()
            Event.ClickedLogin -> state.copy(isLoggingIn = true).advance()
            is Event.ReceivedLogInResponse -> {
                if (event.success) {
                    endFlowWith(state.email)
                } else {
                    state.copy(isLoggingIn = false).advance()
                }
            }
        }
    },
    feedbacks = listOf(loginFeedback()),
    render = { state, context ->
        return@Flow LoginScreen(state, context.sink)
    }
)

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

object AuthenticationManager {
    fun login(email: String, password: String): Observable<Boolean> {
        // simulate network call here...
        return Observable.just(true)
            .delaySubscription(2, TimeUnit.SECONDS) // To simulate a network call
    }
}
