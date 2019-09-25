/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.core.modals

class FullScreenModal<ContentScreen : Any>(val screen: ContentScreen) : Modal

fun <ContentScreen : Any> ContentScreen.asFullScreenModal(): FullScreenModal<ContentScreen> {
    return FullScreenModal(this)
}