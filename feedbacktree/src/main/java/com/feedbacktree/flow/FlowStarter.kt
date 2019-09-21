package com.feedbacktree.flow

import androidx.fragment.app.FragmentActivity
import com.feedbacktree.R
import com.feedbacktree.flow.ui.ViewRegistry
import com.feedbacktree.flow.ui.WorkflowLayout
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo

fun <State : StateCompletable<Result>, Result>
        FragmentActivity.startFlow(
    flow: Flow<Unit, State, *, Result, *>,
    onResult: (FlowResult<Result>) -> Unit,
    viewRegistry: ViewRegistry
): Disposable = startFlow(Unit, flow, onResult, viewRegistry)

fun <Input, State : StateCompletable<Result>, Result>
        FragmentActivity.startFlow(
    input: Input,
    flow: Flow<Input, State, *, Result, *>,
    onResult: (FlowResult<Result>) -> Unit,
    viewRegistry: ViewRegistry
): Disposable {

    val disposeBag = CompositeDisposable()
    val rootNode = FlowNode(
        input = input,
        flow = flow,
        id = "RootFlow",
        disposable = disposeBag
    )

    flow.run(input)
        .subscribe {
            onResult(it)
        }
        .addTo(disposeBag)

    val layout = WorkflowLayout(this).apply {
        val renderings = screenChanged.startWith(Unit).map {
            RenderingContext().renderNode(rootNode) as Any
        }
        id = R.id.workflow_layout
        start(renderings, viewRegistry)
    }
    setContentView(layout)
    return rootNode // Disposable
}