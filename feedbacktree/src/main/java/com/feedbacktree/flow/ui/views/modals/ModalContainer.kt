/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views.modals

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.feedbacktree.flow.ui.core.compatible
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.core.modals.ModalContainerScreen
import com.feedbacktree.flow.ui.views.core.*

/**
 * Containers used to display [Modal]s.
 *
 * The [DialogRegistry] is passed to the [ModalContainer] in order to specify how to create and update the dialog views.
 *
 */
class ModalContainer
@JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet), HandlesBack {

    private lateinit var dialogRegistry: DialogRegistry

    internal constructor(context: Context, dialogRegistry: DialogRegistry) : this(context, null) {
        this.dialogRegistry = dialogRegistry
    }

    private val base: View? get() = getChildAt(0)

    private var dialogs: List<DialogRef<Modal>> = emptyList()

    private lateinit var registry: ViewRegistry

    override fun onBackPressed(): Boolean {
        // This should only be hit if there are no modals showing, so we only need to consider the body.
        return base?.let { HandlesBack.Helper.onBackPressed(it) } == true
    }

    private fun update(newScreen: ModalContainerScreen<*, *>) {
        base?.takeIf { it.canShowViewModel(newScreen.baseScreen) }
            ?.showViewModel(newScreen.baseScreen)
            ?: run {
                base?.cleanupViewModel()
                removeAllViews()
                val newBase = registry.buildView(newScreen.baseScreen, this)
                addView(newBase)
            }

        val newDialogs = mutableListOf<DialogRef<Modal>>()
        for ((i, modal) in newScreen.modals.withIndex()) {
            newDialogs += if (i < dialogs.size && compatible(
                    dialogs[i].modal,
                    modal
                )
            ) {
                dialogs[i].copy(modal = modal)
                    .also { dialogRegistry.updateDialog(it) }
            } else {
                dialogRegistry.buildDialog(modal, context, registry).apply {
                    // Android makes a lot of logcat noise if it has to close the window for us. :/
                    // https://github.com/square/workflow/issues/51
                    dialog.lifecycleOrNull()
                        ?.addObserver(OnDestroy { dialog.dismiss() })
                }
            }
        }

        (dialogs - newDialogs).forEach {
            dialogRegistry.cleanUp(it)
            it.dialog.dismiss()
        }
        dialogs = newDialogs
    }

    private fun cleanUp() {
        base?.cleanupViewModel()
        dialogs.forEach {
            dialogRegistry.cleanUp(it)
            it.dialog.dismiss()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(
            super.onSaveInstanceState()!!,
            dialogs.map { it.save() }
        )
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        (state as? SavedState)
            ?.let {
                if (it.dialogBundles.size == dialogs.size) {
                    it.dialogBundles.zip(dialogs) { viewState, dialogRef ->
                        dialogRef.restore(
                            viewState
                        )
                    }
                }
                super.onRestoreInstanceState(state.superState)
            }
            ?: super.onRestoreInstanceState(state)
    }

    class Binding(
        dialogRegistry: DialogRegistry
    ) : ViewBinding<ModalContainerScreen<*, *>>
    by BuilderBinding(
        type = ModalContainerScreen::class,
        viewConstructor = { viewRegistry, initialViewModel, contextForNewView, container ->
            ModalContainer(
                contextForNewView,
                dialogRegistry
            ).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                registry = viewRegistry
                bindShowViewModel(initialViewModel, ::update, ::cleanUp)
            }
        }
    )
}

private class OnDestroy(private val block: () -> Unit) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() = block()
}

private fun Dialog.lifecycleOrNull(): Lifecycle? = decorView?.context?.lifecycleOrNull()

private val Dialog.decorView: View?
    get() = window?.decorView

/**
 * The [Lifecycle] for this context, or null if one can't be found.
 *
 * We keep all this very forgiving because we're just using it to keep some logging
 * noise out of logcat. If someone manages to run this under a strange context whose
 * [Lifecycle] we can't find, just return null and let the caller no-op.
 */
private tailrec fun Context.lifecycleOrNull(): Lifecycle? = when {
    this is LifecycleOwner -> this.lifecycle
    else -> (this as? ContextWrapper)?.baseContext?.lifecycleOrNull()
}
