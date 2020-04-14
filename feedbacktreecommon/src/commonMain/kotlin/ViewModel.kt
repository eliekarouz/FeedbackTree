package com.feedbacktree.flow.core

interface ViewModel<EventT : Any> {
    val sink: Sink<EventT>
}

data class Sink<EventT : Any>(
    val flowHasCompleted: Boolean,
    val eventSink: (EventT) -> Unit
)