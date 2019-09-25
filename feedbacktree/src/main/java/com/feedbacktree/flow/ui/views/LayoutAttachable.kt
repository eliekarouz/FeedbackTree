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
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.bindShowRendering
import com.feedbacktree.flow.utils.logVerbose
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import kotlin.reflect.KClass

/**
 * (Experimental)
 * Equivalent to [LayoutRunner] but slightly modified to make use of feeback loops for
 * rendering.
 */
interface LayoutAttachable {
    fun attachFeedbacks(): Disposable

    class Binding<RenderingT : Any>
    constructor(
        override val type: KClass<RenderingT>,
        @LayoutRes private val layoutId: Int,
        private val runnerConstructor: (View, RenderingT, ViewRegistry) -> LayoutAttachable
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

                    bindShowRendering(
                        initialRendering,
                        {}
                    )
                    val layoutAttachable =
                        runnerConstructor.invoke(this, initialRendering, registry)
                    this.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                        var disposable: Disposable? = null

                        override fun onViewAttachedToWindow(p0: View?) {
                            if (this@apply == p0) {
                                logVerbose("LayoutAttachable - Attached screen: $type")
                                disposable = layoutAttachable.attachFeedbacks()
                            }

                        }

                        override fun onViewDetachedFromWindow(p0: View?) {
                            if (this@apply == p0) {
                                logVerbose("LayoutAttachable - Detached screen: $type")
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
         * using a [LayoutAttachable] created by [constructor].
         */
        inline fun <reified RenderingT : Any> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View, RenderingT, ViewRegistry) -> LayoutAttachable
        ): ViewBinding<RenderingT> = Binding(
            RenderingT::class,
            layoutId,
            constructor
        )

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to show renderings of type [RenderingT],
         * using a [LayoutAttachable] created by [constructor].
         */
        inline fun <reified RenderingT : Any> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View, RenderingT) -> LayoutAttachable
        ): ViewBinding<RenderingT> =
            bind(layoutId) { view, rendering, _ ->
                constructor.invoke(
                    view,
                    rendering
                )
            }

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to "show" renderings of type [RenderingT],
         * with a no-op [LayoutAttachable]. Handy for showing static views.
         */
        inline fun <reified RenderingT : Any> bindNoRunner(
            @LayoutRes layoutId: Int
        ): ViewBinding<RenderingT> =
            bind(layoutId) { _, _ ->
                object : LayoutAttachable {
                    override fun attachFeedbacks(): Disposable = Disposables.empty()
                }
            }
    }
}