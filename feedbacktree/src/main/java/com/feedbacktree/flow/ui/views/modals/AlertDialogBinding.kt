package com.feedbacktree.flow.ui.views.modals

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import com.feedbacktree.flow.ui.core.modals.AlertModal
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.utils.logAndShow
import com.zippyyum.subtemp.signinviews.feedbacktree.alert.DialogBinding
import kotlin.reflect.KClass

class AlertScreenDialogBinding(
    @StyleRes private val dialogThemeResId: Int = 0,
    override val type: KClass<AlertModal> = AlertModal::class
) : Modal,
    DialogBinding<AlertModal> {

    override fun buildDialog(
        initialRenderingT: AlertModal,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<AlertModal> {
        val dialog = AlertDialog.Builder(context, dialogThemeResId)
            .create()
        val ref = DialogRef(initialRenderingT, dialog)
        updateDialog(ref)
        dialog.logAndShow("AlertModal")
        return ref
    }

    override fun updateDialog(dialogRef: DialogRef<AlertModal>) {
        val dialog = dialogRef.dialog as AlertDialog
        val rendering = dialogRef.modalRendering

        if (rendering.cancelable) {
            dialog.setOnCancelListener { rendering.onEvent(AlertModal.Event.Canceled) }
            dialog.setCancelable(true)
        } else {
            dialog.setCancelable(false)
        }

        for (button in AlertModal.Button.values()) {
            rendering.buttons[button]
                ?.let { name ->
                    dialog.setButton(button.toId(), name) { _, _ ->
                        rendering.onEvent(
                            AlertModal.Event.ButtonClicked(
                                button
                            )
                        )
                    }
                }
                ?: run {
                    dialog.getButton(button.toId())
                        ?.visibility = View.INVISIBLE
                }
        }

        if (rendering.message.isNotEmpty()) {
            dialog.setMessage(rendering.message)
        }

        if (rendering.title.isNotEmpty()) {
            dialog.setTitle(rendering.title)
        }
    }

    private fun AlertModal.Button.toId(): Int = when (this) {
        AlertModal.Button.POSITIVE -> DialogInterface.BUTTON_POSITIVE
        AlertModal.Button.NEGATIVE -> DialogInterface.BUTTON_NEGATIVE
        AlertModal.Button.NEUTRAL -> DialogInterface.BUTTON_NEUTRAL
    }

}