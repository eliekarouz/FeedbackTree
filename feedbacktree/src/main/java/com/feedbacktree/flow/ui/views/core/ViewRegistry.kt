/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */
package com.feedbacktree.flow.ui.views.core

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.feedbacktree.flow.ui.core.Named
import com.feedbacktree.flow.ui.views.backstack.BackStackContainer
import com.feedbacktree.flow.ui.views.modals.DialogRegistry
import com.feedbacktree.flow.ui.views.modals.ModalContainer
import kotlin.reflect.KClass


class ViewRegistry private constructor(
    private val bindings: Map<KClass<*>, ViewBinding<*>>
) {
    /** [bindings] plus any built-ins. Segregated to keep dup checking simple. */
    private val allBindings = mapOf<KClass<*>, ViewBinding<*>>(
        BackStackContainer.type to BackStackContainer,
        NamedBinding.type to NamedBinding,
        modalContainerViewViewBinding.type to modalContainerViewViewBinding
    ) + bindings

    constructor(vararg bindings: ViewBinding<*>) : this(
        bindings.map
        { it.type to it }.toMap().apply
        {
            check(keys.size == bindings.size) {
                "${bindings.map { it.type }} must not have duplicate entries."
            }
        }
    )

    constructor(vararg registries: ViewRegistry) : this(
        registries.map { it.bindings }
            .reduce { left, right ->
                val duplicateKeys = left.keys.intersect(right.keys)
                check(duplicateKeys.isEmpty()) { "Must not have duplicate entries: $duplicateKeys." }
                left + right
            }
    )

    /**
     * Creates a [View] to display [initialRendering], which can be updated via calls
     * to [View.showRendering].
     */
    fun <RenderingT : Any> buildView(
        initialRendering: RenderingT,
        contextForNewView: Context,
        container: ViewGroup? = null
    ): View {
        @Suppress("UNCHECKED_CAST")
        return (allBindings[initialRendering::class] as? ViewBinding<RenderingT>)
            ?.buildView(this, initialRendering, contextForNewView, container)
            ?.apply {
                checkNotNull(showRenderingTag?.showing) {
                    "View.bindShowRendering must be called for $this."
                }
            }
            ?: throw IllegalArgumentException(
                "A binding for ${initialRendering::class.java.name} must be registered " +
                        "to display $initialRendering."
            )
    }

    /**
     * Creates a [View] to display [initialRendering], and which can handle calls
     * to [View.showRendering].
     */
    fun <RenderingT : Any> buildView(
        initialRendering: RenderingT,
        container: ViewGroup
    ): View {
        return buildView(initialRendering, container.context, container)
    }

    operator fun <RenderingT : Any> plus(binding: ViewBinding<RenderingT>): ViewRegistry {
        check(binding.type !in bindings.keys) {
            "Already registered ${bindings[binding.type]} for ${binding.type}, cannot accept $binding."
        }
        return ViewRegistry(bindings + (binding.type to binding))
    }

    operator fun plus(registry: ViewRegistry): ViewRegistry {
        return ViewRegistry(this, registry)
    }

    private companion object {
        val modalContainerViewViewBinding = ModalContainer.Binding(DialogRegistry.registry())
    }
}

private object NamedBinding : ViewBinding<Named<*>>
by BuilderBinding(
    type = Named::class,
    viewConstructor = { viewRegistry, initialRendering, contextForNewView, container ->
        val view = viewRegistry.buildView(initialRendering.wrapped, contextForNewView, container)
        view.apply {
            val wrappedUpdater = showRenderingTag!!.showRendering
            bindShowRendering(initialRendering) {
                wrappedUpdater.invoke(it.wrapped)
            }
        }
    }
)
