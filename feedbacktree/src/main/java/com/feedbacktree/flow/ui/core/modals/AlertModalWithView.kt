/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.core.modals

data class AlertModalWithView<ContentViewModelT : Any>(
    val alertModal: AlertModal,
    val contentViewModel: ContentViewModelT
) : Modal

fun <ContentViewModelT : Any> AlertModal.withView(contentScreen: ContentViewModelT): AlertModalWithView<ContentViewModelT> {
    return AlertModalWithView(this, contentScreen)
}