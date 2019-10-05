/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.root

import com.feedbacktree.example.flows.login.LoginFlow
import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.RenderingContext
import com.feedbacktree.flow.core.StateCompletable

object RootFlow : Flow<Unit, State, Event, Nothing, Any>(
    reduce = ::reduce,
    feedbacks = listOf()
) {
    override fun initialState(input: Unit): State {
        return State.LoggedOut
    }

    override fun render(state: State, context: RenderingContext): Any {
        return when (state) {
            State.LoggedOut -> context.renderChild(LoginFlow, onResult = {
                enterState(State.LoggedIn)
            })
            State.LoggedIn -> TODO()
        }
    }
}

sealed class State : StateCompletable<Nothing> {
    override val flowOutput: Nothing? = null

    object LoggedOut : State()
    object LoggedIn : State()
}


sealed class Event {
    object LogOut : Event()
}

fun reduce(state: State, event: Event): State {
    return when (event) {
        Event.LogOut -> State.LoggedOut
    }
}
