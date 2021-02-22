/*
 * Created by eliek on 12/28/2020
 * Copyright (c) 2020 eliekarouz. All rights reserved.
 */

@file:Suppress("DEPRECATION")

package com.feedbacktree.flow.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.feedbacktree.flow.core.BindingsBuilder
import com.feedbacktree.flow.core.Feedback
import com.feedbacktree.flow.core.ObservableSchedulerContext
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.bindShowScreen
import com.feedbacktree.flow.utils.logVerbose
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlin.reflect.KClass

typealias ViewBindingInflater<BindingT> = (layoutInflator: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean) -> BindingT

object LayoutBinder {

    inline fun <reified ScreenT : Any, reified EventT : Any> create(
        @LayoutRes layoutId: Int,
        noinline sink: (ScreenT) -> (EventT) -> Unit,
        noinline build: (LayoutBinderBuilder<ScreenT, EventT>).(View) -> Unit
    ): ViewBinding<ScreenT> = create(
        layoutId = layoutId,
        sink = sink,
        build = { view, _ ->
            build(view)
        }
    )

    inline fun <reified ScreenT : Any, reified EventT : Any> create(
        @LayoutRes layoutId: Int,
        noinline sink: (ScreenT) -> (EventT) -> Unit,
        noinline build: (LayoutBinderBuilder<ScreenT, EventT>).(View, ViewRegistry) -> Unit
    ): ViewBinding<ScreenT> {
        return LayoutResBinding(
            type = ScreenT::class,
            layoutId = layoutId,
            build = build,
            sink = sink
        )
    }

    inline fun <reified ScreenT : Any> create(
        @LayoutRes layoutId: Int,
        noinline build: (LayoutBinderBuilder<ScreenT, NoEvent>).(View) -> Unit
    ) = create(
        layoutId = layoutId,
        sink = { { } },
        build = build
    )

    inline fun <reified ScreenT : Any> create(
        @LayoutRes layoutId: Int,
        noinline build: (LayoutBinderBuilder<ScreenT, NoEvent>).(View, ViewRegistry) -> Unit
    ) = create(
        layoutId = layoutId,
        sink = { { } },
        build = build
    )

    // ViewBinding factories
    inline fun <reified ScreenT : Any, reified EventT : Any, reified BindingT : androidx.viewbinding.ViewBinding> create(
        noinline viewBindingInflater: ViewBindingInflater<BindingT>,
        noinline sink: (ScreenT) -> (EventT) -> Unit,
        noinline build: (LayoutBinderBuilder<ScreenT, EventT>).(BindingT) -> Unit
    ): ViewBinding<ScreenT> = create(
        viewBindingInflater = viewBindingInflater,
        sink = sink,
        build = { view, _ ->
            build(view)
        }
    )

    inline fun <reified ScreenT : Any, reified EventT : Any, reified BindingT : androidx.viewbinding.ViewBinding> create(
        noinline viewBindingInflater: ViewBindingInflater<BindingT>,
        noinline sink: (ScreenT) -> (EventT) -> Unit,
        noinline build: (LayoutBinderBuilder<ScreenT, EventT>).(BindingT, ViewRegistry) -> Unit
    ): ViewBinding<ScreenT> {
        return AndroidViewBinding(
            type = ScreenT::class,
            viewBindingInflater = viewBindingInflater,
            build = build,
            sink = sink
        )
    }

