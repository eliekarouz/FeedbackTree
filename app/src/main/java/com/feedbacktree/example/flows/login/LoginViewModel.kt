/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example.flows.login

data class LoginViewModel(
    val state: State,
    val sink: (Event) -> Unit
) {
    val email: String
        get() = state.email

    val loginButtonTitle: String
        get() = if (state.isLoggingIn) "Signing In" else "Sign In"

    val isLoginButtonEnabled: Boolean
        get() = state.email.isNotEmpty() && state.password.isNotEmpty()
}
