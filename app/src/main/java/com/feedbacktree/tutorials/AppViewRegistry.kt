/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials

import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.tutorials.flows.counter.CounterLayoutBinder
import com.feedbacktree.tutorials.flows.login.LoginLayoutBinder
import com.feedbacktree.tutorials.flows.root.RootLayoutBinder

val appViewRegistry = ViewRegistry(
    RootLayoutBinder,
    CounterLayoutBinder,
    LoginLayoutBinder
)