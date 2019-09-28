/*
 * Created by eliek on 9/28/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

class StateLess<Input, Output>(
    val input: Input,
    override val flowOutput: Output? = null
) : StateCompletable<Output>

abstract class StatelessFlow<Input, Output, Rendering> :
    Flow<Input, StateLess<Input, Output>, Unit, Output, Rendering>(
        reduce = { state, _ -> state },
        feedbacks = listOf()
    ) {
    override fun initialState(input: Input) = StateLess<Input, Output>(input)

    override fun render(state: StateLess<Input, Output>, context: RenderingContext): Rendering {
        return render(state.input, context)
    }

    abstract fun render(input: Input, context: RenderingContext): Rendering
}

