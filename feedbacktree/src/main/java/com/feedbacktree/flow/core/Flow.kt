/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

data class Flow<InputT : Any, StateT : Any, EventT : Any, OutputT : Any, ScreenT>(
    val initialState: (InputT) -> StateT,
    val stepper: (StateT, EventT) -> Step<StateT, OutputT>,
    val feedbacks: List<Feedback<StateT, EventT>> = listOf(),
    val render: (StateT, RenderingContext<EventT, OutputT>) -> ScreenT
)