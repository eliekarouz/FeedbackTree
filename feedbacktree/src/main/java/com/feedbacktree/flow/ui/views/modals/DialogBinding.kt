package com.zippyyum.subtemp.signinviews.feedbacktree.alert

import android.content.Context
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.modals.DialogRef
import com.feedbacktree.flow.ui.views.modals.DialogRegistry
import kotlin.reflect.KClass

interface DialogBinding<RenderingT : Modal> {

    val type: KClass<RenderingT>

    fun buildDialog(
        initialRenderingT: RenderingT,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<RenderingT>

    fun updateDialog(dialogRef: DialogRef<RenderingT>)
}