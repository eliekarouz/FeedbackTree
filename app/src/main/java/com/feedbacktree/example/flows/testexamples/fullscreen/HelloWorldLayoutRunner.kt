package com.feedbacktree.example.flows.testexamples.fullscreen

import android.view.View
import com.feedbacktree.example.R
import com.feedbacktree.flow.core.Bindings
import com.feedbacktree.flow.core.bind
import com.feedbacktree.flow.ui.views.LayoutRunner
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.feedbacktree.flow.ui.views.core.backPresses
import io.reactivex.Observable

class HelloWorldLayoutRunner(private val view: View) : LayoutRunner<HelloWorldViewModel, Event> {

    override fun feedbacks() = listOf(bindUI())

    private fun bindUI() = bind<HelloWorldViewModel, Event> {
        return@bind Bindings(
            subscriptions = listOf(),
            events = listOf<Observable<Event>>(
                view.backPresses().map { Event.BackPressed }
            )
        )
    }

    companion object : ViewBinding<HelloWorldViewModel> by LayoutRunner.bind(
        R.layout.hello_world_top_bottom, ::HelloWorldLayoutRunner, HelloWorldViewModel::sink
    )
}