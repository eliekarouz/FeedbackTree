/*
 * Created by eliek on 12/31/2020
 * Copyright (c) 2020 eliekarouz. All rights reserved.
 */

@file:Suppress("RemoveExplicitTypeArguments")

package com.feedbacktree.howtoguides

import com.feedbacktree.flow.core.Feedback
import com.feedbacktree.flow.core.bind
import io.reactivex.Observable

private val productsFeedback: Feedback<ProductsState, ProductsEvent> =
    bind<ProductsState, ProductsEvent> { state: Observable<ProductsState> ->
        subscriptions = listOf()
        events = listOf()
    }

