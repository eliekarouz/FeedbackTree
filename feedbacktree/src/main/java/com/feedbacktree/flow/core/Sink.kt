package com.feedbacktree.flow.core

data class Sink<Event>(
    internal val flowHasCompleted: Boolean,
    internal val eventSink: (Event) -> Unit
)