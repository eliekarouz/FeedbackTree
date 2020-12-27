package com.feedbacktree.tutorials.flows.counter

import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.flow.ui.views.core.backPresses
import com.feedbacktree.tutorials.R
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable

val CounterLayoutBinder = LayoutBinder.create(
    layoutId = R.layout.counter,
    sink = CounterScreen::sink
) { view ->

    val counterTextView = view.findViewById<TextView>(R.id.counterTextView)
    val incrementButton = view.findViewById<Button>(R.id.incrementButton)
    val decrementButton = view.findViewById<Button>(R.id.decrementButton)

    (view.context as AppCompatActivity).supportActionBar?.title = "Counter"

    bind { screen: Observable<CounterScreen> ->
        subscriptions = listOf(
            screen.map { it.counterText }.subscribe { counterTextView.text = it },
            screen.map { it.isDecrementButtonInvisible }
                .subscribe { decrementButton.isInvisible = it }
        )
        events = listOf(
            incrementButton.clicks().map { Event.Increment },
            decrementButton.clicks().map { Event.Decrement },
            view.backPresses().map { Event.BackPressed },
        )
    }
}