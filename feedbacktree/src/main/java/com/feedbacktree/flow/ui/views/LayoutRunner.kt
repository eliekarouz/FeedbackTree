/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.feedbacktree.flow.core.Feedback
import com.feedbacktree.flow.core.ObservableSchedulerContext
import com.feedbacktree.flow.core.ViewModel
import com.feedbacktree.flow.ui.views.core.BuilderBinding
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.bindShowViewModel
import com.feedbacktree.flow.utils.logVerbose
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlin.reflect.KClass

/**
 * (Experimental)
 */
interface LayoutRunner<ViewModelT : ViewModel<EventT>, EventT : Any> {

    fun feedbacks(): List<Feedback<ViewModelT, EventT>>

    class Binding<ViewModelT : ViewModel<EventT>, EventT : Any>
    constructor(
        override val type: KClass<ViewModelT>,
        @LayoutRes private val layoutId: Int,
        private val runnerConstructor: (View, ViewRegistry) -> LayoutRunner<ViewModelT, EventT>
    ) : ViewBinding<ViewModelT> {
        override fun buildView(
            registry: ViewRegistry,
            initialViewModel: ViewModelT,
            contextForNewView: Context,
            container: ViewGroup?
        ): View {
            return LayoutInflater.from(container?.context ?: contextForNewView)
                .cloneInContext(contextForNewView)
                .inflate(layoutId, container, false)
                .apply {

                    val screenBehaviorSubject = BehaviorSubject.createDefault(initialViewModel)
                    val mergedEvents by lazy {
                        // create the view
                        val layoutAttachable = runnerConstructor.invoke(this, registry)
                        val feedbacks = layoutAttachable.feedbacks()
                        val events = feedbacks.map {
                            val observableSchedulerContext = ObservableSchedulerContext(
                                screenBehaviorSubject,
                                AndroidSchedulers.mainThread()
                            )
                            it(observableSchedulerContext)
                        }
                        Observable.merge(events)
                    }

                    var disposable: Disposable? = mergedEvents.subscribe {
                        initialViewModel.sink.eventSink.invoke(it)
                    }
                    logVerbose("LayoutRunner - Attached screen: $type")

                    bindShowViewModel(
                        initialViewModel,
                        showViewModel = { viewModel ->
                            if (!viewModel.sink.flowHasCompleted) {
                                screenBehaviorSubject.onNext(viewModel)
                            }
                        },
                        cleanupViewModel = {
                            logVerbose("LayoutRunner - Detached screen: $type")
                            disposable?.dispose()
                            disposable = null
                        }
                    )
                }
        }
    }

    companion object {
        /**
         * Creates a [ViewBinding] that inflates [layoutId] to show viewModels of type [ViewModelT],
         * using a [LayoutRunner] created by [constructor].
         */
        inline fun <reified ViewModelT : ViewModel<EventT>, EventT : Any> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View, ViewRegistry) -> LayoutRunner<ViewModelT, EventT>
        ): ViewBinding<ViewModelT> = Binding(
            ViewModelT::
            class,
            layoutId,
            constructor
        )

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to show viewModels of type [ViewModelT],
         * using a [LayoutRunner] created by [constructor].
         */
        inline fun <reified ViewModelT : ViewModel<EventT>, EventT : Any> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View) -> LayoutRunner<ViewModelT, EventT>
        ): ViewBinding<ViewModelT> =
            bind(layoutId) { view, _ ->
                constructor.invoke(
                    view
                )
            }

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to "show" viewModels of type [ViewModelT].
         * Handy for showing static views.
         */
        inline fun <reified ViewModelT : Any> bindStatic(
            @LayoutRes layoutId: Int
        ): ViewBinding<ViewModelT> = BuilderBinding(
            type = ViewModelT::class,
            viewConstructor = { _, initialViewModel, contextForNewView, container ->
                LayoutInflater.from(container?.context ?: contextForNewView)
                    .cloneInContext(contextForNewView)
                    .inflate(layoutId, container, false).apply {
                        bindShowViewModel(initialViewModel,
                            showViewModel = { },
                            cleanupViewModel = { }
                        )
                    }
            }
        )

    }
}