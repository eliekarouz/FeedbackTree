/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.root

import com.feedbacktree.example.flows.login.LoginFlow
import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.Step
import com.feedbacktree.flow.core.advance

val RootFlow = Flow<Unit, State, Event, Nothing, Any>(
    initialState = { State.LoggedOut },
    stepper = ::reduce,
    feedbacks = listOf()
) { state, context ->
    when (state) {
        State.LoggedOut -> context.renderChild(
            input = "email@example.com",
            flow = LoginFlow,
            onResult = {
            context.sink(Event.LogInCompleted)
        })
        State.LoggedIn -> TODO()
    }
}

sealed class State {
    object LoggedOut : State()
    object LoggedIn : State()
}


sealed class Event {
    object LogInCompleted : Event()
    object LogOut : Event()
}

fun reduce(state: State, event: Event): Step<State, Nothing> {
    return when (event) {
        Event.LogInCompleted -> State.LoggedIn.advance()
        Event.LogOut -> State.LoggedOut.advance()
    }
}
