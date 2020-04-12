/*
 * Created by eliek on 9/28/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core


abstract class StatelessFlow<InputT : Any, OutputT : Any, ViewModelT> :
    Flow<InputT, InputT, Unit, OutputT, ViewModelT>(
        stepper = { state, _ -> state.advance() },
        feedbacks = listOf()
    ) {
    override fun initialState(input: InputT) = input
}

