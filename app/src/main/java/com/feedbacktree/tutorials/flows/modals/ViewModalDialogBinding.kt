/*
 * Created by eliek on 8/11/2023
 * Copyright (c) 2023 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.modals

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Display
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.feedbacktree.R
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.disposeScreenBinding
import com.feedbacktree.flow.ui.views.core.showScreen
import com.feedbacktree.flow.ui.views.modals.DialogBinding
import com.feedbacktree.flow.ui.views.modals.DialogRef
import com.feedbacktree.flow.ui.views.modals.DialogRegistry
import com.feedbacktree.flow.utils.windowManager
import kotlin.reflect.KClass


class ViewModalDialogBinding(
    override val type: KClass<ViewModal<*>> = ViewModal::class,
) : DialogBinding<ViewModal<*>> {

    override fun buildDialog(
        initialModal: ViewModal<*>,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<ViewModal<*>> {

        return Dialog(context, R.style.PanelDialog)
            .run {
                setCancelable(false)
                val view = addFullScreenView(viewRegistry, initialModal, context)
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
        viewModal: ViewModal<ScreenT>,
        context: Context
    ): View {
        // used to position the custom view when the not in full screen mode
        val fullWindowPanel = FrameLayout(context).apply {
            ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            setBackgroundColor(Color.TRANSPARENT)
        }

        setContentView(fullWindowPanel)
        show()

        window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.dim)))


        // panel that wraps the custom view with the background color needed
        val contentPanel = FrameLayout(context)

        // Setting backgroundColor of the panel ideally with using the windowBackground
        val panelBackgroundColor = if (viewModal.backgroundColor != null) {
            viewModal.backgroundColor
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
            if (typedValue.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT) {
                typedValue.data
            } else null
        }

        if (viewModal.roundCorners ?: !viewModal.hasFullScreenDimension()) {
            contentPanel.background = GradientDrawable().apply {
                panelBackgroundColor?.also { color = ColorStateList.valueOf(it) }
                cornerRadius = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    20f,
                    Resources.getSystem().displayMetrics
                )
            }
            contentPanel.clipToOutline = true
        } else if (panelBackgroundColor != null) {
            contentPanel.setBackgroundColor(panelBackgroundColor)
        }

        // Adjusting the panel size
        val screenSize = context.screenSize
        val scale = context.resources.displayMetrics.density
        contentPanel.layoutParams = FrameLayout.LayoutParams(
            layoutParams(viewModal.widthLayout, screenSize.x, scale),
            layoutParams(viewModal.heightLayout, screenSize.y, scale)
        ).apply {
            gravity = Gravity.CENTER
        }
        fullWindowPanel.addView(contentPanel)
        // The view is created and added after Dialog.show is called to preserve of the dialogs as described by the
        // virtual DOM screens.
        val view = viewRegistry.buildView(viewModal.content, context)
        contentPanel.addView(view)
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

private fun <Screen : Any> ViewModal<Screen>.hasFullScreenDimension(): Boolean {
    return widthLayout == Layout.FullScreen ||
            heightLayout == Layout.FullScreen ||
            (widthLayout is Layout.Percentage && widthLayout.percentage == 100) ||
            (heightLayout is Layout.Percentage && heightLayout.percentage == 100)
}