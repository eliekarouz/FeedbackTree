/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.core.modals

class FullscreenModal<ContentViewModelT : Any>(val contentViewModel: ContentViewModelT) : Modal

fun <ContentViewModelT : Any> ContentViewModelT.asFullScreenModal(): FullscreenModal<ContentViewModelT> {
    return FullscreenModal(this)
}