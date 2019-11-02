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
import com.feedbacktree.flow.ui.views.core.bindShowRendering
import com.feedbacktree.flow.utils.logVerbose
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.notests.rxfeedback.ObservableSchedulerContext
import kotlin.reflect.KClass

interface Screen<Event> {
    val sink: Sink<Event>
}

/**
 * (Experimental)
 */
interface LayoutRunner<RenderingT : Screen<Event>, Event> {

    fun feedbacks(): List<Feedback<RenderingT, Event>>

    class Binding<RenderingT : Screen<Event>, Event>
    constructor(
        override val type: KClass<RenderingT>,
        @LayoutRes private val layoutId: Int,
        private val runnerConstructor: (View, ViewRegistry) -> LayoutRunner<RenderingT, Event>
    ) : ViewBinding<RenderingT> {
        override fun buildView(
            registry: ViewRegistry,
            initialRendering: RenderingT,
            contextForNewView: Context,
            container: ViewGroup?
        ): View {
            return LayoutInflater.from(container?.context ?: contextForNewView)
                .cloneInContext(contextForNewView)
                .inflate(layoutId, container, false)
                .apply {

                    val screenBehaviorSubject = BehaviorSubject.createDefault(initialRendering)
                    bindShowRendering(
                        initialRendering
                    ) { rendering ->
                        if (!rendering.sink.flowHasCompleted) {
                            screenBehaviorSubject.onNext(rendering)
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
                                    initialRendering.sink.eventSink.invoke(it)
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
         * Creates a [ViewBinding] that inflates [layoutId] to show renderings of type [RenderingT],
         * using a [LayoutRunner] created by [constructor].
         */
        inline fun <reified RenderingT : Screen<Event>, Event> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View, ViewRegistry) -> LayoutRunner<RenderingT, Event>
        ): ViewBinding<RenderingT> = Binding(
            RenderingT::
            class,
            layoutId,
            constructor
        )

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to show renderings of type [RenderingT],
         * using a [LayoutRunner] created by [constructor].
         */
        inline fun <reified RenderingT : Screen<Event>, Event> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View) -> LayoutRunner<RenderingT, Event>
        ): ViewBinding<RenderingT> =
            bind(layoutId) { view, _ ->
                constructor.invoke(
                    view
                )
            }

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to "show" renderings of type [RenderingT],
         * with a no-op [LayoutRunner]. Handy for showing static views.
         */
        inline fun <reified RenderingT : Screen<Event>, Event> bindNoRunner(
            @LayoutRes layoutId: Int
        ): ViewBinding<RenderingT> =
            bind(layoutId) { _, _ ->
                object : LayoutRunner<RenderingT, Event> {
                    override fun feedbacks() = listOf<Feedback<RenderingT, Event>>()
                }
            }
    }
}