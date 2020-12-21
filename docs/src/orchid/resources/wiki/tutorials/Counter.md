In this tutorial, you will learn the steps needed to set up a basic FeedbackTree `Flow`. 
You will build a screen that has a counter and two buttons to increment and decrement the counter.

<div style:"text-align: center;"> 
  <img style: src="FeedbackTree/assets/media/counter_app_screenshot.jpeg" width="250"/> 
</div>

<br>

### Getting Started

Let's start by adding FeedbackTree as a dependency to the `build.gradle (app)` file. 

```gro
dependencies {
  implementation "com.github.eliekarouz.feedbacktree:feedbacktree:0.11.1"
}
```

Note that the library is published to `Maven Central`. You might also need to add to your `build.gradle (.)`

```groo
allprojects {
  repositories {
    mavenCentral()
  }
}
```

Although it is a very simple screen, and, we might not use the full power of FeedbackTree, you will benefit a lot if you don't have any knowledge about how to build state machines in Kotlin.

### State and Events

*What is a State?*\
A state is the condition your software is in at a specific time. In the context of this tutorial, it is just the value of the counter. \
In Kotlin, states can be represented using `data classes`, `enums`, `sealed classes` or even  `primitive types` . The latter is rarely used in practice.

*What is an Event?*\
It is the external input that will allow the software to transition from one state to another. In the context of this tutorial, you have two events, the user clicked on the increment or decrement buttons.\
In Kotlin, events are usually represented with `sealed classes`

Let's go back to our example, and write the state and events in Kotlin:

```kotlin
data class State(
    val counter: Int
)

sealed class Event {
    object Increment : Event()
    object Decrement : Event()
}
```

Note that we could have just use the `Int` to represent the state but it is really rare in practice to have one primitive type as a state, that's why we will stick with a `data class`.

### Stepper

If you haven't noticed yet, the counter is a `val` and I assume you are wondering how we will update the state when the attributes are immutable. We won't, we will just create a new state!

In FeedbackTree or more generally with state machines, an event "advances" the state machine to a new state. In some cases, the event can cause the state machine to reach an end.  That transformation is called `Stepper` in FeedbackTree.

A stepper is a [pure function](https://en.wikipedia.org/wiki/Pure_function) with the following signature: `(state: State, event: Event) -> Step<State, Output>`.  \
In other terms, the stepper takes the current state your in and the event that occured, and, produces a `Step` which is either the new state we will get in next or the end of state machine/flow.

If we want to write the write the stepper for the Counter tutorial, 

```kotlin
fun stepper(state: State, event: Event): Step<State, Unit> {
    return when (event) {
        Event.Increment -> state.copy(counter = state.counter + 1).advance()
        Event.Decrement -> state.copy(counter = state.counter - 1).advance()
    }
}
```

Two things to notice:

- We are using the `copy` feature of `data classes` to create a new state with a different `counter` value.
- `state.advance()` is a syntactic sugar that converts the new state into a `Step` so that it can be returned to the `Stepper`. 

