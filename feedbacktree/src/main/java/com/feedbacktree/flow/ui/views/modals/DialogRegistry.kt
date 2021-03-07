/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views.modals

import android.content.Context
import androidx.annotation.StyleRes
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import kotlin.reflect.KClass

class DialogRegistry private constructor(
    var bindings: Map<KClass<out Modal>, DialogBinding<out Modal>> = mapOf()
) {

    constructor(vararg bindings: DialogBinding<*>) : this(
        bindings.map { it.type to it }.toMap().apply {
            check(keys.size == bindings.size) {
                "${bindings.map { it.type }} must not have duplicate entries."
            }
        }
    )

    constructor(vararg registries: DialogRegistry) : this(
        registries.map { it.bindings }
            .reduce { left, right ->
                val duplicateKeys = left.keys.intersect(right.keys)
                check(duplicateKeys.isEmpty()) { "Must not have duplicate entries: $duplicateKeys." }
                left + right
            }
    )

    /**
     * Creates a [DialogRef] (aka [Dialog]) to display [initialModal], which can be updated via calls
     * to [updateDialog].
     */
    fun <ModalT : Modal> buildDialog(
        initialModal: ModalT,
        context: Context,
        viewRegistry: ViewRegistry
    ): DialogRef<ModalT> {
        @Suppress("UNCHECKED_CAST")
        return (bindings[initialModal::class] as? DialogBinding<ModalT>)
            ?.buildDialog(initialModal, this, viewRegistry, context)
            ?: throw IllegalArgumentException(
                "A binding for ${initialModal::class.java.name} must be registered " +
                        "to display $initialModal."
            )
    }

    fun <ModalT : Modal> updateDialog(dialogRef: DialogRef<ModalT>) {
        @Suppress("UNCHECKED_CAST")
        (bindings[dialogRef.modal::class] as? DialogBinding<ModalT>)
            ?.updateDialog(dialogRef)
    }

    fun <ModalT : Modal> cleanUp(dialogRef: DialogRef<ModalT>) {
        @Suppress("UNCHECKED_CAST")
        (bindings[dialogRef.modal::class] as? DialogBinding<ModalT>)
            ?.cleanUpDialog(dialogRef)
    }

    operator fun <ModalT : Modal> plus(binding: DialogBinding<ModalT>): DialogRegistry {
        check(binding.type !in bindings.keys) {
            "Already registered ${bindings[binding.type]} for ${binding.type}, cannot accept $binding."
        }
        return DialogRegistry(bindings + (binding.type to binding))
    }

    operator fun plus(registry: DialogRegistry): DialogRegistry {
        return DialogRegistry(this, registry)
    }

    companion object {
        fun registry(
            @StyleRes dialogThemeResId: Int = 0
        ): DialogRegistry {
            return DialogRegistry(
                AlertDialogBinding(dialogThemeResId = dialogThemeResId)
            ) + ViewModalDialogBinding()
        }
    }
}