    // The term ViewBinding is a bit confusing here. There is a name collision between feedbacktree.ViewBinding,
    // and AndroidX.ViewBinding.
    class AndroidViewBinding<ScreenT : Any, EventT : Any, BindingT : androidx.viewbinding.ViewBinding>(
        override val type: KClass<ScreenT>,
        val viewBindingInflater: ViewBindingInflater<BindingT>,
        val build: (LayoutBinderBuilder<ScreenT, EventT>).(BindingT, ViewRegistry) -> Unit,
        val sink: (ScreenT) -> (EventT) -> Unit,
    ) : ViewBinding<ScreenT> {

        override fun buildView(
            registry: ViewRegistry,
            initialScreen: ScreenT,
            contextForNewView: Context,
            container: ViewGroup?
        ): View {
            val layoutInflator = LayoutInflater.from(container?.context ?: contextForNewView)
            val viewBinding =
                viewBindingInflater(
                    layoutInflator.cloneInContext(contextForNewView),
                    container,
                    false
                )
            val screenBehaviorSubject = BehaviorSubject.createDefault(initialScreen)
            val mergedEvents by lazy {
                // create the view
                val builder = LayoutBinderBuilder<ScreenT, EventT>()
                builder.build(viewBinding, registry)
                val events = builder.feedbacks.map {
                    val observableSchedulerContext = ObservableSchedulerContext(
                        screenBehaviorSubject,
                        AndroidSchedulers.mainThread()
                    )
                    it(observableSchedulerContext)
                }
                Observable.merge(events)
            }

            var disposable: Disposable? = mergedEvents.subscribe {
                sink(initialScreen).invoke(it)
            }
            logVerbose("LayoutBinder - Attached screen: $type")

            return viewBinding.root.apply {
                bindShowScreen(
                    initialScreen,
                    showScreen = { screen ->
                        screenBehaviorSubject.onNext(screen)
                    },
                    disposeScreenBinding = {
                        logVerbose("LayoutBinder - Detached screen: $type")
                        disposable?.dispose()
                        disposable = null
                    }
                )
            }
        }
    }

    class LayoutResBinding<ScreenT : Any, EventT : Any>(
        override val type: KClass<ScreenT>,
        @LayoutRes val layoutId: Int,
        val build: (LayoutBinderBuilder<ScreenT, EventT>).(View, ViewRegistry) -> Unit,
        val sink: (ScreenT) -> (EventT) -> Unit,
    ) : ViewBinding<ScreenT> {
        override fun buildView(
            registry: ViewRegistry,
            initialScreen: ScreenT,
            contextForNewView: Context,
            container: ViewGroup?
        ): View {
            return LayoutInflater.from(container?.context ?: contextForNewView)
                .cloneInContext(contextForNewView)
                .inflate(layoutId, container, false)
                .apply {
                    val screenBehaviorSubject = BehaviorSubject.createDefault(initialScreen)
                    val mergedEvents by lazy {
                        // create the view
                        val builder = LayoutBinderBuilder<ScreenT, EventT>()
                        builder.build(this, registry)
                        val events = builder.feedbacks.map {
                            val observableSchedulerContext = ObservableSchedulerContext(
                                screenBehaviorSubject,
                                AndroidSchedulers.mainThread()
                            )
                            it(observableSchedulerContext)
                        }
                        Observable.merge(events)
                    }

                    var disposable: Disposable? = mergedEvents.subscribe {
                        sink(initialScreen).invoke(it)
                    }
                    logVerbose("LayoutBinder - Attached screen: $type")

                    bindShowScreen(
                        initialScreen,
                        showScreen = { screen ->
                            screenBehaviorSubject.onNext(screen)
                        },
                        disposeScreenBinding = {
                            logVerbose("LayoutBinder - Detached screen: $type")
                            disposable?.dispose()
                            disposable = null
                        }
                    )
                }
        }
    }
}

class NoEvent private constructor()

class LayoutBinderBuilder<ScreenT : Any, EventT : Any>(
    val feedbacks: MutableList<Feedback<ScreenT, EventT>> = mutableListOf()
) {

    fun bind(bindings: BindingsBuilder<EventT>.(Observable<ScreenT>) -> Unit) {
        feedbacks.add(com.feedbacktree.flow.core.bind(bindings))
    }

    fun <QueryT : Any> react(
        query: (ScreenT) -> QueryT?,
        areEqual: (QueryT, QueryT) -> Boolean,
        effects: (QueryT) -> Observable<EventT>
    ) {
        feedbacks.add(com.feedbacktree.flow.core.react(query, areEqual, effects))
    }

    fun <QueryT : Any> react(
        query: (ScreenT) -> QueryT?,
        effects: (QueryT) -> Observable<EventT>
    ) {
        feedbacks.add(com.feedbacktree.flow.core.react(query, effects))
    }

    fun <QueryT : Any> reactSet(
        query: (ScreenT) -> Set<QueryT>,
        effects: (QueryT) -> Observable<EventT>
    ) {
        feedbacks.add(com.feedbacktree.flow.core.reactSet(query, effects))
    }

    fun <QueryT : Any, QueryID> react(
        queries: (ScreenT) -> Map<QueryID, QueryT>,
        effects: (initial: QueryT, queryObservable: Observable<QueryT>) -> Observable<EventT>
    ) {
        feedbacks.add(com.feedbacktree.flow.core.react(queries, effects))
    }
}

