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
import com.feedbacktree.flow.core.Sink
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.bindShowViewModel
import com.feedbacktree.flow.utils.logVerbose
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.notests.rxfeedback.ObservableSchedulerContext
import kotlin.reflect.KClass

interface ViewModel<EventT> {
    val sink: Sink<EventT>
}

/**
 * (Experimental)
 */
interface LayoutRunner<ViewModelT : ViewModel<EventT>, EventT> {

    fun feedbacks(): List<Feedback<ViewModelT, EventT>>

    class Binding<ViewModelT : ViewModel<EventT>, EventT>
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
                    bindShowViewModel(
                        initialViewModel
                    ) { viewModel ->
                        if (!viewModel.sink.flowHasCompleted) {
                            screenBehaviorSubject.onNext(viewModel)
                        }
                    }

                    val layoutAttachable =
                        runnerConstructor.invoke(this, registry)
                    val feedbacks = layoutAttachable.feedbacks()
                    val events = feedbacks.map {
                        val observableSchedulerContext = ObservableSchedulerContext(
                            screenBehaviorSubject,
                            AndroidSchedulers.mainThread()
                        )
                        it(observableSchedulerContext)
                    }
                    val mergedEvents = Observable.merge(events)

                    this.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                        var disposable: Disposable? = null

                        override fun onViewAttachedToWindow(p0: View?) {
                            if (this@apply == p0) {
                                logVerbose("LayoutRunner - Attached screen: $type")
                                disposable = mergedEvents.subscribe {
                                    initialViewModel.sink.eventSink.invoke(it)
                                }
                            }

                        }

                        override fun onViewDetachedFromWindow(p0: View?) {
                            if (this@apply == p0) {
                                logVerbose("LayoutRunner - Detached screen: $type")
                                disposable?.dispose()
                                disposable = null
                            }
                        }

                    })
                }
        }
    }

    companion object {
        /**
         * Creates a [ViewBinding] that inflates [layoutId] to show viewModels of type [ViewModelT],
         * using a [LayoutRunner] created by [constructor].
         */
        inline fun <reified ViewModelT : ViewModel<EventT>, EventT> bind(
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
        inline fun <reified ViewModelT : ViewModel<EventT>, EventT> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View) -> LayoutRunner<ViewModelT, EventT>
        ): ViewBinding<ViewModelT> =
            bind(layoutId) { view, _ ->
                constructor.invoke(
                    view
                )
            }

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to "show" viewModels of type [ViewModelT],
         * with a no-op [LayoutRunner]. Handy for showing static views.
         */
        inline fun <reified ViewModelT : ViewModel<EventT>, EventT> bindNoRunner(
            @LayoutRes layoutId: Int
        ): ViewBinding<ViewModelT> =
            bind(layoutId) { _, _ ->
                object : LayoutRunner<ViewModelT, EventT> {
                    override fun feedbacks() = listOf<Feedback<ViewModelT, EventT>>()
                }
            }
    }
}