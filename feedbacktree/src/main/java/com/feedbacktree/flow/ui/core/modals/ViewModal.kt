/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.core.modals

data class ViewModal<out ViewModelT : Any>(
    val viewModel: ViewModelT
) : Modal

fun <ViewModelT : Any> ViewModelT.asViewModal() = ViewModal(this)