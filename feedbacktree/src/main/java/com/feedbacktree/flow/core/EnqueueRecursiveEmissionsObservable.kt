/*
 * Created by eliek on 2/19/2023
 * Copyright (c) 2023 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.internal.fuseable.HasUpstreamObservableSource
import io.reactivex.internal.observers.BasicFuseableObserver
import io.reactivex.plugins.RxJavaPlugins

/**
 * Transforms recursive(re-entrant) emissions into sequential emissions.
 *
 * In case onNext did not complete yet, using this operator guarantees that the next emission does not
 * start unless the previous emission is fully processed by the downstream observer.
 * In other terms, in case of recursion, the stack trace will not grow but the `onNext` emissions will be done sequentially.
 */
internal fun <T> Observable<T>.enqueueRecursiveEmissions(): Observable<T> {
    return RxJavaPlugins.onAssembly(EnqueueRecursiveEmissionsObservable(this))
}

private class EnqueueRecursiveEmissionsObservable<T>(
    val source: ObservableSource<T>
) : Observable<T>(), HasUpstreamObservableSource<T> {

    override fun source(): ObservableSource<T> {
        return source
    }

    override fun subscribeActual(observer: Observer<in T>) {
        source.subscribe(EnqueueRecursiveEmissionsObserver(observer))
    }

    class EnqueueRecursiveEmissionsObserver<T : Any>(actual: Observer<in T>) :
        BasicFuseableObserver<T, T>(actual) {

        private val queueLock = SequentialExecutionQueue()

        override fun onNext(t: T) {
            if (sourceMode != NONE) {
                downstream.onNext(null)
                return
            }
            queueLock.enqueueOrExecuteAll {
                if (done) {
                    return@enqueueOrExecuteAll
                }
                downstream.onNext(t)
            }
        }

        override fun poll(): T? {
            return qd.poll()
        }

        override fun requestFusion(mode: Int): Int {
            return transitiveBoundaryFusion(mode)
        }
    }
}

internal class SequentialExecutionQueue {

    private val queue = mutableListOf<() -> Unit>()

    fun enqueueOrExecuteAll(mutate: () -> Unit) {
        var executeMutation = enqueue(mutate) ?: return
        do {
            executeMutation()
            val nextExecuteMutation = dequeue() ?: return
            executeMutation = nextExecuteMutation
        } while (true)
    }

    private fun enqueue(mutate: () -> Unit): (() -> Unit)? {
        synchronized(this) {
            val wasEmpty = queue.isEmpty()
            queue.add(mutate)
            return if (wasEmpty) {
                mutate
            } else null
        }
    }

    private fun dequeue(): (() -> Unit)? {
        synchronized(this) {
            if (queue.isNotEmpty()) {
                queue.removeAt(0)
            }
            return queue.firstOrNull()
        }
    }
}