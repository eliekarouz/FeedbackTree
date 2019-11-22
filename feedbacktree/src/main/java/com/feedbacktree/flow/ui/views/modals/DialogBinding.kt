/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views.modals

import android.content.Context
import com.feedbacktree.flow.ui.core.modals.Modal
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import kotlin.reflect.KClass

interface DialogBinding<ModalT : Modal> {

    val type: KClass<ModalT>

    fun buildDialog(
        initialModal: ModalT,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<ModalT>

    fun updateDialog(dialogRef: DialogRef<ModalT>)
}