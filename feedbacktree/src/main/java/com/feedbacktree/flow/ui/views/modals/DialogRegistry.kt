/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views.modals

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
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
     * Creates a [DialogRef] (aka [Dialog]) to display [initialRendering], which can be updated via calls
     * to [updateDialog].
     */
    fun <RenderingT : Modal> buildDialog(
        initialRendering: RenderingT,
        context: Context,
        viewRegistry: ViewRegistry
    ): DialogRef<RenderingT> {
        @Suppress("UNCHECKED_CAST")
        return (bindings[initialRendering::class] as? DialogBinding<RenderingT>)
            ?.buildDialog(initialRendering, this, viewRegistry, context)
            ?: throw IllegalArgumentException(
                "A binding for ${initialRendering::class.java.name} must be registered " +
                        "to display $initialRendering."
            )
    }

    fun <RenderingT : Modal> updateDialog(dialogRef: DialogRef<RenderingT>) {
        @Suppress("UNCHECKED_CAST")
        (bindings[dialogRef.modalRendering::class] as? DialogBinding<RenderingT>)
            ?.updateDialog(dialogRef)
    }

    operator fun <RenderingT : Modal> plus(binding: DialogBinding<RenderingT>): DialogRegistry {
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
            @StyleRes dialogThemeResId: Int = 0,
            @ColorInt fullScreenBackgroundColor: Int = Color.RED
        ): DialogRegistry {
            return DialogRegistry(
                AlertScreenDialogBinding(dialogThemeResId = dialogThemeResId),
                ViewDialogBinding(dialogThemeResId = dialogThemeResId),
                AlertWithViewDialogBinding(dialogThemeResId = dialogThemeResId),
                FullScreenDialogBinding(fullScreenBackgroundColor)
            )
        }
    }
}