package com.feedbacktree.example.ui

import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.zippyyum.subtemp.signinviews.feedbacktree.fingerprint.EnterFingerprintLayoutRunner

val appViewRegistry =
    ViewRegistry(LoginLayoutRunner, EnterFingerprintLayoutRunner)