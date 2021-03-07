/*
 * Created by eliek on 2/13/2021
 * Copyright (c) 2021 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.modals

import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.flow.ui.views.core.backPresses
import com.feedbacktree.tutorials.databinding.ModalExamplesBinding
import com.feedbacktree.utils.actionBarTitle
import com.jakewharton.rxbinding3.view.clicks

val ModalsExampleLayoutBinder = LayoutBinder.create(
    viewBindingInflater = ModalExamplesBinding::inflate,
    sink = ModalsScreen::sink
) { viewBinding ->

    viewBinding.actionBarTitle = "Modals Examples"

    bind {
        events = listOf(
            viewBinding.alertButton.clicks().map { Event.ShowAlertClicked },
            viewBinding.alertModalWithViewButton.clicks()
                .map { Event.ShowAlertWithCustomViewClicked },
            viewBinding.fullScreenModalButton.clicks().map { Event.ShowFullScreenModalClicked },
            viewBinding.customSizeModalButton.clicks().map { Event.ShowCustomSizeModalClicked },
            viewBinding.root.backPresses().map { Event.BackClicked }
        )
    }
}