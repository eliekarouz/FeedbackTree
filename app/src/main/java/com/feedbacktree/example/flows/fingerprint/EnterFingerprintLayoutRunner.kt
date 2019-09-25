/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.fingerprint

import android.view.View
import com.feedbacktree.example.R
import com.feedbacktree.flow.ui.views.LayoutRunner
import com.feedbacktree.flow.ui.views.core.ViewBinding


class EnterFingerprintLayoutRunner(val view: View) : LayoutRunner<EnterFingerprintScreen> {

    override fun showRendering(rendering: EnterFingerprintScreen) {
        println("Rendering EnterFingerprintLayoutRunner")
    }

    companion object : ViewBinding<EnterFingerprintScreen> by LayoutRunner.bind(
        R.layout.fingerprint_alert_dialog, ::EnterFingerprintLayoutRunner
    )
}