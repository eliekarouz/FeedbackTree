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
import com.feedbacktree.flow.ui.core.modals.AlertModalWithView
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.showRendering
import com.feedbacktree.flow.utils.logAndShow
import kotlin.reflect.KClass

class AlertWithViewDialogBinding(
    @StyleRes private val dialogThemeResId: Int = 0,
    override val type: KClass<AlertModalWithView<*>> = AlertModalWithView::class
) :
    DialogBinding<AlertModalWithView<*>> {

    override fun buildDialog(
        initialRenderingT: AlertModalWithView<*>,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<AlertModalWithView<*>> {
        val contentView = viewRegistry.buildView(initialRenderingT.contentScreen, context)
        val dialog = AlertDialog.Builder(context, dialogThemeResId)
            .setView(contentView)
            .create()
        dialog.logAndShow("AlertModalWithView")
        val ref = DialogRef(initialRenderingT, dialog, contentView)
        updateDialog(ref)
        return ref
    }

    override fun updateDialog(dialogRef: DialogRef<AlertModalWithView<*>>) {
        val dialog = dialogRef.dialog as AlertDialog
        val alertScreenRendering = dialogRef.modalRendering.alertModal

        if (alertScreenRendering.cancelable) {
            dialog.setOnCancelListener { alertScreenRendering.onEvent(AlertModal.Event.Canceled) }
            dialog.setCancelable(true)
        } else {
            dialog.setCancelable(false)
        }

        for (button in AlertModal.Button.values()) {
            alertScreenRendering.buttons[button]
                ?.let { name ->
                    dialog.setButton(button.toId(), name) { _, _ ->
                        alertScreenRendering.onEvent(
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

        dialog.setMessage(alertScreenRendering.message)
        dialog.setTitle(alertScreenRendering.title)

        contentViewUpdate(dialogRef)
    }

    private fun contentViewUpdate(dialogRef: DialogRef<AlertModalWithView<*>>) {
        (dialogRef.extra as View).showRendering(dialogRef.modalRendering.contentScreen)
    }

    private fun AlertModal.Button.toId(): Int = when (this) {
        AlertModal.Button.POSITIVE -> DialogInterface.BUTTON_POSITIVE
        AlertModal.Button.NEGATIVE -> DialogInterface.BUTTON_NEGATIVE
        AlertModal.Button.NEUTRAL -> DialogInterface.BUTTON_NEUTRAL
    }
}