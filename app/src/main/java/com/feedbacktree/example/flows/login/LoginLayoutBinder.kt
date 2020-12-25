/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.login

import android.view.View
import android.widget.Button
import com.feedbacktree.example.R
import com.feedbacktree.example.util.FTEditText
import com.feedbacktree.flow.core.Bindings
import com.feedbacktree.flow.core.bind
import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable

class LoginLayoutBinder(private val view: View) : LayoutBinder<LoginScreen, Event> {

    private val emailEditText: FTEditText = view.findViewById(R.id.inputEmail)
    private val passwordEditText: FTEditText = view.findViewById(R.id.inputPassword)
    private val btnLogin: Button = view.findViewById(R.id.btnLogin)

    override fun feedbacks() = listOf(bindUI())

    private fun bindUI() = bind<LoginScreen, Event> { screen ->
        Bindings(
            subscriptions = listOf(
                screen.map { it.emailText }.subscribe { emailEditText.text = it },
                screen.map { it.passwordText }.subscribe { passwordEditText.text = it },
                screen.map { it.loginButtonTitle }.subscribe { btnLogin.text = it },
                screen.map { it.isLoginButtonEnabled }.subscribe { btnLogin.isEnabled = it }
            ),
            events = listOf<Observable<Event>>(
                emailEditText.textChanges().map { Event.EnteredEmail(it.toString()) },
                passwordEditText.textChanges().map { Event.EnteredPassword(it.toString()) },
                btnLogin.clicks().map { Event.ClickedLogin }
            )
        )
    }

    companion object : ViewBinding<LoginScreen> by LayoutBinder.bind(
        R.layout.login, ::LoginLayoutBinder, LoginScreen::sink
    )
}