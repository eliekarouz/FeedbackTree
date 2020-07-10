/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.login

import com.feedbacktree.flow.core.*
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

val LoginFlow = Flow<String, State, Event, Unit, LoginViewModel>(
    initialState = { email -> State(email = email) },
    stepper = ::stepper,
    feedbacks = listOf(loginFeedback())
) { state, context ->
    // This more clear to start with the architecture: LoginViewModel(state, onEvent = { event -> send(event) })
    // A shorted version would be LoginViewModel(state, onEvent = ::send)
    return@Flow LoginViewModel(state, context.sink)
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

typealias LoginQuery = Pair<String, String>

fun loginFeedback() = react<State, LoginQuery, Event>(
    query = { state ->
        state
            .takeIf {
                it.isLoggingIn
            }?.let {
                it.email to it.password
            }
    },
    effects = { (email, password) ->
        login(
            email = email,
            password = password
        ) // Observable<Boolean>, true when authentication succeeds
            .map { loginSucceeded ->
                Event.ReceivedLogInResponse(loginSucceeded)
            }
    }
)

private fun login(email: String, password: String): Observable<Boolean> {
    // simulate network call here...
    return Observable.just(true)
        .delaySubscription(2, TimeUnit.SECONDS) // To simulate a network call
}