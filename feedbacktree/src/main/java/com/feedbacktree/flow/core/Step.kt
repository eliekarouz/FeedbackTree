package com.feedbacktree.flow.core

sealed class Step<out StateT, out OutputT> {
    data class State<StateT, OutputT>(val state: StateT) : Step<StateT, OutputT>()
    data class Output<StateT, OutputT>(val output: OutputT) : Step<StateT, OutputT>()
}

fun <StateT, OutputT> StateT.enterState(): Step<StateT, OutputT> = Step.State(this)

fun <StateT> endFlow(): Step<StateT, Unit> = Step.Output(Unit)
fun <StateT, OutputT> endFlowWith(output: OutputT): Step<StateT, OutputT> = Step.Output(output)