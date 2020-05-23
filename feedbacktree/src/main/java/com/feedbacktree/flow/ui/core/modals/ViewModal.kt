/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.core.modals

class ViewModal<ContentViewModelT : Any>(
    val content: ContentViewModelT,
    val widthLayout: Layout,
    val heightLayout: Layout
) : Modal {
    constructor(content: ContentViewModelT) : this(
        content,
        widthLayout = Layout.Wrap,
        heightLayout = Layout.Wrap
    )
}

sealed class Layout {
    object Wrap : Layout()
    object FullScreen : Layout()
    data class Pixels(val pixels: Int) : Layout()
    data class Percentage(val percentage: Int) : Layout() {
        init {
            check(percentage in 0..100) { "Percentage value must be between 0 and 100" }
        }
    }
}

fun <ContentViewModelT : Any> ContentViewModelT.asModal(): ViewModal<ContentViewModelT> {
    return ViewModal(content = this)
}

@Suppress("FunctionName")
fun <ContentViewModelT : Any> FullScreenModal(content: ContentViewModelT): ViewModal<ContentViewModelT> {
    return ViewModal(
        content = content,
        widthLayout = Layout.FullScreen,
        heightLayout = Layout.FullScreen
    )
}

fun <ContentViewModelT : Any> ContentViewModelT.asFullScreenModal(): ViewModal<ContentViewModelT> {
    return FullScreenModal(this)
}