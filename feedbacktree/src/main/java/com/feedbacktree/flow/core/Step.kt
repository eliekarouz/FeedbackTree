package com.feedbacktree.flow.core

//sealed class Step<out StateT, out OutputT> {
//    data class Advance<StateT, OutputT>(val state: StateT) : Step<StateT, OutputT>()
//    data class End<StateT, OutputT>(val output: OutputT) : Step<StateT, OutputT>()
//}
//
//fun <StateT, OutputT> StateT.advance(): Step<StateT, OutputT> = Step.Advance(this)
//
//fun <StateT> endFlow(): Step<StateT, Unit> = Step.End(Unit)
//fun <StateT, OutputT> endFlowWith(output: OutputT): Step<StateT, OutputT> = Step.End(output)