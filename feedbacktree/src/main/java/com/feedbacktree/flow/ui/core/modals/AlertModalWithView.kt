package com.feedbacktree.flow.ui.core.modals

data class AlertModalWithView<ContentScreen : Any>(
    val alertModal: AlertModal,
    val contentScreen: ContentScreen
) : Modal

fun <ContentScreen : Any> AlertModal.withView(contentScreen: ContentScreen): AlertModalWithView<ContentScreen> {
    return AlertModalWithView(this, contentScreen)
}