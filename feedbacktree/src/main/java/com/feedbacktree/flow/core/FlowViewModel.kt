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

class FlowViewModel<Input, Output>(
    input: Input,
    flow: Flow<Input, *, *, Output, *>
) : ViewModel() {

    private val _output = BehaviorSubject.create<Output>()

    private val rootNode: FlowNode<*, *, *> = {
        val disposeBag = CompositeDisposable()
        flow.run(input)
            .subscribe {
                _output.onNext(it)
            }
            .addTo(disposeBag)

        FlowNode(
            input = input,
            flow = flow,
            id = "RootFlow",
            disposable = disposeBag
        )
    }()


    val output: Observable<Output> = _output
    val screens: Observable<Any> = screenChanged.startWith(Unit).map {
        RenderingContext().renderNode(rootNode) as Any
    }

    override fun onCleared() {
        super.onCleared()
        rootNode.dispose()
    }

    class Factory<Input, Output>(
        private val input: Input,
        private val flow: Flow<Input, *, *, Output, *>
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FlowViewModel(input, flow) as T
        }

    }
}