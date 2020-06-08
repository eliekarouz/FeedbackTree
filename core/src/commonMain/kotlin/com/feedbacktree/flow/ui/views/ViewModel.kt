package com.feedbacktree.flow.ui.views

import com.feedbacktree.flow.core.Sink

interface ViewModel<EventT : Any> {
    val sink: Sink<EventT>
}
