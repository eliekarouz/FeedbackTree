/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.core.modals

data class ViewModal<out Rendering : Any>(
    val rendering: Rendering
) : Modal

fun <T : Any> T.asViewModal() = ViewModal(this)