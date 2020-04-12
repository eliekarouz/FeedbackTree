/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class FlowViewModel<InputT : Any, OutputT : Any>(
    input: InputT,
    flow: Flow<InputT, *, *, OutputT, *>
) : ViewModel() {

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


    val output: Observable<OutputT> = _output
    val viewModels: Observable<Any> = newViewModelTrigger.startWith(Unit).map {
        RenderingContext().renderNode(rootNode) as Any
    }

    override fun onCleared() {
        super.onCleared()
        rootNode.dispose()
    }

    class Factory<InputT : Any, OutputT : Any>(
        private val input: InputT,
        private val flow: Flow<InputT, *, *, OutputT, *>
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FlowViewModel(input, flow) as T
        }

    }
}