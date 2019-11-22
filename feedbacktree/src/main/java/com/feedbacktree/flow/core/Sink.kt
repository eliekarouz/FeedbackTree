package com.feedbacktree.flow.core

data class Sink<EventT>(
    internal val flowHasCompleted: Boolean,
    internal val eventSink: (EventT) -> Unit
)