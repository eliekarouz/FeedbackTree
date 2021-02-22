/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.login

import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.flow.ui.views.core.backPresses
import com.feedbacktree.tutorials.databinding.LoginBinding
import com.feedbacktree.utils.actionBarTitle
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges

val LoginLayoutBinder = LayoutBinder.create(
    viewBindingInflater = LoginBinding::inflate,
    sink = LoginScreen::sink,
) { viewBinding ->

    viewBinding.actionBarTitle = "Login"

    bind { screen ->
        subscriptions = listOf(
            screen.map { it.emailText }.subscribe { viewBinding.inputEmail.text = it },
            screen.map { it.passwordText }.subscribe { viewBinding.inputPassword.text = it },
            screen.map { it.loginButtonTitle }.subscribe { viewBinding.btnLogin.text = it },
            screen.map { it.isLoginButtonEnabled }.subscribe { viewBinding.btnLogin.isEnabled = it }
        )
        events = listOf(
            viewBinding.inputEmail.textChanges().map { Event.EnteredEmail(it.toString()) },
            viewBinding.inputPassword.textChanges().map { Event.EnteredPassword(it.toString()) },
            viewBinding.btnLogin.clicks().map { Event.ClickedLogin },
            viewBinding.root.backPresses().map { Event.BackPressed }
        )
    }
}