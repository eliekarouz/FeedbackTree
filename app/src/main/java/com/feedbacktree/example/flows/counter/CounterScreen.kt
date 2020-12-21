package com.feedbacktree.example.flows.counter

data class CounterScreen(
    private val state: State,
    val sink: (Event) -> Unit
) {
    val counterText: String = state.counter.toString()
}