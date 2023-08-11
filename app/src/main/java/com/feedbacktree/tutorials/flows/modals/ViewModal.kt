/*
 * Created by eliek on 8/9/2023
 * Copyright (c) 2023 eliekarouz. All rights reserved.
 */

@file:Suppress("FunctionName")

package com.feedbacktree.tutorials.flows.modals

import androidx.annotation.ColorInt
import com.feedbacktree.flow.ui.core.Compatible
import com.feedbacktree.flow.ui.core.modals.Modal

class ViewModal<ScreenT : Any>(
    val content: ScreenT,
    val widthLayout: Layout,
    val heightLayout: Layout,
    @ColorInt val backgroundColor: Int? = null,
    val roundCorners: Boolean? = null
) : Modal, Compatible {
    constructor(
        content: ScreenT,
        roundCorners: Boolean?,
    ) : this(
        content,
        widthLayout = Layout.Wrap,
        heightLayout = Layout.Wrap,
        roundCorners = roundCorners
    )

    override val compatibilityKey: String
        get() = content::class.java.name
}

sealed class Layout {
    object Wrap : Layout()
    object FullScreen : Layout()
    data class DPs(val dps: Int) : Layout()
    data class Percentage(val percentage: Int) : Layout() {
        init {
            check(percentage in 0..100) { "Percentage value must be between 0 and 100" }
        }
    }
}

fun <ScreenT : Any> ScreenT.asModal(roundCorners: Boolean? = null): ViewModal<ScreenT> {
    return ViewModal(content = this, roundCorners = roundCorners)
}


/**
 * Wrap you screen in a [FullScreenModal] to show it as if you have hosted it in a new activity.
 * Use cases for a [FullScreenModal] is when you have a split screen or even a bottom navigation view
 * and you want the [content] to cover the whole screen even though the rendering is being done in a
 * more restricted area.
 */
fun <ScreenT : Any> FullScreenModal(content: ScreenT): ViewModal<ScreenT> {
    return ViewModal(
        content = content,
        widthLayout = Layout.FullScreen,
        heightLayout = Layout.FullScreen,
    )
}

fun <ScreenT : Any> ScreenT.asFullScreenModal(): ViewModal<ScreenT> {
    return FullScreenModal(this)
}