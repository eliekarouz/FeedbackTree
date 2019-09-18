package com.feedbacktree.example.flows.root

import com.feedbacktree.example.flows.login.LoginFlow
import com.feedbacktree.example.flows.root.RootFlow.reduce
import com.feedbacktree.flow.Flow
import com.feedbacktree.flow.FlowResult
import com.feedbacktree.flow.RenderingContext
import com.feedbacktree.flow.StateCompletable

object RootFlow : Flow<Unit, RootFlow.State, RootFlow.Event, Nothing, Any>(
    reduce = ::reduce,
    feedbacks = listOf()
) {
    override fun initialState(input: Unit): State {
        return State.LoggingIn
    }

    override fun render(state: State, context: RenderingContext): Any {
        return when (state) {
            RootFlow.State.LoggingIn -> context.renderChild(LoginFlow, onResult = {
                send(Event.SuccessfullyLoggedIn)
            })
            RootFlow.State.LoggedIn -> TODO()
        }
    }

    sealed class State : StateCompletable<Nothing> {
        override val flowResult: FlowResult<Nothing>? = null

        object LoggingIn : State()
        object LoggedIn : State()
    }


    sealed class Event {
        object SuccessfullyLoggedIn : Event()
        object LogOut : Event()
    }

    fun reduce(state: State, event: Event): State {
        return when (event) {
            RootFlow.Event.SuccessfullyLoggedIn -> State.LoggedIn
            RootFlow.Event.LogOut -> State.LoggingIn
        }
    }
}
