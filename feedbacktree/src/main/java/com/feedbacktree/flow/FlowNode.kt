package com.feedbacktree.flow

import android.annotation.SuppressLint
import com.feedbacktree.flow.utils.logVerbose
import io.reactivex.disposables.Disposable

internal class FlowNode<Input, State : StateCompletable<*>, Rendering>(
    val input: Input,
    val flow: Flow<Input, State, *, *, Rendering>,
    val id: String,
    var disposable: Disposable? = null,
    internal var children: MutableList<FlowNode<*, *, *>> = mutableListOf(),
    internal var tempChildren: MutableList<FlowNode<*, *, *>> = mutableListOf()
) : Disposable {

    fun render(context: RenderingContext): Rendering {
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

    private val treeStackTraversedNodes: MutableList<FlowNode<*, *, *>> = mutableListOf()

    private val currentLeafNode
        get() = treeStackTraversedNodes.last()

    @SuppressLint("CheckResult")
    fun <ChildState, ChildResult, ChildRendering> renderChild(
        flow: Flow<Unit, ChildState, *, ChildResult, ChildRendering>,
        id: String? = null,
        onResult: (FlowResult<ChildResult>) -> Unit
    ): ChildRendering
            where ChildState : StateCompletable<ChildResult> =
        renderChild(Unit, flow, id, onResult)

    @SuppressLint("CheckResult")
    fun <Input, ChildState, ChildResult, ChildRendering> renderChild(
        input: Input,
        flow: Flow<Input, ChildState, *, ChildResult, ChildRendering>,
        id: String? = null,
        onResult: (FlowResult<ChildResult>) -> Unit
    ): ChildRendering
            where ChildState : StateCompletable<ChildResult> {
        val flowId = id ?: flow::class.toString()

        logVerbose("renderChild: $flowId, currentLeafNode = ${currentLeafNode.id}, currentLeafNode.children= ${currentLeafNode.children.size}")
        val existingNode = currentLeafNode.children.firstOrNull { it.id == flowId }
        return if (existingNode != null) {
            val castedNode = existingNode as FlowNode<*, *, ChildRendering>
            currentLeafNode.tempChildren.add(castedNode)
            renderNode(castedNode)
        } else {
            logVerbose("renderChild - Create new node $flowId")
            val disposable = flow.run(input).subscribe { result ->
                onResult(result)
            }
            val newNode =
                FlowNode(
                    input = input,
                    flow = flow,
                    id = flowId,
                    disposable = disposable
                )
            currentLeafNode.tempChildren.add(newNode)
            renderNode(newNode)
        }
    }

    internal fun <Rendering> renderNode(node: FlowNode<*, *, Rendering>): Rendering {
        logVerbose("Rendering node - start ${node.id}")
        treeStackTraversedNodes.add(node)
        node.tempChildren.clear()

        val rendering = node.render(this)
        logVerbose("Rendering - $rendering")
        val currentChildrenFlowKeys = node.tempChildren.map { it.id }

        val childrenToRemove =
            node.children.filter { !currentChildrenFlowKeys.contains(it.id) }
        childrenToRemove.forEach {
            logVerbose("Rendering node - removing children ${node.id}")
            it.dispose()
        }

        node.children = node.tempChildren.toMutableList() //
        treeStackTraversedNodes.remove(node)
        logVerbose("Rendering node - end ${node.id}, node.children = ${node.children.size}")
        return rendering
    }
}

