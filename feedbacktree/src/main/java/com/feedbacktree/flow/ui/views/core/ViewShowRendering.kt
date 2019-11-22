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
 * to respond to [View.showViewModel].
 */
typealias ShowViewModel<ViewModelT> = (@UnsafeVariance ViewModelT) -> Unit

data class ShowViewModelTag<out ViewModelT : Any>(
    val showing: ViewModelT,
    val showViewModel: ShowViewModel<ViewModelT>
)

/**
 * Establishes [showViewModel] as the implementation of [View.showViewModel]
 * for the receiver, possibly replacing the existing one. Calls [showViewModel]
 * to display [initialViewModel].
 *
 * Intended for use by implementations of [ViewBinding.buildView].
 */
fun <ViewModelT : Any> View.bindShowViewModel(
    initialViewModel: ViewModelT,
    showViewModel: ShowViewModel<ViewModelT>
) {
    setTag(
        R.id.view_show_rendering_function,
        ShowViewModelTag(initialViewModel, showViewModel)
    )
    showViewModel.invoke(initialViewModel)
}

/**
 * True if this view is able to show [viewModel].
 *
 * Returns `false` if [bindShowViewModel] has not been called, so it is always safe to
 * call this method. Otherwise returns the [compatibility][compatible] of the new
 * [viewModel] and the current one.
 */
fun View.canShowViewModel(viewModel: Any): Boolean {
    return showViewModelTag?.showing?.matches(viewModel) == true
}

/**
 * Shows [viewModel] in a view that has been initialized by [bindShowViewModel].
 */
fun <ViewModelT : Any> View.showViewModel(viewModel: ViewModelT) {
    showViewModelTag
        ?.let { tag ->
            check(tag.showing.matches(viewModel)) {
                "Expected $this to be able to update of ${tag.showing} from $viewModel"
            }

            @Suppress("UNCHECKED_CAST")
            (tag.showViewModel as ShowViewModel<ViewModelT>).invoke(viewModel)
        }
        ?: error("Expected $this to have a showViewModel function for $viewModel.")
}

/**
 * Returns the [ShowViewModelTag] established by the last call to [View.bindShowViewModel],
 * or null if none has been set.
 */
val View.showViewModelTag: ShowViewModelTag<*>?
    get() = getTag(R.id.view_show_rendering_function) as? ShowViewModelTag<*>

private fun Any.matches(other: Any) = compatible(this, other)
