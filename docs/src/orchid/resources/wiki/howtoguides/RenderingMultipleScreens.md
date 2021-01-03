A Flow in FeedbackTree is not bound to a particular screen/layout. In fact, a flow can render multiple screens. You can do that by setting the `ScreenType` to `Any` (the 5th generic argument when you create a Flow):

### Example

Here's an example of a phone registration code where the user enters their phone number and then validate it using a code sent by SMS:

```kotlin
val phoneNumberRegistrationFlow = Flow<Unit, State, Event, Unit, Any>(
    initialState = {
        ...
    },
    stepper = { state, event ->
        when (event) {
            ...
        }
    },
    feedbacks = listOf(...),
    render = { state, context ->
        when (state.progress) {
            EnteringNumber -> EnterPhoneScreen(state, context.sink)
            SendingCode -> LoadingScreen(message = "Sending Code", context.sink)
            EnteringRegistrationCode -> EnterRegistrationCodeScreen(state, context.sink)
            Registering -> LoadingScreen(message = "Validating Code", context.sink)
        }
    }
)
```

You can check the complete Flow <a href="https://github.com/eliekarouz/FeedbackTree/tree/master/app/src/main/java/com/feedbacktree/tutorials/flows/phonenumber/PhoneNumberRegistrationFlow.kt" target="_blank">here</a>,