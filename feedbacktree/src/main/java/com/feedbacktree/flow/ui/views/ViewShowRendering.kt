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

import android.view.View
import com.feedbacktree.R
import com.feedbacktree.flow.ui.core.compatible

/**
 * Function attached to a view created by [ViewRegistry], to allow it
 * to respond to [View.showRendering].
 */
typealias ViewShowRendering<RenderingT> = (@UnsafeVariance RenderingT) -> Unit

data class ShowRenderingTag<out RenderingT : Any>(
    val showing: RenderingT,
    val showRendering: ViewShowRendering<RenderingT>
)

/**
 * Establishes [showRendering] as the implementation of [View.showRendering]
 * for the receiver, possibly replacing the existing one. Calls [showRendering]
 * to display [initialRendering].
 *
 * Intended for use by implementations of [ViewBinding.buildView].
 */
fun <RenderingT : Any> View.bindShowRendering(
    initialRendering: RenderingT,
    showRendering: ViewShowRendering<RenderingT>
) {
    setTag(
        R.id.view_show_rendering_function,
        ShowRenderingTag(initialRendering, showRendering)
    )
    showRendering.invoke(initialRendering)
}

/**
 * True if this view is able to show [rendering].
 *
 * Returns `false` if [bindShowRendering] has not been called, so it is always safe to
 * call this method. Otherwise returns the [compatibility][compatible] of the new
 * [rendering] and the current one.
 */
fun View.canShowRendering(rendering: Any): Boolean {
    return showRenderingTag?.showing?.matches(rendering) == true
}

/**
 * Shows [rendering] in a view that has been initialized by [bindShowRendering].
 */
fun <RenderingT : Any> View.showRendering(rendering: RenderingT) {
    showRenderingTag
        ?.let { tag ->
            check(tag.showing.matches(rendering)) {
                "Expected $this to be able to update of ${tag.showing} from $rendering"
            }

            @Suppress("UNCHECKED_CAST")
            (tag.showRendering as ViewShowRendering<RenderingT>).invoke(rendering)
        }
        ?: error("Expected $this to have a showRendering function for $rendering.")
}

/**
 * Returns the [ShowRenderingTag] established by the last call to [View.bindShowRendering],
 * or null if none has been set.
 */
val View.showRenderingTag: ShowRenderingTag<*>?
    get() = getTag(R.id.view_show_rendering_function) as? ShowRenderingTag<*>

private fun Any.matches(other: Any) = compatible(this, other)
