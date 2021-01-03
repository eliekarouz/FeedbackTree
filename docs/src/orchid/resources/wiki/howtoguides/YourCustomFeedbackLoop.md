The `bind` and `react` operators cover 99% of the cases you might encounter but you can still create your custom feedback loop if you need to.

A feedback loop is just a function that takes the **states**  and returns a stream of **events**:\
`(Observable<State>) -> Observable<Event>`. 

To be more accurate it is `(ObservableSchedulerContext<State>) -> Observable<Event>`:

- You use `ObservableSchedulerContext.source` to get to the `Observable<State>`
- You use `ObservableSchedulerContext.scheduler` to access the Flow scheduler (usually the main thread) and you should enqueue all events to that scheduler.

### UI Feedback Loop

When you want to create a custom UI feedback in the `LayoutBinder`

```kotlin
LayoutBinder.create(...) {
	feedbacks.add { screenScheduler ->
		// screenScheduler.source is the Observable<Screen>
      Observable.empty<Event>()
                 .observeOn(screenScheduler.scheduler)
  }
}
```



### Non-UI Feedback Loop

When you want to create a custom feedback that you can add to the `Flow.feedbacks`:

```kotlin 
fun yourCustomFeedback(): Feedback<State, Event> = { stateScheduler ->
		// stateScheduler.source is the Observable<State>
      Observable.empty<Event>()
                 .observeOn(screenScheduler.scheduler)
}
```

