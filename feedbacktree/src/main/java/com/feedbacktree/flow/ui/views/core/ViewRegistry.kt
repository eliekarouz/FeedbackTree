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
     * Creates a [View] to display [initialViewModel], which can be updated via calls
     * to [View.showViewModel].
     */
    fun <ViewModelT : Any> buildView(
        initialViewModel: ViewModelT,
        contextForNewView: Context,
        container: ViewGroup? = null
    ): View {
        @Suppress("UNCHECKED_CAST")
        return (allBindings[initialViewModel::class] as? ViewBinding<ViewModelT>)
            ?.buildView(this, initialViewModel, contextForNewView, container)
            ?.apply {
                checkNotNull(showViewModelTag?.showing) {
                    "View.bindShowViewModel must be called for $this."
                }
            }
            ?: throw IllegalArgumentException(
                "A binding for ${initialViewModel::class.java.name} must be registered " +
                        "to display $initialViewModel."
            )
    }

    /**
     * Creates a [View] to display [initialViewModel], and which can handle calls
     * to [View.showViewModel].
     */
    fun <ViewModelT : Any> buildView(
        initialViewModel: ViewModelT,
        container: ViewGroup
    ): View {
        return buildView(initialViewModel, container.context, container)
    }

    operator fun <ViewModelT : Any> plus(binding: ViewBinding<ViewModelT>): ViewRegistry {
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
    viewConstructor = { viewRegistry, initialViewModel, contextForNewView, container ->
        val view = viewRegistry.buildView(initialViewModel.wrapped, contextForNewView, container)
        view.apply {
            val wrappedUpdater = showViewModelTag!!.showViewModel
            val cleanupViewModel = cleanupViewModelTag
            bindShowViewModel(initialViewModel,
                showViewModel = {
                    wrappedUpdater.invoke(it.wrapped)
                },
                cleanupViewModel = {
                    cleanupViewModel?.invoke()
                })
        }
    }
)