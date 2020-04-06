# FeedbackTree

**The API is not stable yet as we are still experimenting with the concept.**

FeedbackTree is a unidirectional data flow architecture for Android which relies heavily on state mahines to perform all types of side effects, like network calls, bluetooth, UI updates as well as navigation.
It was highly inspired by [RxFeedback.swift](www.github.com/notests/rxfeedback.swift) and by [Square Workflows](www.github.com/square/workflow)

#### Introduction
Most UI architectural patterns out there are screen-based meaning that the code is always organized around a particular screen or part of a screen. For instance, in MVVM (i.e. MVP) you have a View-Model (i.e. Presenter) driving a particular View telling how to update the different UI components as the state in the ViewModel (i.e. Presenter) is progressing. 

As you hit more complex flows that span over multiple screens, organising the shared area gets harder.
FeedbackTree allows you to confine business logic in what is called a `Flow`. Business logic or `Flows` drive what should be displayed to the user even if the `Flow` spans over multiple screens.

Let's elucidate how FeedbackTree is different by considering the example of a payment flow where the user should enter his card number on the first screen, his address on the second and once he taps the button `Pay` we show a progress dialog.
In FeedbackTree, you create a `PaymentFlow`, and, depending on which state you are in, the flow renders the appropriate layout.

- If you are in the state `EnteringCreditCardNumber`, render the `EnterCreditNumberViewModel`
- If you are in the state `EnteringAddress`, render the `EnterAddressViewModel`
- If you are in the state `Paying`, show a `ProgressDialog`

FeedbackTree:
- Organizes the code around the business rules.
- Logic can be easily unit tested
	- Most logic is placed in the `Stepper` which is pure function.
	- Side effect are isolated and testing can be done with a TestScheduler.
- Built-in navigation 
- Reactive UI

#### Installation

The project recently migrated from `jitpack` to `Maven Central`

Add it in your root build.gradle:
```
allprojects {
  repositories {
    mavenCentral()
  }
}
```

Add the dependency
```
dependencies {
  implementation "com.github.eliekarouz.feedbacktree:feedbacktree:0.8.5"
}
```
