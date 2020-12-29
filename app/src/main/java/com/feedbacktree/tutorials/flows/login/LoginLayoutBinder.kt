/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.login

import android.widget.Button
import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.tutorials.R
import com.feedbacktree.utils.FTEditText
import com.feedbacktree.utils.actionBarTitle
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges

val LoginLayoutBinder = LayoutBinder.create(
    layoutId = R.layout.login,
    sink = LoginScreen::sink,
) { view ->

    view.actionBarTitle = "Login"

    val emailEditText: FTEditText = view.findViewById(R.id.inputEmail)
    val passwordEditText: FTEditText = view.findViewById(R.id.inputPassword)
    val btnLogin: Button = view.findViewById(R.id.btnLogin)

    bind { screen ->
        subscriptions = listOf(
            screen.map { it.emailText }.subscribe { emailEditText.text = it },
            screen.map { it.passwordText }.subscribe { passwordEditText.text = it },
            screen.map { it.loginButtonTitle }.subscribe { btnLogin.text = it },
            screen.map { it.isLoginButtonEnabled }.subscribe { btnLogin.isEnabled = it }
        )
        events = listOf(
            emailEditText.textChanges().map { Event.EnteredEmail(it.toString()) },
            passwordEditText.textChanges().map { Event.EnteredPassword(it.toString()) },
            btnLogin.clicks().map { Event.ClickedLogin }
        )
    }
}