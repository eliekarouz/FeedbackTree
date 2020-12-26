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
package com.feedbacktree.flow.ui.views.core

import android.view.View
import com.feedbacktree.R
import com.feedbacktree.flow.ui.core.compatible

/**
 * Function attached to a view created by [ViewRegistry], to allow it
 * to respond to [View.showScreen].
 */
typealias ShowScreen<ScreenT> = (@UnsafeVariance ScreenT) -> Unit

/**
 *  Function attached to aview created by [ViewRegistry], to allow cleanup of resources
 *  when the view is removed from the window hierarchy.
 */
typealias DisposeScreenBinding = () -> Unit


data class ShowScreenTag<out ScreenT : Any>(
    val showing: ScreenT,
    val showScreen: ShowScreen<ScreenT>
)

/**
 * Establishes [showScreen] as the implementation of [View.showScreen]
 * for the receiver, possibly replacing the existing one. Calls [showScreen]
 * to display [initialScreen].
 *
 * Intended for use by implementations of [ViewBinding.buildView].
 */
fun <ScreenT : Any> View.bindShowScreen(
    initialScreen: ScreenT,
    showScreen: ShowScreen<ScreenT>,
    disposeScreenBinding: DisposeScreenBinding? = null
) {
    setTag(
        R.id.view_show_rendering_function,
        ShowScreenTag(initialScreen, showScreen)
    )
    if (disposeScreenBinding != null) {
        setTag(
            R.id.view_cleanup_rendering_function,
            disposeScreenBinding
        )
    }

    showScreen.invoke(initialScreen)
}

/**
 * True if this view is able to show [screen].
 *
 * Returns `false` if [bindShowScreen] has not been called, so it is always safe to
 * call this method. Otherwise returns the [compatibility][compatible] of the new
 * [screen] and the current one.
 */
fun View.canShowScreen(screen: Any): Boolean {
    return showScreenTag?.showing?.matches(screen) == true
}

/**
 * Shows [screen] in a view that has been initialized by [bindShowScreen].
 */
fun <ScreenT : Any> View.showScreen(screen: ScreenT) {
    showScreenTag
        ?.let { tag ->
            check(tag.showing.matches(screen)) {
                "Expected $this to be able to update of ${tag.showing} from $screen"
            }

            @Suppress("UNCHECKED_CAST")
            (tag.showScreen as ShowScreen<ScreenT>).invoke(screen)
        }
        ?: error("Expected $this to have a showViewModel function for $screen.")
}

/**
 * Invokes cleanup code installed by [bindShowScreen]
 */
fun View.disposeScreenBinding() {
    disposeScreenBindingTag?.invoke()
}

/**
 * Returns the [ShowScreenTag] established by the last call to [View.bindShowScreen],
 * or null if none has been set.
 */
val View.showScreenTag: ShowScreenTag<*>?
    get() = getTag(R.id.view_show_rendering_function) as? ShowScreenTag<*>


/**
 * Returns the [DisposeScreenBinding] established by the last call to [View.bindShowScreen],
 * or null if none has been set.
 */
@Suppress("UNCHECKED_CAST")
val View.disposeScreenBindingTag: DisposeScreenBinding?
    get() = getTag(R.id.view_cleanup_rendering_function) as? DisposeScreenBinding


private fun Any.matches(other: Any) = compatible(this, other)
