/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.login

data class LoginScreen(
    val state: LoginFlow.State,
    val onEvent: (LoginFlow.Event) -> Unit
) {
    val isLoginButtonEnabled: Boolean
        get() = state.email.isNotEmpty() && state.password.isNotEmpty()
}
