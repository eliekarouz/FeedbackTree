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
import com.feedbacktree.flow.ui.views.core.BuilderBinding
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.bindShowScreen
import com.feedbacktree.flow.utils.logVerbose
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlin.reflect.KClass

/**
 * (Experimental)
 */
@Deprecated("Deprecated in favor of LayoutBinder.create")
interface LayoutRunner<ScreenT : Any, EventT : Any> {

    fun feedbacks(): List<Feedback<ScreenT, EventT>>

    class Binding<ScreenT : Any, EventT : Any>
    constructor(
        override val type: KClass<ScreenT>,
        @LayoutRes private val layoutId: Int,
        private val binderConstructor: (View, ViewRegistry) -> LayoutRunner<ScreenT, EventT>,
        private val sink: (ScreenT) -> (EventT) -> Unit
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
                        val layoutAttachable = binderConstructor.invoke(this, registry)
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

    companion object {
        /**
         * Creates a [ViewBinding] that inflates [layoutId] to show screens of type [ScreenT],
         * using a [LayoutRunner] created by [constructor].
         */
        inline fun <reified ScreenT : Any, EventT : Any> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View, ViewRegistry) -> LayoutRunner<ScreenT, EventT>,
            noinline sink: (ScreenT) -> (EventT) -> Unit
        ): ViewBinding<ScreenT> = Binding(
            type = ScreenT::class,
            layoutId = layoutId,
            binderConstructor = constructor,
            sink = sink
        )

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to show screens of type [ScreenT],
         * using a [LayoutRunner] created by [constructor].
         */
        inline fun <reified ScreenT : Any, EventT : Any> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View) -> LayoutRunner<ScreenT, EventT>,
            noinline sink: (ScreenT) -> (EventT) -> Unit
        ): ViewBinding<ScreenT> =
            bind(
                layoutId = layoutId,
                constructor = { view, _ ->
                    constructor.invoke(
                        view
                    )
                },
                sink = sink
            )

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to "show" screens of type [ScreenT].
         * Handy for showing static views.
         */
        inline fun <reified ScreenT : Any> bindStatic(
            @LayoutRes layoutId: Int
        ): ViewBinding<ScreenT> = BuilderBinding(
            type = ScreenT::class,
            viewConstructor = { _, initialScreen, contextForNewView, container ->
                LayoutInflater.from(container?.context ?: contextForNewView)
                    .cloneInContext(contextForNewView)
                    .inflate(layoutId, container, false).apply {
                        bindShowScreen(initialScreen,
                            showScreen = { },
                            disposeScreenBinding = { }
                        )
                    }
            }
        )

    }
}