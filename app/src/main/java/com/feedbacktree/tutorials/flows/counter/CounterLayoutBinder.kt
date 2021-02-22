package com.feedbacktree.tutorials.flows.counter

import androidx.core.view.isInvisible
import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.flow.ui.views.core.backPresses
import com.feedbacktree.tutorials.databinding.CounterBinding
import com.feedbacktree.utils.actionBarTitle
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable

val CounterLayoutBinder = LayoutBinder.create(
    viewBindingInflater = CounterBinding::inflate,
    sink = CounterScreen::sink
) { viewBinding ->

    viewBinding.actionBarTitle = "Counter"

    bind { screen: Observable<CounterScreen> ->
        subscriptions = listOf(
            screen.map { it.counterText }.subscribe { viewBinding.counterTextView.text = it },
            screen.map { it.isDecrementButtonInvisible }
                .subscribe { viewBinding.decrementButton.isInvisible = it }
        )
        events = listOf(
            viewBinding.incrementButton.clicks().map { Event.Increment },
            viewBinding.decrementButton.clicks().map { Event.Decrement },
            viewBinding.root.backPresses().map { Event.BackPressed },
        )
    }
}