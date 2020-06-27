---
title: FeedbackTree
---

### Introduction
Feedback tree is a unidirectional reactive architecture that allows you to build Android UIs in Kotlin
with the focus on organizing the code around business rules.
Core concepts:
- Flow which you kickstart with some input and wait for the output to be produced.
- The flow is state driven and the state is used for rendering and navigation.
- Events are used to update the state of the flow.
- Stepper is used to progress or complete a flow based on the events received. It is a pure function which
makes unit testing very straightforward
- Feedback loops are used separate business logic from side effects like network calls, timers, bluetooth...

### Installation
Add this in your project root build.gradle:
```gradle
allprojects {
  repositories {
    mavenCentral()
  }
}
```

##### Android Projects
Add this to your app build.gradle
```gradle
dependencies {
  implementation "com.github.eliekarouz.feedbacktree:feedbacktree:0.10.1"
}
```

##### Kotlin Multiplatform
We are only supporting the concept of `Step` and `Stepper` in Kotlin multiplatform (iOS/Android only).
We might provide additional multiplatform support in the future.
To include these concepts to a multipatform module, add this dependency to commonMain.
```gradle
dependencies {
    api "com.github.eliekarouz.feedbacktree:core:0.10.1"
}
```
If you are targeting iOS you will also need to export the dependencies:
```gradle
framework("Example") {
    export "com.github.eliekarouz.feedbacktree:core:0.10.1"
    transitiveExport = true
}
```

### Example

In the example below, we will be taking a simple login screen:

##### State

```kotlin
data class State(
    val email: String = "",
    val password: String = "",
    val isLoggingIn: Boolean = false
)
```

##### Event

```kotlin
sealed class Event {
    data class EnteredEmail(val email: String) : Event()
    data class EnteredPassword(val password: String) : Event()
    object ClickedLogin : Event()
    data class ReceivedLogInResponse(val success: Boolean) : Event()
}
```

##### Stepper

```kotlin
fun stepper(state: State, event: Event): Step<State, Unit> {
    return when (event) {
        is Event.EnteredEmail -> state.copy(email = event.email).advance()
        is Event.EnteredPassword -> state.copy(password = event.password).advance()
        Event.ClickedLogin -> endFlowWith(Unit)
        is Event.ReceivedLogInResponse -> endFlowWith(
            Unit
        )
    }
}
```

##### Feedbacks

```kotlin

```

##### ViewModel

```kotlin

```

##### The Flow

```kotlin
val LoginFlow = Flow<Unit, State, Event, Unit, LoginViewModel>(
    initialState = { State() },
    stepper = ::stepper,
    feedbacks = listOf()
) { state, context ->
    LoginViewModel(state, context.sink)
}
```

##### View binding


##### And voila!