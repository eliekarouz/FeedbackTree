package com.feedbacktree.flow.core

data class Sink<EventT : Any>(
    val flowHasCompleted: Boolean,
    val eventSink: (EventT) -> Unit
)