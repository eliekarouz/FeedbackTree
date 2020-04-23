/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views.modals

import android.app.Dialog
import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.StyleRes
import com.feedbacktree.flow.ui.core.modals.ViewModal
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.cleanupViewModel
import com.feedbacktree.flow.ui.views.core.showViewModel
import com.feedbacktree.flow.utils.logAndShow
import kotlin.reflect.KClass


class ViewDialogBinding(
    override val type: KClass<ViewModal<*>> = ViewModal::class,
    @StyleRes private val dialogThemeResId: Int = 0
) : DialogBinding<ViewModal<Any>> {

    override fun buildDialog(
        initialModal: ViewModal<*>,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<ViewModal<*>> {
        val viewModel = initialModal.viewModel
        val view = viewRegistry.buildView(viewModel, context)
        return Dialog(context, dialogThemeResId)
            .apply {
                setOnKeyListener { _, keyCode, keyEvent ->
                    if (keyEvent.action != KeyEvent.ACTION_DOWN) {
                        true
                    } else {
                        keyCode == KeyEvent.KEYCODE_BACK && HandlesBack.Helper.onBackPressed(
                            view
                        )
                    }
                }
                window!!.setLayout(WRAP_CONTENT, WRAP_CONTENT)
                logAndShow("ViewModal")
                setCancelable(false)
                setContentView(view)
            }
            .run {
                DialogRef(
                    initialModal,
                    this,
                    view
                )
            }
    }

    override fun updateDialog(dialogRef: DialogRef<ViewModal<*>>) {
        with(dialogRef) { (extra as View).showViewModel(modal.viewModel) }
    }

    override fun cleanUpDialog(dialogRef: DialogRef<ViewModal<*>>) {
        super.cleanUpDialog(dialogRef)
        (dialogRef.extra as View).cleanupViewModel()
    }
}