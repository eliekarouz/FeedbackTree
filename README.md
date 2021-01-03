# FeedbackTree

**The API is not stable yet as we are still experimenting with the concept.**

FeedbackTree is a unidirectional data flow architecture for Android which relies heavily on state mahines to perform all types of side effects, like network calls, bluetooth, UI updates as well as navigation.

#### Why FeedbackTree?

- Organizes the code around the business rules.
- Reactive Declarative UI
- Built-in navigation
- Logic can be easily unit tested
  - Most logic is placed in the `Stepper` which is pure function.
  - Side effect are isolated and testing can be done with a TestScheduler.

#### Core concepts

- Business rules are wrapped in a Flow. You kickstart the Flow with some input and wait for the output to be produced.
- The Flow is driven by a state machine. 
- You emit Events to advance the Flow.
- Stepper is used to progress or complete a Flow based on the Events received. It is a pure function which
  makes unit testing very straightforward.
- Feedback loops are used separate business logic from side effects like network calls, database operations, timers, bluetooth...

#### Installation

The project recently migrated from `jitpack` to `Maven Central`

Add it in your root build.gradle:
```groovy
allprojects {
  repositories {
    mavenCentral()
  }
}
```

##### Android Projects
Add this to your build.gradle (app)
```groovy
dependencies {
  implementation "com.github.eliekarouz.feedbacktree:feedbacktree:0.12.0"
  implementation "com.github.eliekarouz.feedbacktree:core:0.12.0"
}
```

##### Kotlin Multiplatform
We are only supporting the concept of `Step` and `Stepper` in Kotlin multiplatform (iOS/Android only).  
We might provide additional multiplatform support in the future.
To include these concepts to a multipatform module, add this dependency to commonMain.
```groovy
dependencies {
    api "com.github.eliekarouz.feedbacktree:core:0.12.0"
}
```
If you are targeting iOS you will also need to export the dependencies:
```groovy
framework("Example") {
    export "com.github.eliekarouz.feedbacktree:core:0.12.0"
    transitiveExport = true
}
```

##### Versions

|Kotlin|FeedbackTree|
|------|------|
|1.3.72|0.10.2|
|1.4.0|0.11|
|1.4.10|0.11.1|

#### Acknowledgements

The following repos were a great source of inspiration:

- <a href="https://www.github.com/notests/rxfeedback.swift" target="_blank">RxFeedback</a>
- <a href="https://www.github.com/square/workflow" target="_blank">Workflow</a>

