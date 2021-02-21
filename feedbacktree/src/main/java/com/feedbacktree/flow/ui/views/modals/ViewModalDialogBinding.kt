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
import android.view.*
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.feedbacktree.flow.ui.core.modals.Layout
import com.feedbacktree.flow.ui.core.modals.ViewModal
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.disposeScreenBinding
import com.feedbacktree.flow.ui.views.core.showScreen
import com.feedbacktree.flow.utils.logAndShow
import com.feedbacktree.flow.utils.windowManager
import kotlin.reflect.KClass


class ViewModalDialogBinding(
    @ColorInt private val backgroundColor: Int = Color.WHITE,
    override val type: KClass<ViewModal<*>> = ViewModal::class,
) : DialogBinding<ViewModal<*>> {

    override fun buildDialog(
        initialModal: ViewModal<*>,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<ViewModal<*>> {

        return Dialog(context, 0)
            .run {
                setCancelable(false)
                val view =
                    if (initialModal.widthLayout == Layout.Wrap && initialModal.heightLayout == Layout.Wrap) {
                        addWrapContentView(viewRegistry, initialModal)
                    } else {
                        addFullScreenView(viewRegistry, initialModal)
                    }


                setOnKeyListener { _, keyCode, keyEvent ->
                    if (keyEvent.action != KeyEvent.ACTION_DOWN) {
                        true
                    } else {
                        keyCode == KeyEvent.KEYCODE_BACK && HandlesBack.Helper.onBackPressed(view)
                    }
                }
                DialogRef(
                    initialModal,
                    this,
                    view
                )
            }
    }

    private fun <ScreenT : Any> Dialog.addFullScreenView(
        viewRegistry: ViewRegistry,
        viewModal: ViewModal<ScreenT>
    ): View {
        val view by lazy { viewRegistry.buildView(viewModal.content, context) }
        val panel = PanelBodyWrapper(context).apply {
            background = ColorDrawable(backgroundColor)
        }
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        setContentView(panel)
        window!!.setLayout(WRAP_CONTENT, WRAP_CONTENT)
        window!!.setBackgroundDrawable(null)
        logAndShow("FullScreen")

        val screenSize = context.screenSize
        val scale = context.resources.displayMetrics.density
        view.layoutParams = FrameLayout.LayoutParams(
            layoutParams(viewModal.widthLayout, screenSize.x, scale),
            layoutParams(viewModal.heightLayout, screenSize.y, scale)
        ).apply {
            gravity = Gravity.CENTER
        }
        panel.addView(view)
        return view
    }

    private fun <ScreenT : Any> Dialog.addWrapContentView(
        viewRegistry: ViewRegistry,
        viewModal: ViewModal<ScreenT>
    ): View {
        window!!.setLayout(WRAP_CONTENT, WRAP_CONTENT)
        logAndShow("ViewModal")
        val view = viewRegistry.buildView(viewModal.content, context)
        setContentView(view)
        return view
    }

    override fun updateDialog(dialogRef: DialogRef<ViewModal<*>>) {
        with(dialogRef) { (extra as View).showScreen(modal.content) }
    }

    override fun cleanUpDialog(dialogRef: DialogRef<ViewModal<*>>) {
        super.cleanUpDialog(dialogRef)
        with(dialogRef) { (extra as View).disposeScreenBinding() }
    }
}

internal class PanelBodyWrapper
@JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet) {

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val screenSize = context.screenSize
        val calculatedWidthSpec: Int = makeMeasureSpec(screenSize.x, EXACTLY)
        val calculatedHeightSpec: Int =
            makeMeasureSpec(screenSize.y, EXACTLY)
        super.onMeasure(calculatedWidthSpec, calculatedHeightSpec)
    }
}

private val Context.screenSize: Point
    get() {
        // Get the size between the status & the navigation bars

        // Get the size between the status & the navigation bars
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        // Real size of the screen (full screen)

        // Real size of the screen (full screen)
        val realSize = Point()
        val realDisplay: Display = windowManager.defaultDisplay
        realDisplay.getRealSize(realSize)

        // Get the screen dimensions

        fun getStatusBarSize(): Int {
            val statusBarId =
                resources.getIdentifier("status_bar_height", "dimen", "android")
            if (statusBarId > 0) {
                return resources.getDimensionPixelSize(statusBarId)
            }
            return 0
        }

        fun getNavigationBarHeight(): Int {
            val navigationBarId =
                resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (navigationBarId > 0) {
                return resources.getDimensionPixelSize(navigationBarId)
            }
            return 0
        }

        // Get the screen dimensions
        val maxWidth = size.x
        val maxHeight =
            if (realSize.y - getNavigationBarHeight() == size.y || realSize.y == size.y) {
                size.y - getStatusBarSize()
            } else size.y
        return Point(maxWidth, maxHeight)
    }

private fun layoutParams(layout: Layout, screenDimension: Int, scale: Float): Int {
    return when (layout) {
        Layout.Wrap -> WRAP_CONTENT
        Layout.FullScreen -> MATCH_PARENT
        is Layout.DPs -> (layout.dps * scale).toInt()
        is Layout.Percentage -> (screenDimension.toFloat() * layout.percentage.toFloat() * 0.01).toInt()
    }
}