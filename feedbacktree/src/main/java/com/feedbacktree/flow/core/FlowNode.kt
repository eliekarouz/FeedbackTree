/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import com.feedbacktree.flow.utils.logInfo
import com.feedbacktree.flow.utils.logVerbose
import com.feedbacktree.flow.utils.mapNotNull
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class RenderingContext<EventT : Any, OutputT : Any> internal constructor(
    val sink: (EventT) -> Unit,
    private val node: FlowNode<*, *, EventT, OutputT, *>
) {

    internal var tempNodes = mutableListOf<FlowNode<*, *, *, *, *>>()
        private set

    fun <EventU : Any> sink(transform: (EventU) -> EventT): (EventU) -> Unit {
        return { eventU ->
            sink(transform(eventU))
        }
    }

    fun sendEvent(event: EventT) {
        sink(event)
    }

    fun complete(output: OutputT) {
        node.complete(output)
    }

    fun <InputT : Any, ChildStateT : Any, ChildOutputT : Any, ChildViewModelT> renderChild(
        input: InputT,
        flow: Flow<InputT, ChildStateT, *, ChildOutputT, ChildViewModelT>,
        id: String? = null,
        onResult: (ChildOutputT) -> Unit
    ): ChildViewModelT {
        val flowId = id ?: flow::class.toString()
        logVerbose("renderChild: $flowId, node = ${node.id}, node.children= ${node.children.size}")
        val existingNode = node.children.firstOrNull { it.id == flowId }

        return if (existingNode != null) {
            @Suppress("UNCHECKED_CAST")
            val castedNode = existingNode as FlowNode<*, *, *, ChildOutputT, ChildViewModelT>
            // We update the onResult block with the new block provided block that will be called when the child output ends.
            // In fact, it could be that the Parent State captured inside the onResult block when the child flow was started
            // is not valid anymore.
            castedNode.onResult = onResult
            tempNodes.add(castedNode)
            castedNode.render()
        } else {
            logVerbose("renderChild - Create new node $flowId")
            val newNode = FlowNode(
                input = input,
                flow = flow,
                id = flowId,
                renderingTrigger = node.renderingTrigger,
                onResult = onResult
            )
            newNode.run()
            tempNodes.add(newNode)
            newNode.render()
        }
    }


    fun <ChildStateT : Any, ChildOutputT : Any, ChildViewModelT> renderChild(
        flow: Flow<Unit, ChildStateT, *, ChildOutputT, ChildViewModelT>,
        id: String? = null,
        onResult: (ChildOutputT) -> Unit
    ) = renderChild(Unit, flow, id, onResult)
}

