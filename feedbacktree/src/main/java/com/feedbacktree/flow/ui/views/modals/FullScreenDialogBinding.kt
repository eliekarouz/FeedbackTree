/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views.modals

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.feedbacktree.flow.ui.core.modals.FullScreenModal
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.flow.ui.views.core.showRendering
import com.feedbacktree.flow.utils.display
import com.feedbacktree.flow.utils.isTablet
import com.feedbacktree.flow.utils.logAndShow
import kotlin.math.min
import kotlin.reflect.KClass

class FullScreenDialogBinding(
    @ColorInt private val backgroundColor: Int,
    override val type: KClass<FullScreenModal<*>> = FullScreenModal::class
) : DialogBinding<FullScreenModal<*>> {

    override fun buildDialog(
        initialRenderingT: FullScreenModal<*>,
        dialogRegistry: DialogRegistry,
        viewRegistry: ViewRegistry,
        context: Context
    ): DialogRef<FullScreenModal<*>> {
        val viewRendering = initialRenderingT.screen

        val view by lazy { viewRegistry.buildView(viewRendering, context) }
        val panel = PanelBodyWrapper(context).apply {
            background = ColorDrawable(backgroundColor)
        }
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
                logAndShow("FullScreen")
                panel.addView(view)
            }
            .run {
                DialogRef(
                    initialRenderingT,
                    this,
                    view
                )
            }
    }

    override fun updateDialog(dialogRef: DialogRef<FullScreenModal<*>>) {
        with(dialogRef) { (extra as View).showRendering(modalRendering.screen) }
    }
}

internal class PanelBodyWrapper
@JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet) {


    /** For use only by [onMeasure]. Instantiated here to avoid allocation during measure. */
    private val displayMetrics = DisplayMetrics()

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        context.display.getMetrics(displayMetrics)
        val calculatedWidthSpec: Int
        val calculatedHeightSpec: Int

        if (context.isTablet) {
            val size = min(displayMetrics.widthPixels, displayMetrics.heightPixels)

            calculatedWidthSpec = makeMeasureSpec(size, EXACTLY)
            calculatedHeightSpec = makeMeasureSpec(size, EXACTLY)
        } else {
            calculatedWidthSpec = makeMeasureSpec(displayMetrics.widthPixels, EXACTLY)
            calculatedHeightSpec = makeMeasureSpec(displayMetrics.heightPixels, EXACTLY)
        }

        super.onMeasure(calculatedWidthSpec, calculatedHeightSpec)
    }
}