/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.ui

import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.feedbacktree.example.R
import com.feedbacktree.example.flows.login.Event
import com.feedbacktree.example.flows.login.LoginScreen
import com.feedbacktree.flow.ui.views.LayoutRunner
import com.feedbacktree.flow.ui.views.core.ViewBinding
import kotlinx.android.synthetic.main.login.view.*

class LoginLayoutRunner(private val view: View) :
    LayoutRunner<LoginScreen> {

    private var subscribedToEvents = false

    override fun showRendering(rendering: LoginScreen) = with(view) {
        // Subscriptions
        btnLogin.isEnabled = rendering.isLoginButtonEnabled

        // Events
        if (!subscribedToEvents) {
            inputEmail.doAfterTextChanged { rendering.onEvent(Event.EnteredEmail(it.toString())) }
            inputPassword.doAfterTextChanged { rendering.onEvent(Event.EnteredPassword(it.toString())) }
            btnLogin.setOnClickListener { rendering.onEvent(Event.ClickedLogin) }
            subscribedToEvents = true
        }
    }

    companion object : ViewBinding<LoginScreen> by LayoutRunner.bind(
        R.layout.login, ::LoginLayoutRunner
    )
}