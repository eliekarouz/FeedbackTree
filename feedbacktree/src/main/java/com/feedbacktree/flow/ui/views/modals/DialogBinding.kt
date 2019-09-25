/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views.modals

import android.content.Context
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.views.core.ViewRegistry
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