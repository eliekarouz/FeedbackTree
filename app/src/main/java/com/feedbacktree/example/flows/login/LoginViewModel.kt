/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.login

import com.feedbacktree.flow.core.Sink
import com.feedbacktree.flow.core.ViewModel

data class LoginViewModel(
    val state: State,
    override val sink: Sink<Event>
) : ViewModel<Event> {
    val isLoginButtonEnabled: Boolean
        get() = state.email.isNotEmpty() && state.password.isNotEmpty()
}
