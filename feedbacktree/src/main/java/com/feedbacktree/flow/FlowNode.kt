package com.feedbacktree.flow

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import com.feedbacktree.R
import com.feedbacktree.flow.ui.ViewRegistry
import com.feedbacktree.flow.ui.WorkflowLayout
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo

class FlowNode<State : StateCompletable<*>, Event, Rendering>(
    val flow: Flow<State, Event, *, Rendering>,
    val id: String,
    var disposable: Disposable? = null,
    internal var children: MutableList<FlowNode<*, *, *>> = mutableListOf(),
    internal var tempChildren: MutableList<FlowNode<*, *, *>> = mutableListOf()
) : Disposable {

    fun render(context: RenderingContext): Rendering {
        return flow.render(flow.state.value ?: flow.initialState, context)
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

class RenderingContext(private val treeStackTraversedNodes: MutableList<FlowNode<*, *, *>> = mutableListOf()) {

    private val currentLeafNode
        get() = treeStackTraversedNodes.last()

    @SuppressLint("CheckResult")
    fun <ChildState, ChildEvent, ChildResult, ChildRendering, Event> renderChild(
        flow: Flow<ChildState, ChildEvent, ChildResult, ChildRendering>,
        toEvent: (FlowResult<ChildResult>) -> Event
    ): ChildRendering
            where ChildState : StateCompletable<ChildResult> {
        println("renderChild: ${flow.key}, currentLeafNode = ${currentLeafNode.flow.key}, currentLeafNode.children= ${currentLeafNode.children.size}")
        val existingNode = currentLeafNode.children.firstOrNull { it.flow.key == flow.key }
        return if (existingNode != null) {
            val castedNode = existingNode as FlowNode<*, *, ChildRendering>
            currentLeafNode.tempChildren.add(castedNode)
            renderNode(castedNode)
        } else {
            val parentNode = currentLeafNode as FlowNode<*, Event, *>
            println("renderChild - Create new node ${flow.key}")
            val disposable = flow.run().map { childResult ->
                toEvent(childResult)
            }.subscribe {
                parentNode
                    .flow
                    .childrenResultPublishSubject
                    .onNext(it)
            }

            val newNode = FlowNode(flow, id = flow.key, disposable = disposable)
            currentLeafNode.tempChildren.add(newNode)
            renderNode(newNode)
        }
    }

    internal fun <Rendering> renderNode(node: FlowNode<*, *, Rendering>): Rendering {
        println("Rendering node - start ${node.flow.key}")
        treeStackTraversedNodes.add(node)
        node.tempChildren.clear()

        val rendering = node.render(this)
        println("Rendering - $rendering")
        val currentChildrenFlowKeys = node.tempChildren.map { it.flow.key }

        val childrenToRemove =
            node.children.filter { !currentChildrenFlowKeys.contains(it.flow.key) }
        childrenToRemove.forEach {
            println("Rendering node - removing children ${node.flow.key}")
            it.dispose()
        }

        node.children = node.tempChildren.toMutableList() //
        treeStackTraversedNodes.remove(node)
        println("Rendering node - end ${node.flow.key}, node.children = ${node.children.size}")
        return rendering
    }
}

fun <State : StateCompletable<Result>, Event, Result>
        FragmentActivity.startFlow(
    flow: Flow<State, Event, Result, *>,
    onResult: (FlowResult<Result>) -> Unit,
    viewRegistry: ViewRegistry
): Disposable {

    val disposeBag = CompositeDisposable()
    val rootNode = FlowNode(flow, id = flow.key, disposable = disposeBag)

    flow.run()
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

