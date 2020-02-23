package com.feedbacktree.flow.core.migrationhelper

import androidx.fragment.app.FragmentActivity
import com.feedbacktree.flow.core.*
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * This class is useful when you need to run a flow without going
 * through [FlowFragment] or [FragmentActivity.startFlow].
 * It's a utility that can be used when migrating your project to [FeedbackTree]
 */
class FlowStarter<InputT, StateT : StateCompletable<OutputT>, OutputT>(
    input: InputT,
    flow: Flow<InputT, StateT, *, OutputT, *>
) {

    private val _output = BehaviorSubject.create<OutputT>()

    private val rootNode: FlowNode<*, *, *, *> = {
        FlowNode(
            input = input,
            flow = flow,
            id = "RootFlow",
            onResult = {
                _output.onNext(it)
            }
        ).apply {
            run()
        }
    }()

    fun dispose() {
        return rootNode.dispose()
    }

    val output: Observable<OutputT> = _output

    val viewModels: Observable<Any> = newViewModelTrigger.startWith(Unit).map {
        RenderingContext().renderNode(rootNode) as Any
    }
}