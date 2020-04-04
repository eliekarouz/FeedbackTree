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
import com.feedbacktree.flow.ui.views.core.cleanupViewModel
import com.feedbacktree.flow.ui.views.core.showViewModel
import com.feedbacktree.flow.utils.logAndShow
import kotlin.reflect.KClass

class AlertWithViewDialogBinding(
    @StyleRes private val dialogThemeResId: Int = 0,
    override val type: KClass<AlertModalWithView<*>> = AlertModalWithView::class
) :
    DialogBinding<AlertModalWithView<*>> {

    override fun buildDialog(
        initialModal: AlertModalWithView<*>,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<AlertModalWithView<*>> {
        val contentView = viewRegistry.buildView(initialModal.contentViewModel, context)
        val dialog = AlertDialog.Builder(context, dialogThemeResId)
            .setView(contentView)
            .create()
        dialog.logAndShow("AlertModalWithView")
        val ref = DialogRef(initialModal, dialog, contentView)
        updateDialog(ref)
        return ref
    }

    override fun updateDialog(dialogRef: DialogRef<AlertModalWithView<*>>) {
        val dialog = dialogRef.dialog as AlertDialog
        val alertModal = dialogRef.modal.alertModal

        if (alertModal.cancelable) {
            dialog.setOnCancelListener { alertModal.onEvent(AlertModal.Event.Canceled) }
            dialog.setCancelable(true)
        } else {
            dialog.setCancelable(false)
        }

        for (button in AlertModal.Button.values()) {
            alertModal.buttons[button]
                ?.let { name ->
                    dialog.setButton(button.toId(), name) { _, _ ->
                        alertModal.onEvent(
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

        dialog.setMessage(alertModal.message)
        dialog.setTitle(alertModal.title)

        contentViewUpdate(dialogRef)
    }

    override fun cleanUpDialog(dialogRef: DialogRef<AlertModalWithView<*>>) {
        super.cleanUpDialog(dialogRef)
        (dialogRef.extra as View).cleanupViewModel()
    }
    
    private fun contentViewUpdate(dialogRef: DialogRef<AlertModalWithView<*>>) {
        (dialogRef.extra as View).showViewModel(dialogRef.modal.contentViewModel)
    }


    private fun AlertModal.Button.toId(): Int = when (this) {
        AlertModal.Button.POSITIVE -> DialogInterface.BUTTON_POSITIVE
        AlertModal.Button.NEGATIVE -> DialogInterface.BUTTON_NEGATIVE
        AlertModal.Button.NEUTRAL -> DialogInterface.BUTTON_NEUTRAL
    }
}