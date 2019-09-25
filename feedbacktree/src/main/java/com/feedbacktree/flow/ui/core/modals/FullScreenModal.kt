package com.feedbacktree.flow.ui.core.modals

class FullScreenModal<ContentScreen : Any>(val screen: ContentScreen) : Modal

fun <ContentScreen : Any> ContentScreen.asFullScreenModal(): FullScreenModal<ContentScreen> {
    return FullScreenModal(this)
}