/*
 * Created by eliek on 9/28/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

class StateLess<InputT, OutputT>(
    val input: InputT,
    override val flowOutput: OutputT? = null
) : StateCompletable<OutputT>

abstract class StatelessFlow<InputT, OutputT, ViewModelT> :
    Flow<InputT, StateLess<InputT, OutputT>, Unit, OutputT, ViewModelT>(
        reduce = { state, _ -> state },
        feedbacks = listOf()
    ) {
    override fun initialState(input: InputT) = StateLess<InputT, OutputT>(input)

    override fun render(state: StateLess<InputT, OutputT>, context: RenderingContext): ViewModelT {
        return render(state.input, context)
    }

    abstract fun render(input: InputT, context: RenderingContext): ViewModelT
}

