package com.feedbacktree.flow.core

sealed class Step<out StateT : Any, out OutputT : Any> {
    data class State<StateT : Any, OutputT : Any>(val state: StateT) : Step<StateT, OutputT>()
    data class Output<StateT : Any, OutputT : Any>(val output: OutputT) : Step<StateT, OutputT>()
}

fun <StateT : Any, OutputT : Any> StateT.advance(): Step<StateT, OutputT> =
    Step.State(this)

fun <StateT : Any> endFlow(): Step<StateT, Unit> =
    Step.Output(Unit)

fun <StateT : Any, OutputT : Any> endFlowWith(output: OutputT): Step<StateT, OutputT> =
    Step.Output(output)