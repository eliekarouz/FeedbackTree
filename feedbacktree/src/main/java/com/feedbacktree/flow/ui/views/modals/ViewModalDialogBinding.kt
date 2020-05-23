/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views.modals

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.feedbacktree.flow.ui.core.modals.Layout
import com.feedbacktree.flow.ui.core.modals.ViewModal
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.cleanupViewModel
import com.feedbacktree.flow.ui.views.core.showViewModel
import com.feedbacktree.flow.utils.display
import com.feedbacktree.flow.utils.logAndShow
import kotlin.reflect.KClass

class ViewModalDialogBinding(
    override val type: KClass<ViewModal<*>> = ViewModal::class
) : DialogBinding<ViewModal<*>> {

    override fun buildDialog(
        initialModal: ViewModal<*>,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<ViewModal<*>> {
        val contentViewModel = initialModal.content

        val panel = PanelBodyWrapper(context).apply {
            background = ColorDrawable(Color.TRANSPARENT)
        }
        val view by lazy { viewRegistry.buildView(contentViewModel, context) }

        return Dialog(context, 0)
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
                setContentView(panel)
                window!!.setLayout(WRAP_CONTENT, WRAP_CONTENT)
                window!!.setBackgroundDrawable(null)
                val windowManager = window!!.windowManager
                logAndShow("FullScreen")
                val dimensions = Point()
                val statusBarHeight =
                    context.resources.getIdentifier("status_bar_height", "dimen", "android")
                        .takeIf { it > 0 }
                        ?.let { context.resources.getDimensionPixelSize(it) } ?: 0

                windowManager.defaultDisplay.getSize(dimensions)
                view.layoutParams = FrameLayout.LayoutParams(
                    layoutParams(initialModal.widthLayout, dimensions.x),
                    layoutParams(initialModal.heightLayout, dimensions.y - statusBarHeight)
                ).apply {
                    gravity = Gravity.CENTER
                }

                panel.addView(view)
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
        with(dialogRef) { (extra as View).showViewModel(modal.content) }
    }

    override fun cleanUpDialog(dialogRef: DialogRef<ViewModal<*>>) {
        super.cleanUpDialog(dialogRef)
        with(dialogRef) { (extra as View).cleanupViewModel() }
    }
}

internal class PanelBodyWrapper
@JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet) {


    /** For use only by [onMeasure]. Instantiated here to avoid allocation during measure. */
    private val displayMetrics = DisplayMetrics()
    private val statusBarId =
        resources.getIdentifier("status_bar_height", "dimen", "android").takeIf { it > 0 }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val statusBarHeight = statusBarId?.let {
            resources.getDimensionPixelSize(it)
        } ?: 0

        context.display.getMetrics(displayMetrics)
        val calculatedWidthSpec: Int = makeMeasureSpec(displayMetrics.widthPixels, EXACTLY)
        val calculatedHeightSpec: Int =
            makeMeasureSpec(displayMetrics.heightPixels - statusBarHeight, EXACTLY)
        super.onMeasure(calculatedWidthSpec, calculatedHeightSpec)
    }
}

private fun layoutParams(layout: Layout, screenDimension: Int): Int {
    return when (layout) {
        Layout.Wrap -> WRAP_CONTENT
        Layout.FullScreen -> MATCH_PARENT
        is Layout.Pixels -> layout.pixels
        is Layout.Percentage -> (screenDimension.toFloat() * layout.percentage.toFloat() * 0.01).toInt()
    }
}