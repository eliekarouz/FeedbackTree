/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FlowViewModel<InputT : Any, StateT : Any, OutputT : Any>(
    input: InputT,
    flow: Flow<InputT, StateT, *, OutputT, *>
) : ViewModel() {

    private val _output = BehaviorSubject.create<OutputT>()
    private val renderingTrigger = PublishSubject.create<Unit>()

    private val rootNode: FlowNode<*, *, *, *, *> = {
        FlowNode(
            input = input,
            flow = flow,
            id = "RootFlow",
            renderingTrigger = renderingTrigger,
            onResult = {
                _output.onNext(it)
            }
        ).apply {
            run()
        }
    }()


    val output: Observable<OutputT> = _output
    val screens: Observable<Any> = renderingTrigger.startWith(Unit).map {
        rootNode.render() as Any
    }

    override fun onCleared() {
        super.onCleared()
        rootNode.dispose()
    }

    class Factory<InputT : Any, StateT : Any, OutputT : Any>(
        private val input: InputT,
        private val flow: Flow<InputT, StateT, *, OutputT, *>
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FlowViewModel(input, flow) as T
        }

    }
}