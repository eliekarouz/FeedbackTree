package com.feedbacktree.example.flows.testexamples.fullscreen

import com.feedbacktree.flow.core.Sink
import com.feedbacktree.flow.ui.views.ViewModel

data class HelloWorldViewModel(override val sink: Sink<Event>) :
    ViewModel<Event>