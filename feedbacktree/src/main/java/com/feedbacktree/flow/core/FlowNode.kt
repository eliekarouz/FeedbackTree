/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import android.annotation.SuppressLint
import com.feedbacktree.flow.utils.logVerbose
import io.reactivex.disposables.Disposable

internal class FlowNode<InputT : Any, StateT : Any, OutputT : Any, ViewModelT>(
    val input: InputT,
    val flow: Flow<InputT, StateT, *, OutputT, ViewModelT>,
    val id: String,
    var disposable: Disposable? = null,
    internal var children: MutableList<FlowNode<*, *, *, *>> = mutableListOf(),
    internal var tempChildren: MutableList<FlowNode<*, *, *, *>> = mutableListOf(),
    var onResult: (OutputT) -> Unit
) : Disposable {

    fun run() {
        disposable = flow.run(input).subscribe { result ->
            onResult(result)
        }
    }

    fun render(context: RenderingContext): ViewModelT {
        return flow.render(flow.currentState ?: flow.initialState(input), context)
    }

    override fun isDisposed(): Boolean = disposable == null

    override fun dispose() {
        if (!isDisposed) {
            children.forEach { it.dispose() }
            disposable?.dispose()
            disposable = null
            children.clear()
            tempChildren.clear()
        }
    }
}

class RenderingContext {

    private val treeStackTraversedNodes: MutableList<FlowNode<*, *, *, *>> = mutableListOf()

    private val currentLeafNode
        get() = treeStackTraversedNodes.last()

    @SuppressLint("CheckResult")
    fun <ChildStateT : Any, ChildOutputT : Any, ChildViewModelT> renderChild(
        flow: Flow<Unit, ChildStateT, *, ChildOutputT, ChildViewModelT>,
        id: String? = null,
        onResult: (ChildOutputT) -> Unit
    ): ChildViewModelT =
        renderChild(Unit, flow, id, onResult)

    @SuppressLint("CheckResult")
    fun <InputT : Any, ChildStateT : Any, ChildOutputT : Any, ChildViewModelT> renderChild(
        input: InputT,
        flow: Flow<InputT, ChildStateT, *, ChildOutputT, ChildViewModelT>,
        id: String? = null,
        onResult: (ChildOutputT) -> Unit
    ): ChildViewModelT {
        val flowId = id ?: flow::class.toString()

        logVerbose("renderChild: $flowId, currentLeafNode = ${currentLeafNode.id}, currentLeafNode.children= ${currentLeafNode.children.size}")
        val existingNode = currentLeafNode.children.firstOrNull { it.id == flowId }
        return if (existingNode != null) {

            @Suppress("UNCHECKED_CAST")
            val castedNode = existingNode as FlowNode<*, *, ChildOutputT, ChildViewModelT>
            // We update the onResult block with the new block provided block that will be called when the child output ends.
            // In fact, it could be that the Parent State captured inside the onResult block when the child flow was started
            // is not valid anymore.
            castedNode.onResult = onResult
            currentLeafNode.tempChildren.add(castedNode)
            renderNode(castedNode)
        } else {
            logVerbose("renderChild - Create new node $flowId")
            val newNode = FlowNode(
                input = input,
                flow = flow,
                id = flowId,
                onResult = onResult
            )
            newNode.run()
            currentLeafNode.tempChildren.add(newNode)
            renderNode(newNode)
        }
    }

    internal fun <ViewModelT> renderNode(node: FlowNode<*, *, *, ViewModelT>): ViewModelT {
        logVerbose("ViewModel node - start ${node.id}")
        treeStackTraversedNodes.add(node)
        node.tempChildren.clear()

        val viewModel = node.render(this)
        logVerbose("ViewModel - $viewModel")
        val currentChildrenFlowKeys = node.tempChildren.map { it.id }

        val childrenToRemove =
            node.children.filter { !currentChildrenFlowKeys.contains(it.id) }
        childrenToRemove.forEach {
            logVerbose("ViewModel node - removing children ${node.id}")
            it.dispose()
        }

        node.children = node.tempChildren.toMutableList() //
        treeStackTraversedNodes.remove(node)
        logVerbose("ViewModel node - end ${node.id}, node.children = ${node.children.size}")
        return viewModel
    }
}

