/*
 * Created by eliek on 1/3/2021
 * Copyright (c) 2021 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.phonenumber

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.advance
import com.feedbacktree.flow.core.endFlow
import com.feedbacktree.tutorials.flows.phonenumber.State.Progress.*

val PhoneNumberRegistrationFlow = Flow<Unit, State, Event, Unit, Any>(
    initialState = {
        State(
            phoneNumber = "",
            countryCode = "",
            registrationCode = "",
            progress = EnteringNumber
        )
    },
    stepper = { state, event ->
        when (event) {
            is Event.EnteredCountryCode -> state.copy(
                countryCode = event.countryCode
            ).advance()
            is Event.EnteredLineNumber -> state.copy(
                phoneNumber = event.number
            ).advance()
            Event.ClickedSendCode -> state.copy(
                progress = SendingCode
            ).advance()
            is Event.ReceivedRegistrationResponse -> state.copy(
                progress = EnteringRegistrationCode
            ).advance()
            is Event.EnteredRegistrationCode -> state.copy(
                registrationCode = event.code
            ).advance()
            Event.ClickedValidateCode -> state.copy(
                progress = Registering
            ).advance()
            is Event.ReceivedValidationResponse -> endFlow()
        }
    },
    feedbacks = listOf(),
    render = { state, context ->
        when (state.progress) {
            EnteringNumber -> EnterPhoneScreen(state, context.sink)
            SendingCode -> LoadingScreen(message = "Sending Code", context.sink)
            EnteringRegistrationCode -> EnterRegistrationCodeScreen(state, context.sink)
            Registering -> LoadingScreen(message = "Validating Code", context.sink)
        }
    }
)


data class State(
    val phoneNumber: String,
    val countryCode: String,
    val registrationCode: String,
    val progress: Progress
) {
    enum class Progress {
        EnteringNumber, SendingCode, EnteringRegistrationCode, Registering
    }
}

sealed class Event {
    data class EnteredCountryCode(val countryCode: String) : Event()
    data class EnteredLineNumber(val number: String) : Event()
    object ClickedSendCode : Event()
    data class ReceivedRegistrationResponse(val error: String?) : Event()

    data class EnteredRegistrationCode(val code: String) : Event()
    object ClickedValidateCode : Event()
    data class ReceivedValidationResponse(val error: String?) : Event()
}

data class EnterPhoneScreen(
    private val state: State,
    val sink: (Event) -> Unit
) {
    // screen properties go here
}

data class EnterRegistrationCodeScreen(
    private val state: State,
    val sink: (Event) -> Unit
) {
    // screen properties go here
}

data class LoadingScreen(
    val message: String,
    val sink: (Event) -> Unit
)