/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.fingerprint

import com.feedbacktree.example.R
import com.feedbacktree.flow.ui.views.LayoutBinder


val EnterFingerprintLayoutBinder = LayoutBinder.create(
    layoutId = R.layout.fingerprint_alert_dialog,
    sink = EnterFingerprintScreen::sink
) { _ ->
}