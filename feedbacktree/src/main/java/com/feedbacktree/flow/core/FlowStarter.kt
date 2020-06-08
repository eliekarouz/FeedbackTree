/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.feedbacktree.R
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.views.DialogFlowRenderer
import com.feedbacktree.flow.ui.views.WorkflowLayout
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.modals.DialogRegistry
import com.feedbacktree.flow.utils.Optional
import com.feedbacktree.flow.utils.asOptional
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo

fun <StateT : Any, OutputT : Any>
        FragmentActivity.startFlow(
    flow: Flow<Unit, StateT, *, OutputT, *>,
    onOutput: (OutputT) -> Unit,
    viewRegistry: ViewRegistry
): Disposable = startFlow(Unit, flow, onOutput, viewRegistry)

fun <InputT : Any, StateT : Any, OutputT : Any>
        FragmentActivity.startFlow(
    input: InputT,
    flow: Flow<InputT, StateT, *, OutputT, *>,
    onOutput: (OutputT) -> Unit,
    viewRegistry: ViewRegistry
): Disposable {

    val factory = FlowViewModel.Factory(input, flow)

    @Suppress("UNCHECKED_CAST")
    val viewModel = ViewModelProviders.of(
        this,
        factory
    )[FlowViewModel::class.java] as FlowViewModel<InputT, StateT, OutputT>


    val disposeBag = CompositeDisposable()

    viewModel.output
        .subscribe {
            onOutput(it)
        }
        .addTo(disposeBag)

    val layout = WorkflowLayout(this).apply {
        id = R.id.workflow_layout
        start(viewModel.viewModels, viewRegistry)
    }
    setContentView(layout)
    return disposeBag
}


/**
 * Utility that can be used to start [Flow]s which produce [Modal]s.
 * It's useful when you need to use FeedbackTree in a areas that are not using it yet.
 */
fun <InputT : Any, StateT : Any, OutputT : Any> FragmentActivity.startModalsFlow(
    input: InputT,
    flow: Flow<InputT, StateT, *, OutputT, *>,
    viewRegistry: ViewRegistry,
    dialogRegistry: DialogRegistry
): Observable<OutputT> {
    return Observable.create<OutputT> { emitter ->
        val rootNode: FlowNode<*, *, *, *> = {
            FlowNode(
                input = input,
                flow = flow,
                id = "RootFlow",
                onResult = {
                    emitter.onNext(it)
                }
            ).apply {
                run()
            }
        }()

        val viewModels: Observable<Optional<Any>> = newViewModelTrigger.startWith(Unit).map {
            RenderingContext().renderNode(rootNode).asOptional
        }

        val renderer = DialogFlowRenderer(this, viewRegistry, dialogRegistry)

        val screenDisposable = viewModels
            .doFinally { renderer.update(listOf()) }
            .subscribe { screen ->
                when (screen) {
                    is Optional.Some -> renderer.update(listOfNotNull(screen.data as? Modal))
                    is Optional.None -> renderer.update(listOf())
                }
            }
        emitter.setCancellable {
            rootNode.dispose()
            screenDisposable.dispose()
        }
    }.take(1)
}