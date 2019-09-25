package com.feedbacktree.flow.ui.core.modals

data class ViewModal<out Rendering : Any>(
    val rendering: Rendering
) : Modal

fun <T : Any> T.asViewModal() = ViewModal(this)