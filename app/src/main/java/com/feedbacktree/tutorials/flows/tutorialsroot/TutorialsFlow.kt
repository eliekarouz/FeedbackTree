/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.tutorialsroot

import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.advance
import com.feedbacktree.tutorials.flows.counter.CounterFlow
import com.feedbacktree.tutorials.flows.login.LoginFlow

data class State(
    val tutorials: List<Tutorial> = listOf(
        Tutorial.Counter, Tutorial.Login
    ),
    val selectedTutorial: Tutorial? = null
)

sealed class Event {
    data class SelectedTutorial(val tutorial: Tutorial) : Event()
    object CompletedTutorial : Event()
}

val TutorialsFlow = Flow<Unit, State, Event, Nothing, Any>(
    initialState = { State() },
    stepper = { state, event ->
        when (event) {
            is Event.SelectedTutorial -> state.copy(selectedTutorial = event.tutorial).advance()
            Event.CompletedTutorial -> state.copy(selectedTutorial = null).advance()
        }
    },
    feedbacks = listOf(),
    render = { state, context ->
        when (state.selectedTutorial) {
            null -> TutorialsScreen(state, context.sink)
            Tutorial.Counter -> context.renderChild(CounterFlow, onResult = {
                context.sendEvent(Event.CompletedTutorial)
            })
            Tutorial.Login -> context.renderChild(input = "", flow = LoginFlow, onResult = {
                context.sendEvent(Event.CompletedTutorial)
            })
        }
    }
)


data class TutorialsScreen(
    val state: State,
    val sink: (Event) -> Unit
) {

    data class Row(
        val title: String,
        val onClickEvent: Event
    )

    val rows: List<Row> = state.tutorials.mapIndexed { index, demo ->
        Row(
            title = "${index + 1}. ${demo.title}",
            onClickEvent = Event.SelectedTutorial(demo)
        )
    }
}