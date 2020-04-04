/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.login

import android.view.View
import com.feedbacktree.example.R
import com.feedbacktree.flow.core.Bindings
import com.feedbacktree.flow.core.bind
import com.feedbacktree.flow.ui.views.LayoutRunner
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import kotlinx.android.synthetic.main.login.view.*

class LoginLayoutRunner(private val view: View) : LayoutRunner<LoginViewModel, Event> {
    override fun feedbacks() = listOf(bindUI())

    private fun bindUI() = bind<LoginViewModel, Event> { screen ->
        with(view) {
            Bindings(
                subscriptions = listOf(
                    screen.map { it.isLoginButtonEnabled }.subscribe { btnLogin.isEnabled = it }
                ),
                events = listOf<Observable<Event>>(
                    inputEmail.textChanges().map { Event.EnteredEmail(it.toString()) },
                    inputPassword.textChanges().map { Event.EnteredPassword(it.toString()) },
                    btnLogin.clicks().map { Event.ClickedLogin }
                )
            )
        }
    }

    companion object : ViewBinding<LoginViewModel> by LayoutRunner.bind(
        R.layout.login, ::LoginLayoutRunner
    )
}