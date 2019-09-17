/*
 * Copyright 2019 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedbacktree.flow.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.feedbacktree.flow.ui.LayoutRunner.Companion.bind
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import kotlin.reflect.KClass

/**
 * An object that handles [View.showRendering] calls for a view inflated
 * from a layout resource in response to [ViewRegistry.buildView].
 * (Use [BuilderBinding] if you want to build views from code rather than
 * layouts.)
 *
 * Typical usage is to have a [LayoutRunner]'s `companion object` implement
 * [ViewBinding] by delegating to [LayoutRunner.bind], specifying the layout resource
 * it expects to drive.
 *
 *   class HelloLayoutRunner(view: View) : LayoutRunner<Rendering> {
 *     private val messageView: TextView = view.findViewById(R.id.hello_message)
 *
 *     override fun showRendering(rendering: Rendering) {
 *       messageView.text = rendering.message
 *       messageView.setOnClickListener { rendering.onClick(Unit) }
 *     }
 *
 *     companion object : ViewBinding<Rendering> by bind(
 *         R.layout.hello_goodbye_layout, ::HelloLayoutRunner
 *     )
 *   }
 *
 * This pattern allows us to assemble a [ViewRegistry] out of the
 * [LayoutRunner] classes themselves.
 *
 *    val TicTacToeViewBuilders = ViewRegistry(
 *        NewGameLayoutRunner, GamePlayLayoutRunner, GameOverLayoutRunner
 *    )
 *
 * Also note that two flavors of [contructor][LayoutRunner.Binding.runnerConstructor]
 * are accepted by [bind]. Every [LayoutRunner] constructor must accept an [View].
 * Optionally, they can also have a second [ViewRegistry] argument, to allow
 * nested renderings to be displayed via nested calls to [ViewRegistry.buildView].
 */
interface LayoutRunner<RenderingT : Any> {
    fun attachFeedbacks(): Disposable

    class Binding<RenderingT : Any>
    constructor(
        override val type: KClass<RenderingT>,
        @LayoutRes private val layoutId: Int,
        private val runnerConstructor: (View, RenderingT, ViewRegistry) -> LayoutRunner<RenderingT>
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
                    val layoutRunner = runnerConstructor.invoke(this, initialRendering, registry)
                    this.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                        var disposable: Disposable? = null

                        override fun onViewAttachedToWindow(p0: View?) {
                            if (this@apply == p0) {
                                println("LayoutRunner - Attached screen: $type")
                                disposable = layoutRunner.attachFeedbacks()
                            }

                        }

                        override fun onViewDetachedFromWindow(p0: View?) {
                            if (this@apply == p0) {
                                println("LayoutRunner - Detached screen: $type")
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
        inline fun <reified RenderingT : Any> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View, RenderingT, ViewRegistry) -> LayoutRunner<RenderingT>
        ): ViewBinding<RenderingT> = Binding(RenderingT::class, layoutId, constructor)

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to show renderings of type [RenderingT],
         * using a [LayoutRunner] created by [constructor].
         */
        inline fun <reified RenderingT : Any> bind(
            @LayoutRes layoutId: Int,
            noinline constructor: (View, RenderingT) -> LayoutRunner<RenderingT>
        ): ViewBinding<RenderingT> =
            bind(layoutId) { view, rendering, _ -> constructor.invoke(view, rendering) }

        /**
         * Creates a [ViewBinding] that inflates [layoutId] to "show" renderings of type [RenderingT],
         * with a no-op [LayoutRunner]. Handy for showing static views.
         */
        inline fun <reified RenderingT : Any> bindNoRunner(
            @LayoutRes layoutId: Int
        ): ViewBinding<RenderingT> = bind(layoutId) { _, _ ->
            object : LayoutRunner<RenderingT> {
                override fun attachFeedbacks(): Disposable = Disposables.empty()
            }
        }
    }
}
