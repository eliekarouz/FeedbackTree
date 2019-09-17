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
package com.feedbacktree.flow.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import kotlin.reflect.KClass

/**
 * Factory for [View] instances that can show renderings of type[RenderingT].
 * Use [LayoutRunner.bind] to work with XML layout resources, or
 * [BuilderBinding] to create views from code.
 *
 * Sets of bindings are gathered in [ViewRegistry] instances.
 */
interface ViewBinding<RenderingT : Any> {
    val type: KClass<RenderingT>

    /**
     * Returns a View ready to display [initialRendering] (and any succeeding values)
     * via [View.showRendering].
     */
    fun buildView(
        registry: ViewRegistry,
        initialRendering: RenderingT,
        contextForNewView: Context,
        container: ViewGroup? = null
    ): View
}
