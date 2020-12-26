/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.core.modals

data class AlertModalWithView<ScreenT : Any>(
    val alertModal: AlertModal,
    val contentScreen: ScreenT
) : Modal

fun <ScreenT : Any> AlertModal.withView(contentScreen: ScreenT): AlertModalWithView<ScreenT> {
    return AlertModalWithView(this, contentScreen)
}