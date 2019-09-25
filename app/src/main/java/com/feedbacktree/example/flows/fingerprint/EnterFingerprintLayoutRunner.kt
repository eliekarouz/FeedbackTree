package com.zippyyum.subtemp.signinviews.feedbacktree.fingerprint

import android.view.View
import com.feedbacktree.example.R
import com.feedbacktree.example.flows.fingerprint.EnterFingerprintScreen
import com.feedbacktree.flow.ui.views.LayoutRunner
import com.feedbacktree.flow.ui.views.core.ViewBinding


class EnterFingerprintLayoutRunner(val view: View) : LayoutRunner<EnterFingerprintScreen> {

    override fun showRendering(rendering: EnterFingerprintScreen) {

    }

    companion object : ViewBinding<EnterFingerprintScreen> by LayoutRunner.bind(
        R.layout.fingerprint_alert_dialog, ::EnterFingerprintLayoutRunner
    )
}