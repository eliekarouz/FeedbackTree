/*
 * Created by eliek on 2/13/2021
 * Copyright (c) 2021 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.modals

import android.widget.Button
import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.flow.ui.views.core.backPresses
import com.feedbacktree.tutorials.R
import com.feedbacktree.utils.actionBarTitle
import com.jakewharton.rxbinding3.view.clicks

val ModalsExampleLayoutBinder = LayoutBinder.create(
    layoutId = R.layout.modal_examples,
    sink = ModalsScreen::sink
) { view ->

    view.actionBarTitle = "Modals Examples"

    val alertButton = view.findViewById<Button>(R.id.topViewButton)
    val alertWithViewButton = view.findViewById<Button>(R.id.alertModalWithViewButton)
    val fullScreenModalButton = view.findViewById<Button>(R.id.buttomViewButton)

    bind {
        events = listOf(
            alertButton.clicks().map { Event.ShowAlertClicked },
            alertWithViewButton.clicks().map { Event.ShowAlertWithCustomViewClicked },
            fullScreenModalButton.clicks().map { Event.ShowFullScreenModalClicked },
            view.backPresses().map { Event.BackClicked }
        )
    }
}