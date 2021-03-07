/*
 * Created by eliek on 2/13/2021
 * Copyright (c) 2021 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.modals

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.advance
import com.feedbacktree.flow.core.endFlow
import com.feedbacktree.flow.ui.core.modals.*
import com.feedbacktree.tutorials.flows.login.LoginFlow

val ModalsFlow = Flow<Unit, State, Event, Unit, ModalContainerScreen<*, *>>(
    initialState = { State.Idle },
    stepper = { _, event ->
        when (event) {
            Event.ShowAlertClicked -> State.ShowingAlertModal.advance()
            Event.ShowAlertWithCustomViewClicked -> State.ShowingAlertModalWithCustomView.advance()
            Event.ShowFullScreenModalClicked -> State.ShowingFullScreenModal.advance()
            Event.ShowCustomSizeModalClicked -> State.ShowingCustomSizeModal.advance()
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
                State.ShowingCustomSizeModal -> {
                    val contentScreen =
                        context.renderChild(input = "", flow = LoginFlow, onResult = {
                            context.sendEvent(Event.FullScreenFlowCompleted)
                        })
                    ViewModal(
                        contentScreen,
                        widthLayout = Layout.Percentage(55),
                        heightLayout = Layout.Percentage(55)
                    )
                }
            }
        )
    }
)


enum class State {
    Idle,
    ShowingAlertModal,
    ShowingAlertModalWithCustomView,
    ShowingFullScreenModal,
    ShowingCustomSizeModal
}

sealed class Event {
    object ShowAlertClicked : Event()
    object ShowAlertWithCustomViewClicked : Event()
    object ShowFullScreenModalClicked : Event()
    object ShowCustomSizeModalClicked : Event()

    data class DismissAlert(val action: String) : Event()

    object FullScreenFlowCompleted : Event()
    object BackClicked : Event()
}

data class ModalsScreen(
    val sink: (Event) -> Unit
)