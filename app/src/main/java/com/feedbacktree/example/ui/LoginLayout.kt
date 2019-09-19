package com.feedbacktree.example.ui

import android.view.View
import com.feedbacktree.example.R
import com.feedbacktree.example.flows.login.LoginFlow
import com.feedbacktree.example.flows.login.LoginScreen
import com.feedbacktree.flow.ui.LayoutRunner
import com.feedbacktree.flow.ui.ViewBinding
import io.reactivex.disposables.Disposable
import org.notests.rxfeedback.Bindings
import org.notests.rxfeedback.bind

class LoginLayout(val view: View, val screen: LoginScreen) : LayoutRunner<LoginScreen> {

    override fun attachFeedbacks(): Disposable = with(view) {
        fun bindUI() = bind<LoginFlow.State, LoginFlow.Event> { state ->
            Bindings(listOf(), listOf())
        }
        return screen.flow.attachFeedbacks(bindUI())
    }

    companion object : ViewBinding<LoginScreen> by LayoutRunner.bind(
        R.layout.login, ::LoginLayout
    )

}