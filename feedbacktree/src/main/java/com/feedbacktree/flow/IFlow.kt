package com.feedbacktree.flow

import io.reactivex.Observable

sealed class FlowResult<out Result> {
    object Aborted : FlowResult<Nothing>()
    data class Completed<out Result>(val result: Result) : FlowResult<Result>()
}

fun <Result> completed(result: Result): FlowResult<Result> = FlowResult.Completed(result)
fun <Result> aborted(): FlowResult<Result> = FlowResult.Aborted

interface IFlow<Input, Output> {
    fun run(input: Input): Observable<FlowResult<Output>>
}