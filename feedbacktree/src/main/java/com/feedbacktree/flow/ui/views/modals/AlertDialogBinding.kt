/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

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
import kotlin.reflect.KClass

class AlertScreenDialogBinding(
    @StyleRes private val dialogThemeResId: Int = 0,
    override val type: KClass<AlertModal> = AlertModal::class
) : Modal,
    DialogBinding<AlertModal> {

    override fun buildDialog(
        initialModal: AlertModal,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<AlertModal> {
        val dialog = AlertDialog.Builder(context, dialogThemeResId)
            .create()
        val ref = DialogRef(initialModal, dialog)
        updateDialog(ref)
        dialog.logAndShow("AlertModal")
        return ref
    }

    override fun updateDialog(dialogRef: DialogRef<AlertModal>) {
        val dialog = dialogRef.dialog as AlertDialog
        val modal = dialogRef.modal

        if (modal.cancelable) {
            dialog.setOnCancelListener { modal.onEvent(AlertModal.Event.Canceled) }
            dialog.setCancelable(true)
        } else {
            dialog.setCancelable(false)
        }

        for (button in AlertModal.Button.values()) {
            modal.buttons[button]
                ?.let { name ->
                    dialog.setButton(button.toId(), name) { _, _ ->
                        modal.onEvent(
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

        if (modal.message.isNotEmpty()) {
            dialog.setMessage(modal.message)
        }

        if (modal.title.isNotEmpty()) {
            dialog.setTitle(modal.title)
        }
    }

    private fun AlertModal.Button.toId(): Int = when (this) {
        AlertModal.Button.POSITIVE -> DialogInterface.BUTTON_POSITIVE
        AlertModal.Button.NEGATIVE -> DialogInterface.BUTTON_NEGATIVE
        AlertModal.Button.NEUTRAL -> DialogInterface.BUTTON_NEUTRAL
    }

}