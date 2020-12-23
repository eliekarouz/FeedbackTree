package com.feedbacktree.example.flows.counter

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isInvisible
import com.feedbacktree.example.R
import com.feedbacktree.flow.core.Bindings
import com.feedbacktree.flow.core.Feedback
import com.feedbacktree.flow.core.bind
import com.feedbacktree.flow.ui.views.LayoutRunner
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.feedbacktree.flow.ui.views.core.backPresses
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class CounterLayoutBinder(private val view: View) : LayoutRunner<CounterScreen, Event> {

    private val counterTextView = view.findViewById<TextView>(R.id.counterTextView)
    private val incrementButton = view.findViewById<Button>(R.id.incrementButton)
    private val decrementButton = view.findViewById<Button>(R.id.decrementButton)

    init {
        (view.context as Activity).title = "Counter"
    }

    override fun feedbacks(): List<Feedback<CounterScreen, Event>> {
        return listOf(bindUI())
    }

    private fun bindUI(): Feedback<CounterScreen, Event> =
        bind { screens: Observable<CounterScreen> ->
            val subscriptions: List<Disposable> = listOf(
                screens.map { it.counterText }.subscribe { counterTextView.text = it },
                screens.map { it.isDecrementButtonInvisible }
                    .subscribe { decrementButton.isInvisible = it }
            )
            val events: List<Observable<Event>> = listOf(
                incrementButton.clicks().map { Event.Increment },
                decrementButton.clicks().map { Event.Decrement },
                view.backPresses().map { Event.BackPressed },
            )
            return@bind Bindings(subscriptions, events)
    }

    companion object : ViewBinding<CounterScreen> by LayoutRunner.bind(
        R.layout.counter, ::CounterLayoutBinder, CounterScreen::sink
    )
}