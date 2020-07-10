---
title: Quick Start Guide
order: 1 
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

In the example below, we will be implementing a simple login flow with the following requirements

- User should be able to enter his email and password
- Sign In button
  - Disabled (gray) when the email/password fields are empty. Colored using the main theme otherwise.
  - Button text is "Sign In" but changes to "Signing In" once the user clicks on it.
- It could be that the user has already entered his email somewhere in the app before kickstarting the login process. The input of the login flow will be the email that we would like to start with.

##### State

State machines are the base of FeedbackTree. Each `Flow` clearly defines the State that will be driving the process. 

```kotlin
data class State(
    val email: String = "",
    val password: String = "",
    val isLoggingIn: Boolean = false
)
```

##### Event

You use a `sealed class` to declare the Events which are responsible of progressing the Flow.
Events can be seen as the things that happened like UI clicks, network call responses... They are the triggers that are used to move to a new State or complete the Flow. 

```kotlin
sealed class Event {
    data class EnteredEmail(val email: String) : Event()
    data class EnteredPassword(val password: String) : Event()
    object ClickedLogin : Event()
    data class ReceivedLogInResponse(val success: Boolean) : Event()
}
```

##### Stepper

The stepper is where you write how the Flow updates when an Event is emitted. The Stepper is a function takes a State and Event and produces a Step. A Step can be either a:

- The new State you are in\
  You use the method `advance()` to advance to the new State
- The Output of the Flow\
  You use the method `endFlowWith(someOutput)` to complete the Flow. 

```kotlin
fun stepper(state: State, event: Event): Step<State, User> {
    return when (event) {
        is Event.EnteredEmail -> state.copy(email = event.email).advance()
        is Event.EnteredPassword -> state.copy(password = event.password).advance()
        Event.ClickedLogin -> endFlowWith(Unit)
        is Event.ReceivedLogInResponse -> endFlowWith(
            User(email = state.email)
        )
    }
}
```

##### Feedbacks

You use feedbacks to perform the side effects like calling an API, reading from a bluetooth device, running database operations... More details about feedback loops can be found in the [Feedbacks](/FeedbackTree/flow/FeedbackLoops/) section. In the example below, we are saying that when we are in the state logging in, we call the `login` method and transform the result of the login into an Event that will be sent to the Stepper.

```kotlin
typealias LoginQuery = Pair<String, String>

fun loginFeedback() = react<State, LoginQuery, Event>(
    query = { state ->
        state
            .takeIf {
                it.isLoggingIn
            }?.let {
                it.email to it.password
            }
    },
    effects = { (email, password) ->
        login(
            email = email,
            password = password
        ) // Observable<Boolean>, true when authentication succeeds
        .map { loginSucceeded ->
            Event.ReceivedLogInResponse(loginSucceeded)
        }
    }
)
```

##### ViewModel / Screen

The ViewModel is just a UI representation of the State that will be rendered on the phone screen.

```kotlin
data class LoginViewModel(
    val state: State,
    val sink: (Event) -> Unit
) {
    val email: String
        get() = state.email

    val loginButtonTitle: String
        get() = if (state.isLoggingIn) "Signing In" else "Sign In"

    val isLoginButtonEnabled: Boolean
        get() = state.email.isNotEmpty() && state.password.isNotEmpty()
}

```



##### The Flow

It is time to group the different components we created so far into a Flow.
As stated above the Flow is declared like this`Flow<Input, State, Event, Output, ViewModel>`. 

```kotlin
val LoginFlow = Flow<String, State, Event, User, LoginViewModel>
    initialState = { email -> State(email = email) }, // A function that builds the initial state from the input
    stepper = ::stepper, // A funtion that will advance or complete the Flow
    feedbacks = listOf(loginFeedback()) // The list of Feedback loops to perform side effects
) { state, context -> 
    // A function to render the State into ViewModels. 
    // Sink is here to pass Events from the Login View to the flow.
    LoginViewModel(state, context.sink)
}
```

##### To Android Views

The LoginFlow renders a LoginViewModel. The LoginViewModel is a simple UI representation of the state. What we need to complete the puzzle is some code that will create the corresponding Login View, update its ui elements when the state updates and consume clicks and events generated by the user and pass them back to the Flow.

The LayoutRunner comes in hand for this purpose. 

```kotlin
class LoginLayoutRunner(private val view: View) : LayoutRunner<LoginViewModel, Event> {
    // More about Feedbacks in the Feedbacks section.
    override fun feedbacks() = listOf(bindUI())

    private fun bindUI() = bind<LoginViewModel, Event> { screen ->
       with(view) {
         // "subscriptions" are used to fill the UI
         // "events" are used to pass the list of UI triggers to the flow.
         Bindings(
           subscriptions = listOf<Disposable>(
             screen.map { it.email }.subscribe { inputEmail.updateText(it) },
             screen.map { it.isLoginButtonEnabled }.subscribe { btnLogin.isEnabled = it }
           ),
           events = listOf<Observable<Event>>(
             inputEmail.ftTextChanges().map { Event.EnteredEmail(it) },
             inputPassword.ftTextChanges().map { Event.EnteredPassword(it) },
             btnLogin.clicks().map { Event.ClickedLogin }
           )
         )
       }	
    }

    // This code tells FeedbackTree rendering engine to:
    // 1. Inflate the R.layout.login.xml when the LoginViewModel is returned. 
    // 2. Use this LayoutRunner to update the UI elements
    // 3. To forward ui events to the Flow via the LoginViewModel.sink
    companion object : ViewBinding<LoginViewModel> by LayoutRunner.bind(
        R.layout.login, ::LoginLayoutRunner, LoginViewModel::sink
    )
}
```



##### And voila!

Combining all the pieces together... 

```kotlin
class LoginActivity : AppCompatActivity() {

    var disposable: Disposable? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      
      	// Tells the Activity to use use the LoginLayoutRunner when you want to 
        // render the LoginViewModel
      	val viewRegistry = ViewRegistry(LoginLayoutRunner)
      
        disposable = startFlow(
          input = "email@example.com",
          flow = LoginFlow,
          viewRegistry = viewRegistry,
          onOutput = {
            // Do something with the output if you want
          })
    }
  
    // ... 
}
```

No need to panic! You don't have to create an Activity for each flow. More about this in the sections [Starting Flows](/FeedbackTree/flow/StartingFlows/) and [Rendering](/FeedbackTree/rendering/Introduction/) 