package com.feedbacktree.flow.ui.views

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.feedbacktree.flow.ui.core.compatible
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.modals.DialogRef
import com.feedbacktree.flow.ui.views.modals.DialogRegistry
import com.feedbacktree.flow.utils.logVerbose

internal class DialogFlowRenderer(
    private val context: Context,
    private val registry: ViewRegistry,
    private val dialogRegistry: DialogRegistry
) {

    private var dialogs: List<DialogRef<Modal>> = listOf()

    fun update(modals: List<Modal>) {
        logVerbose("FT Modals: Render modals: $modals")
        val newDialogs = mutableListOf<DialogRef<Modal>>()
        for ((i, modal) in modals.withIndex()) {
            newDialogs += if (i < dialogs.size && compatible(
                    dialogs[i].modal,
                    modal
                )
            ) {
                logVerbose("FT Modals Update Modal $modal")
                dialogs[i].copy(modal = modal)
                    .also { dialogRegistry.updateDialog(it) }
            } else {
                logVerbose("FT Modals Build Modal $modal")
                dialogRegistry.buildDialog(modal, context, registry).apply {
                    // Android makes a lot of logcat noise if it has to close the window for us. :/
                    // https://github.com/square/workflow/issues/51
                    dialog.lifecycleOrNull()
                        ?.addObserver(OnDestroy { dialog.dismiss() })
                }
            }
        }

        (dialogs - newDialogs).forEach {
            logVerbose("FT Modals Dismiss ${it.modal}")
            dialogRegistry.cleanUp(it)
            it.dialog.dismiss()
        }
        dialogs = newDialogs
    }
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