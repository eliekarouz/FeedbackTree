package com.feedbacktree.example.flows.login

data class LoginScreen(
    val state: LoginFlow.State,
    val onEvent: (LoginFlow.Event) -> Unit
) {
    val isLoginButtonEnabled: Boolean
        get() = state.email.isNotEmpty() && state.password.isNotEmpty()
}
