package com.feedbacktree.flow.core

sealed class Step<out State : Any, out Output : Any> {
    data class Advance<out State : Any, out Output : Any>(val newState: State) :
        Step<State, Output>()

    data class End<out State : Any, out Output : Any>(val output: Output) : Step<State, Output>()
}

fun <StateT : Any, OutputT : Any> StateT.advance(): Step<StateT, OutputT> = Step.Advance(this)

fun <StateT : Any> endFlow(): Step<StateT, Unit> = Step.End(Unit)
fun <StateT : Any, OutputT : Any> endFlowWith(output: OutputT): Step<StateT, OutputT> =
    Step.End(output)