package com.feedbacktree.example.flows.testexamples.fullscreen

import com.feedbacktree.example.R
import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.flow.ui.views.core.backPresses
import io.reactivex.Observable

val HelloWorldLayoutBinder = LayoutBinder.create(
    R.layout.hello_world_top_bottom,
    HelloWorldScreen::sink
) { view ->

    bind {
        events = listOf<Observable<Event>>(
            view.backPresses().map { Event.BackPressed }
        )
    }
}