internal class FlowNode<InputT : Any, StateT : Any, EventT : Any, OutputT : Any, ViewModelT>(
    val input: InputT,
    val flow: Flow<InputT, StateT, EventT, OutputT, ViewModelT>,
    val id: String,
    val renderingTrigger: PublishSubject<Unit>,
    var onResult: (OutputT) -> Unit
) : Disposable {

    private val eventsPublishSubject = PublishSubject.create<EventT>()
    private var disposable: Disposable? = null
    private val currentState: StateT?
        get() = flowState.value?.state

    internal var children: MutableList<FlowNode<*, *, *, *, *>> = mutableListOf()

    private val isRunning: Boolean
        get() = disposable != null


    fun run() {
        if (isRunning) {
            logInfo("Flow with id: $id is already running")
        }
        disposable = createFlow().subscribe {
            onResult(it)
        }
    }

    private val className = javaClass.simpleName

    /**
     * You need first to [run] the state machine first.
     */
    private var flowState = BehaviorSubject.create<FlowState<StateT, OutputT>>()


    fun render(): ViewModelT {
        val context = RenderingContext(
            sink = { event ->
                eventsPublishSubject.onNext(event)
            },
            node = this
        )
        val viewModel = flow.render(currentState ?: flow.initialState(input), context)
        val currentChildrenFlowIds = context.tempNodes.map { it.id }
        val childrenToRemove = children.filter {
            !currentChildrenFlowIds.contains(it.id)
        }
        childrenToRemove.forEach {
            it.dispose()
        }
        children = context.tempNodes
        return viewModel
    }

    private val outputPublishSubject = PublishSubject.create<OutputT>()
    private val enterStatePublishSubject = PublishSubject.create<FlowEvent<StateT, EventT>>()

    private fun backdoorFeedback(): Feedback<FlowState<StateT, OutputT>, FlowEvent<StateT, EventT>> =
        bindWithScheduler { osc ->
            return@bindWithScheduler Bindings(
                subscriptions = listOf(
                    osc.source.subscribe { flowState.onNext(it) },


                    osc.source.skip(1).subscribe { state ->
                        // We will only trigger a rendering pass when the flow doesn't emit an output and ends.
                        // The output will be propagated synchronously to the parent flows. Once a parent/grandparent captures the output,
                        // its state will be updated and it's at that time that we will trigger the rendering pass.
                        if (state.flowOutput == null) {
                            renderingTrigger.onNext(Unit)
                        }
                    },
                    // Observables.system in RxFeedback adds an observeOn(scheduler) before returning the stream of states.
                    // This means the if we collect the flow output directly from the system, the output will be delayed.
                    // We are using this feedback loop to do that because we don't have an observerOn(scheduler) and the state is emitted instantly
                    // as soon as it goes out of the stepper.
                    osc.source.mapNotNull { it.flowOutput }.subscribe { flowOutput ->
                        outputPublishSubject.onNext(flowOutput)
                    }
                ),
                events = listOf(
                    eventsPublishSubject.map<FlowEvent<StateT, EventT>> { event ->
                        FlowEvent.StandardEvent(
                            event
                        )
                    }
                )
            )
        }

    private fun childrenResultEventFeedback(): Feedback<FlowState<StateT, OutputT>, FlowEvent<StateT, EventT>> =
        {
            // This is a custom a feedback that actually forwards the events immediately.
            // This will only be used to forward outputs from the children flows into this flow.
            // No need to add observeOn(scheduler) because all the flows will be running on the main-thread.
            // This guarantees to have no re-entrancy because the child flow will complete due to some event and
            // we will be processing the output and updating the parents(grandparents) states synchronously.
            enterStatePublishSubject
        }

    private fun createFlow(): Observable<OutputT> {
        val stepperWithLog: (StateT, EventT) -> Step<StateT, OutputT> = { state, event ->
            logInfo("$className: $event")
            flow.stepper(state, event)
        }

        val wrappedFeedbacks = flow.feedbacks.map { feedback ->
            wrapFeedback<StateT, EventT, OutputT>(feedback)
        }

        val system = Observables.system(
            initialState = FlowState<StateT, OutputT>(
                state = flow.initialState(input),
                flowOutput = null
            ),
            reduce = wrapStepper(stepperWithLog),
            scheduler = AndroidSchedulers.mainThread(),
            scheduledFeedback = wrappedFeedbacks + backdoorFeedback() + childrenResultEventFeedback()
        )

        val stateEncodedOutput = system.flatMap {
            Observable.empty<OutputT>()
        }

        // Although we are collecting the output directly from the outputPublishSubject,
        // we are using Observable.merge just to bound the lifetime of the system to the lifecycle of the returned
        // observable.
        return Observable.merge(stateEncodedOutput, outputPublishSubject)
            .take(1)
    }

    fun complete(output: OutputT) {
        outputPublishSubject.onNext(output)
    }

    override fun isDisposed(): Boolean = disposable == null

    override fun dispose() {
        if (!isDisposed) {
            children.forEach { it.dispose() }
            disposable?.dispose()
            disposable = null
            children.clear()
        }
    }
}

private data class FlowState<StateT, OutputT>(
    internal val state: StateT,
    internal val flowOutput: OutputT?
)


internal sealed class FlowEvent<StateT, EventT> {
    internal data class StandardEvent<StateT, EventT>(val event: EventT) :
        FlowEvent<StateT, EventT>()

    internal data class EnterStateEvent<StateT, EventT>(val state: StateT) :
        FlowEvent<StateT, EventT>()
}


private fun <StateT : Any, EventT : Any, OutputT : Any> wrapStepper(reduce: (StateT, EventT) -> Step<StateT, OutputT>)
        : (FlowState<StateT, OutputT>, FlowEvent<StateT, EventT>) -> FlowState<StateT, OutputT> {
    return { flowState, flowEvent ->
        when (flowEvent) {
            is FlowEvent.StandardEvent -> {
                when (val stateOutputT = reduce(flowState.state, flowEvent.event)) {
                    is Step.State -> flowState.copy(state = stateOutputT.state)
                    is Step.Output -> flowState.copy(flowOutput = stateOutputT.output)
                }
            }
            is FlowEvent.EnterStateEvent -> flowState.copy(state = flowEvent.state)
        }
    }
}


private fun <StateT : Any, EventT : Any, OutputT : Any> wrapFeedback(feedback: Feedback<StateT, EventT>): Feedback<FlowState<StateT, OutputT>, FlowEvent<StateT, EventT>> {
    return { flowStateObservableSchedulerContext ->
        val stateObservableSchedulerContext = ObservableSchedulerContext(
            source = flowStateObservableSchedulerContext.source.map { it.state },
            scheduler = flowStateObservableSchedulerContext.scheduler
        )
        val observableEvents = feedback(stateObservableSchedulerContext)
        observableEvents.map { event ->
            FlowEvent.StandardEvent<StateT, EventT>(event)
        }
    }
}
