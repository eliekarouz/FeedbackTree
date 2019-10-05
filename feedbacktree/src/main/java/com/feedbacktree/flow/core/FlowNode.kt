/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import android.annotation.SuppressLint
import com.feedbacktree.flow.utils.logVerbose
import io.reactivex.disposables.Disposable

internal class FlowNode<Input, State : StateCompletable<Output>, Output, Screen>(
    val input: Input,
    val flow: Flow<Input, State, *, Output, Screen>,
    val id: String,
    var disposable: Disposable? = null,
    internal var children: MutableList<FlowNode<*, *, *, *>> = mutableListOf(),
    internal var tempChildren: MutableList<FlowNode<*, *, *, *>> = mutableListOf(),
    var onResult: (Output) -> Unit
) : Disposable {

    fun run() {
        disposable = flow.run(input).subscribe { result ->
            onResult(result)
        }
    }

    fun render(context: RenderingContext): Screen {
        return flow.render(flow.state.value ?: flow.initialState(input), context)
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
    fun <ChildState, ChildOutput, ChildScreen> renderChild(
        flow: Flow<Unit, ChildState, *, ChildOutput, ChildScreen>,
        id: String? = null,
        onResult: (ChildOutput) -> Unit
    ): ChildScreen
            where ChildState : StateCompletable<ChildOutput> =
        renderChild(Unit, flow, id, onResult)

    @SuppressLint("CheckResult")
    fun <Input, ChildState, ChildOutput, ChildScreen> renderChild(
        input: Input,
        flow: Flow<Input, ChildState, *, ChildOutput, ChildScreen>,
        id: String? = null,
        onResult: (ChildOutput) -> Unit
    ): ChildScreen
            where ChildState : StateCompletable<ChildOutput> {
        val flowId = id ?: flow::class.toString()

        logVerbose("renderChild: $flowId, currentLeafNode = ${currentLeafNode.id}, currentLeafNode.children= ${currentLeafNode.children.size}")
        val existingNode = currentLeafNode.children.firstOrNull { it.id == flowId }
        return if (existingNode != null) {

            @Suppress("UNCHECKED_CAST")
            val castedNode = existingNode as FlowNode<*, *, ChildOutput, ChildScreen>
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

    internal fun <Screen> renderNode(node: FlowNode<*, *, *, Screen>): Screen {
        logVerbose("Screen node - start ${node.id}")
        treeStackTraversedNodes.add(node)
        node.tempChildren.clear()

        val rendering = node.render(this)
        logVerbose("Screen - $rendering")
        val currentChildrenFlowKeys = node.tempChildren.map { it.id }

        val childrenToRemove =
            node.children.filter { !currentChildrenFlowKeys.contains(it.id) }
        childrenToRemove.forEach {
            logVerbose("Screen node - removing children ${node.id}")
            it.dispose()
        }

        node.children = node.tempChildren.toMutableList() //
        treeStackTraversedNodes.remove(node)
        logVerbose("Screen node - end ${node.id}, node.children = ${node.children.size}")
        return rendering
    }
}

