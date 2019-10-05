/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.feedbacktree.R
import com.feedbacktree.flow.ui.views.WorkflowLayout
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo

fun <State : StateCompletable<Output>, Output>
        FragmentActivity.startFlow(
    flow: Flow<Unit, State, *, Output, *>,
    onOutput: (Output) -> Unit,
    viewRegistry: ViewRegistry
): Disposable = startFlow(Unit, flow, onOutput, viewRegistry)

fun <Input, State : StateCompletable<Output>, Output>
        FragmentActivity.startFlow(
    input: Input,
    flow: Flow<Input, State, *, Output, *>,
    onOutput: (Output) -> Unit,
    viewRegistry: ViewRegistry
): Disposable {

    val factory = FlowViewModel.Factory(input, flow)

    @Suppress("UNCHECKED_CAST")
    val viewModel = ViewModelProviders.of(
        this,
        factory
    )[FlowViewModel::class.java] as FlowViewModel<Input, State, Output>


    val disposeBag = CompositeDisposable()

    viewModel.output
        .subscribe {
            onOutput(it)
        }
        .addTo(disposeBag)

    val layout = WorkflowLayout(this).apply {
        id = R.id.workflow_layout
        start(viewModel.screens, viewRegistry)
    }
    setContentView(layout)
    return disposeBag
}


