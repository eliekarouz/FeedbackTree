package com.feedbacktree.flow

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.feedbacktree.R
import com.feedbacktree.flow.ui.ViewRegistry
import com.feedbacktree.flow.ui.WorkflowLayout
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo

fun <State : StateCompletable<Output>, Output>
        FragmentActivity.startFlow(
    flow: Flow<Unit, State, *, Output, *>,
    onResult: (FlowResult<Output>) -> Unit,
    viewRegistry: ViewRegistry
): Disposable = startFlow(Unit, flow, onResult, viewRegistry)

fun <Input, State : StateCompletable<Output>, Output>
        FragmentActivity.startFlow(
    input: Input,
    flow: Flow<Input, State, *, Output, *>,
    onResult: (FlowResult<Output>) -> Unit,
    viewRegistry: ViewRegistry
): Disposable {

    val factory = FlowViewModel.Factory(input, flow)

    @Suppress("UNCHECKED_CAST")
    val viewModel = ViewModelProviders.of(
        this,
        factory
    )[FlowViewModel::class.java] as FlowViewModel<Input, Output>


    val disposeBag = CompositeDisposable()

    viewModel.output
        .subscribe {
            onResult(it)
        }
        .addTo(disposeBag)

    val layout = WorkflowLayout(this).apply {
        id = R.id.workflow_layout
        start(viewModel.screens, viewRegistry)
    }
    setContentView(layout)
    return disposeBag
}


