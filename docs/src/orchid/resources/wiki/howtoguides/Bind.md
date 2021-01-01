FeedbackTree provides different factories to create Feedback Loops. `bind` is one of them. You can use `bind` to either create:

### UI Feedback Loop

You are going to use `bind` to create UI bindings in the `LayoutBinder` DSL.

```kotlin
bind { screen: Observable<CounterScreen> ->
    subscriptions = listOf(
        screen.map { it.counterText }.subscribe { counterTextView.text = it },
    )
    events = listOf(
        incrementButton.clicks().map { Event.Increment },
        decrementButton.clicks().map { Event.Decrement },
    )
}
```

You get a stream of **screens** that you can subscribe to and update UI elements.\
You can generate stream of events that will be forwarded to the Flow.

### Non-UI Feedback Loop

Although `bind` is not as frequently used for non-ui binding as for UI binding, you can still use it to create a non-UI binding that can be added to the flow feedbacks:\

```kotlin
val someNonUIFeedback: Feedback<State, Event> = bind<State, Event> { state: Observable<State> ->
    subscriptions = listOf() // <- add the subscriptions here
    events = listOf() // <- add the events here
}
```

You get a stream of **states** that you can subscribe to.\
You can generate stream of events that will be forwarded to the Flow.

