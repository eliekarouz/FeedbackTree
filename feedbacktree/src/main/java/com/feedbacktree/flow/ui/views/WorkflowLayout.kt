/*
 * Copyright 2019 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedbacktree.flow.ui.views

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.FrameLayout
import com.feedbacktree.flow.ui.views.core.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * In most cases, if not all, you will be using [com.feedbacktree.flow.core.startFlow] or subclass [FlowFragment]
 * rather than manage this class directly.
 *
 * [WorkflowLayout] is the root [View] container that will be responsible of rendering the screens produced by
 * the root [Flow].
 */
class WorkflowLayout(
    context: Context,
    attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet), HandlesBack {
    private var restoredChildState: SparseArray<Parcelable>? = null
    private val showing: View? get() = if (childCount > 0) getChildAt(0) else null

    /**
     * Subscribes to [screens], and uses [registry] to
     * [build a new view][ViewRegistry.buildView] each time a new type of viewModel is received,
     * making that view the only child of this one.
     *
     * Views created this way may make recursive calls to [ViewRegistry.buildView] to make
     * children of their own to handle nested screens.
     */
    fun start(
        screens: Observable<out Any>,
        registry: ViewRegistry
    ) {
        takeWhileAttached(screens) { show(it, registry) }
    }

    override fun onBackPressed(): Boolean {
        return showing
            ?.let { HandlesBack.Helper.onBackPressed(it) }
            ?: false
    }

    private fun show(
        newViewModel: Any,
        registry: ViewRegistry
    ) {
        showing?.takeIf { it.canShowScreen(newViewModel) }
            ?.let { it ->
                it.showScreen(newViewModel)
                return
            }

        showing?.disposeScreenBinding()
        removeAllViews()
        val newView = registry.buildView(newViewModel, this)
        restoredChildState?.let { restoredState ->
            restoredChildState = null
            newView.restoreHierarchyState(restoredState)
        }
        addView(newView)
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(
            super.onSaveInstanceState()!!,
            SparseArray<Parcelable>().also { array -> showing?.saveHierarchyState(array) }
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? SavedState)
            ?.let {
                restoredChildState = it.childState
                super.onRestoreInstanceState(state.superState)
            }
            ?: super.onRestoreInstanceState(state)
    }

    private class SavedState : BaseSavedState {
        constructor(
            superState: Parcelable?,
            childState: SparseArray<Parcelable>
        ) : super(superState) {
            this.childState = childState
        }

        constructor(source: Parcel) : super(source) {
            this.childState =
                source.readSparseArray<Parcelable>(SavedState::class.java.classLoader)!!
        }

        val childState: SparseArray<Parcelable>

        override fun writeToParcel(
            out: Parcel,
            flags: Int
        ) {
            super.writeToParcel(out, flags)
            @Suppress("UNCHECKED_CAST")
            out.writeSparseArray(childState as SparseArray<Any>)
        }

        companion object CREATOR : Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState =
                SavedState(source)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    /**
     * Subscribes [update] to [source] only while this [View] is attached to a window.
     */
    private fun <S : Any> View.takeWhileAttached(
        source: Observable<S>,
        update: (S) -> Unit
    ) {
        var sub: Disposable? = null
        isAttachedToWindowBinder { isAttached ->
            println("WorkflowLayout is Attached to window $isAttached")
            if (isAttached) {
                sub = source.subscribe { screen -> update(screen) }
            } else {
                sub?.let {
                    showing?.disposeScreenBinding()
                    it.dispose()
                    sub = null
                }
            }
        }
    }

    // callback is called with initial state and on subsequent updates
    private fun View.isAttachedToWindowBinder(
        onAttached: (Boolean) -> Unit
    ) {
        addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                onAttached(true)
            }

            override fun onViewDetachedFromWindow(v: View) {
                onAttached(false)
            }
        })
        onAttached(isAttachedToWindow)
    }
}
