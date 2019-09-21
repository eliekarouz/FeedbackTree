package com.feedbacktree.example.flows.root

import com.feedbacktree.example.flows.login.LoginFlow
import com.feedbacktree.example.flows.root.RootFlow.reduce
import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.FlowOutput
import com.feedbacktree.flow.core.RenderingContext
import com.feedbacktree.flow.core.StateCompletable

object RootFlow : Flow<Unit, RootFlow.State, RootFlow.Event, Nothing, Any>(
    reduce = ::reduce,
    feedbacks = listOf()
) {
    override fun initialState(input: Unit): State {
        return State.LoggedOut
    }

    override fun render(state: State, context: RenderingContext): Any {
        return when (state) {
            RootFlow.State.LoggedOut -> context.renderChild(LoginFlow, onResult = {
                send(Event.SuccessfullyLoggedIn)
            })
            RootFlow.State.LoggedIn -> TODO()
        }
    }

    sealed class State : StateCompletable<Nothing> {
        override val flowOuput: FlowOutput<Nothing>? = null

        object LoggedOut : State()
        object LoggedIn : State()
    }


    sealed class Event {
        object SuccessfullyLoggedIn : Event()
        object LogOut : Event()
    }

    fun reduce(state: State, event: Event): State {
        return when (event) {
            RootFlow.Event.SuccessfullyLoggedIn -> State.LoggedIn
            RootFlow.Event.LogOut -> State.LoggedOut
        }
    }
}
