/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject

class FlowViewModel<Input, State : StateCompletable<Output>, Output>(
    input: Input,
    flow: Flow<Input, State, *, Output, *>
) : ViewModel() {

    private val _output = BehaviorSubject.create<Output>()

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


    val output: Observable<Output> = _output
    val screens: Observable<Any> = screenChanged.startWith(Unit).map {
        RenderingContext().renderNode(rootNode) as Any
    }

    override fun onCleared() {
        super.onCleared()
        rootNode.dispose()
    }

    class Factory<Input, State : StateCompletable<Output>, Output>(
        private val input: Input,
        private val flow: Flow<Input, State, *, Output, *>
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FlowViewModel(input, flow) as T
        }

    }
}