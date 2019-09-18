package com.feedbacktree.example.ui

import android.view.View
import com.feedbacktree.example.R
import com.feedbacktree.example.flows.login.LoginScreen
import com.feedbacktree.flow.ui.LayoutRunner
import com.feedbacktree.flow.ui.ViewBinding
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables

class LoginLayout(val view: View, val screen: LoginScreen) : LayoutRunner<LoginScreen> {

    override fun attachFeedbacks(): Disposable {
        return Disposables.empty()
    }

    companion object : ViewBinding<LoginScreen> by LayoutRunner.bind(
        R.layout.login, ::LoginLayout
    )
}