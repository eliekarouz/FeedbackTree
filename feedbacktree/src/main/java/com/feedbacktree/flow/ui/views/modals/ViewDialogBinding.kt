package com.feedbacktree.flow.ui.views.modals

import android.app.Dialog
import android.content.Context
import android.view.KeyEvent
import android.view.View
import androidx.annotation.StyleRes
import com.feedbacktree.flow.ui.core.modals.ViewModal
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.showRendering
import com.zippyyum.subtemp.signinviews.feedbacktree.alert.DialogBinding
import kotlin.reflect.KClass


class ViewDialogBinding(
    override val type: KClass<ViewModal<*>> = ViewModal::class,
    @StyleRes private val dialogThemeResId: Int = 0
) : DialogBinding<ViewModal<Any>> {

    override fun buildDialog(
        initialRenderingT: ViewModal<*>,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<ViewModal<*>> {
        val viewRendering = initialRenderingT.rendering
        val view = viewRegistry.buildView(viewRendering, context)
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
                setCancelable(false)
                setContentView(view)
            }
            .run {
                DialogRef(
                    initialRenderingT,
                    this,
                    view
                )
            }
    }

    override fun updateDialog(dialogRef: DialogRef<ViewModal<*>>) {
        with(dialogRef) { (extra as View).showRendering(modalRendering.rendering) }
    }

}