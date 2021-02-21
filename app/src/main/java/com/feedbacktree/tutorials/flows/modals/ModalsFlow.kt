/*
 * Created by eliek on 2/13/2021
 * Copyright (c) 2021 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.modals

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.advance
import com.feedbacktree.flow.core.endFlow
import com.feedbacktree.flow.ui.core.modals.AlertModal
import com.feedbacktree.flow.ui.core.modals.FullScreenModal
import com.feedbacktree.flow.ui.core.modals.ModalContainerScreen
import com.feedbacktree.tutorials.flows.login.LoginFlow

val ModalsFlow = Flow<Unit, State, Event, Unit, ModalContainerScreen<*, *>>(
    initialState = { State.Idle },
    stepper = { _, event ->
        when (event) {
            Event.ShowAlertClicked -> State.ShowingAlertModal.advance()
            Event.ShowAlertWithCustomViewClicked -> State.ShowingAlertModalWithCustomView.advance()
            Event.ShowFullScreenModalClicked -> State.ShowingFullScreenModal.advance()
            is Event.DismissAlert -> State.Idle.advance()
            Event.FullScreenFlowCompleted -> State.Idle.advance()
            Event.BackClicked -> endFlow()
        }
    },
    feedbacks = listOf(),
    render = { state, context ->
        ModalContainerScreen(
            baseScreen = ModalsScreen(sink = context.sink),
            modal = when (state) {
                State.Idle -> null
                State.ShowingAlertModal -> AlertModal(context.sink) {
                    title = "COVID Screening"
                    message = "Do you have fever?"
                    positive("Yes", Event.DismissAlert("Yes Clicked"))
                    negative("No", Event.DismissAlert("No Clicked"))
                    cancelEvent = Event.DismissAlert("BackClicked")
                }
                State.ShowingAlertModalWithCustomView -> AlertModal(context.sink) {
                    title = "COVID Info"
                    positive("Dismiss", Event.DismissAlert("Dismiss Clicked"))
                    contentScreen = CovidInfoScreen()
                    cancelEvent = Event.DismissAlert("BackClicked")
                }
                State.ShowingFullScreenModal -> {
                    val contentScreen =
                        context.renderChild(input = "", flow = LoginFlow, onResult = {
                            context.sendEvent(Event.FullScreenFlowCompleted)
                        })
                    // Wrap the screen produced by the LoginFlow in a FullScreenModal
                    FullScreenModal(contentScreen)
                }
            }
        )
    }
)


enum class State {
    Idle,
    ShowingAlertModal,
    ShowingAlertModalWithCustomView,
    ShowingFullScreenModal
}

sealed class Event {
    object ShowAlertClicked : Event()
    object ShowAlertWithCustomViewClicked : Event()
    object ShowFullScreenModalClicked : Event()

    data class DismissAlert(val action: String) : Event()
    object FullScreenFlowCompleted : Event()

    object BackClicked : Event()
}

data class ModalsScreen(
    val sink: (Event) -> Unit
)