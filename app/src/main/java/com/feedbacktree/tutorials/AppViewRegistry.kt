/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials

import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.modals.DialogRegistry
import com.feedbacktree.flow.ui.views.modals.ModalContainer
import com.feedbacktree.tutorials.flows.counter.CounterLayoutBinder
import com.feedbacktree.tutorials.flows.login.LoginLayoutBinder
import com.feedbacktree.tutorials.flows.modals.CovidInfoLayoutBinder
import com.feedbacktree.tutorials.flows.modals.ModalsExampleLayoutBinder
import com.feedbacktree.tutorials.flows.tutorialsroot.TutorialsLayoutBinder

private val dialogRegistry =
    DialogRegistry.registry(R.style.FTDialogTheme)

val appViewRegistry = ViewRegistry(
    ModalContainer.Binding(dialogRegistry),
    TutorialsLayoutBinder,
    CounterLayoutBinder,
    LoginLayoutBinder,
    ModalsExampleLayoutBinder,
    CovidInfoLayoutBinder
)