### Getting Started

In this tutorial, we will be implementing a simple login flow with the following requirements

- User should be able to enter his email and password
- Sign In button
  - Disabled (gray) when the email/password fields are empty. Colored using the main theme otherwise.
  - Button text is "Sign In" but changes to "Signing In" once the user clicks on it.
- It could be that the user has already entered his email somewhere in the app before kickstarting the login process. The input of the login flow will be the email that we would like to start with.

##### The Login Flow

Create a new package called **login** and add to it a new Kotlin file called **LoginFlow.kt**, then. add the code below to it:

```kotlin
import com.feedbacktree.flow.core.*
import io.reactivex.Observable

val LoginFlow = Flow<String, State, Event, Unit, LoginScreen>( // 1
    initialState = { lastEmailUsed -> State(email = lastEmailUsed) }, // 2
    stepper = { state, event ->
        when (event) {
            is Event.EnteredEmail -> state.copy(email = event.email).advance()
            is Event.EnteredPassword -> state.copy(password = event.password).advance()
            Event.ClickedLogin -> state.copy(isLoggingIn = true).advance()
            is Event.ReceivedLogInResponse -> {
                if (event.success) {
                    endFlow() // 3
                } else {
                    state.copy(isLoggingIn = false).advance() // 4
                }
            }
        }
    },
    feedbacks = listOf(),
    render = { state, context -> 
        LoginScreen(state, context.sink) 
    }
)

data class State(
    val email: String = "",
    val password: String = "",
    val isLoggingIn: Boolean = false
)

sealed class Event {
    data class EnteredEmail(val email: String) : Event()
    data class EnteredPassword(val password: String) : Event()
    object ClickedLogin : Event()
    data class ReceivedLogInResponse(val success: Boolean) : Event()
}

data class LoginScreen(
    private val state: State,
    val sink: (Event) -> Unit
) {
    val email: String
        get() = state.email

    val loginButtonTitle: String // 5
        get() = if (state.isLoggingIn) "Signing In" else "Sign In"

    val isLoginButtonEnabled: Boolean
        get() = state.email.isNotEmpty() && state.password.isNotEmpty()
}
```

Here's the breakdown of the code above:

1. Here is the signature of the FeedbackTree Flow `Flow<Input, State, Event, Output, Screen> `:
   - The `Input` of the  `LoginFlow` is a String that represents the last email used.
   - The `State` of the Flow
   - The `Event` that is used to update the state of the `Flow`
   - The `Output` is of type `Unit` which means the `Flow` can complete. 
   - The `Screen` produced is a `LoginScreen`.
2. We build the initial state using the String input.
3. When the login succeeds, we will terminate the flow. 
4. When the login fails, we set back `isLoggingIn` in to false. Ideally, we  should tell the user that something went wrong. We will do this in the next sections
5. When we are logging in we will change the sign in button title to "Signing In" to tell the user that the operation is running.

**Where is the Sign in logic?** 

You use `Feedbacks` to perform side effects, like calling an API, reading from a bluetooth device, running database operations or even updating the UI...\
We have seen in the Counter tutorial a way to build a UI binding `Feedback` using the `bind` operator. Here we will use the `react` operator to perform the authentication logic. 

```kotlin
private data class LoginQuery(
    val email: String,
    val password: String
)
private fun loginFeedback(): Feedback<State, Event> = react<State, LoginQuery, Event>(
    query = { state -> // 1
        if (state.isLoggingIn) {
            LoginQuery(email = state.email, password = state.password)
        } else {
            null
        }
    },
    effects = { queryResult -> // 2
        val authenticationSuccess: Observable<Boolean> = AuthenticationManager.login(
            email = queryResult.email,
            password = queryResult.password
        ) // 3
        authenticationSuccess.map { loginSucceeded ->
            Event.ReceivedLogInResponse(loginSucceeded) // 4
        }
    }
)
```

A `react` feedback loop is a declarative way to perform side effects. Here's a detailed breakdown of what how the Feedback above will run:

1. For every state the flow gets into, the `query` will be evaluated. As soon the evaluated value is different than `null`, the `effects` will kickstart.
2. The `effects` is a block of code that takes the `queryResult`, evaluated in the `query` block, perform the side effect like executing the authentication logic, and emit back an `Event` when done. The signature of the effets is `(Query) -> Observable<Event>`
3. `authenticationSuccess` is an `Observable<Boolean>` that will perform the authentication logic and return true when done.
4. map the `Observable<Boolean>` into an `Observable<Event>` that is returned to the `effects` block. The events being produced will be sent back to the `Flow` to update the state.

Now it's time to add the `loginFeedback()` to the list of `feedbacks` in the `Flow`

```kotlin
val LoginFlow = Flow<String, State, Event, Unit, LoginScreen>(
    ...
    feedbacks = listOf(loginFeedback()),
    ...
)
```



##### The Declarative UI

The `LoginFlow` renders a `LoginScreen`. The `LoginScreen` is a UI representation of the state. What we need to complete the puzzle is some code that will create the corresponding Login View, update its ui elements when the state updates and consume clicks and events generated by the user and pass them back to the Flow.

In the **login** package, add a new file called **LoginLayoutBinder.kt** and the code below to it:

```kotlin
import android.view.View
import android.widget.Button
import com.feedbacktree.example.R
import com.feedbacktree.example.util.FTEditText
import com.feedbacktree.flow.core.Bindings
import com.feedbacktree.flow.core.bind
import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable

val LoginLayoutBinder = LayoutBinder.create(
    layoutId = R.layout.login,
    sink = LoginScreen::sink,
) { view ->
    val emailEditText: FTEditText = view.findViewById(R.id.inputEmail) // 3
    val passwordEditText: FTEditText = view.findViewById(R.id.inputPassword)
    val btnLogin: Button = view.findViewById(R.id.btnLogin)

    bind { screen ->
        subscriptions = listOf(
            screen.map { it.emailText }.subscribe { emailEditText.text = it }, // 2
            screen.map { it.passwordText }.subscribe { passwordEditText.text = it },
            screen.map { it.loginButtonTitle }.subscribe { btnLogin.text = it },
            screen.map { it.isLoginButtonEnabled }.subscribe { btnLogin.isEnabled = it }
        )
        events = listOf(
            emailEditText.textChanges().map { Event.EnteredEmail(it.toString()) }, // 1
            passwordEditText.textChanges().map { Event.EnteredPassword(it.toString()) },
            btnLogin.clicks().map { Event.ClickedLogin }
        )
    }
}
```

1. `emailEditText.textChanges().map { Event.EnteredEmail(it.toString()) }` uses the `textChanges()` from [RxBinding](https://github.com/JakeWharton/RxBinding) to capture the `emailEditText` updates and map it to an `Event`.
2. We subscribe the `emailText`  in `screen.map { it.emailText }.subscribe { emailEditText.text = it }` for two puposes:
   1. The `Flow` can start with the last email that was used to login. So the `emailEditText` can initially be non-empty. 
   2. It is recommended to always rely on the state as the single source of truth.  In other terms, store the values of the textfields in the state and use what's in the state to drive the UI. This technique comes handy when the device configuration changes and a new layout is be inflated which would allow FeedbackTree to automatically refill the new layout from what is stored in the state.
3. If you haven't noticed yet, we are doing a two-way binding for the `emailEditText.text` property, which means that we set the `emailEditText.text` in subscriptions and listen to the text changes in the events.\
   The problem of the `EditText` is that watchers are notified when the `text` is updated **programmatically** which will cause **infinte** update cycles/loops when two-way binding is applied. The `FTEditText` breaks the infinite update cycles. The `FTEditText` mainly removes the `TextWatchers` , updates the `text` property, before adding back the watchers that were removed. You can check [here](https://github.com/eliekarouz/FeedbackTree/blob/master/app/src/main/java/com/feedbacktree/example/util/FTEditText.kt) the full implementation in case you want to apply the same logic for other controls like switches. 
4. We are using  `clicks()` from [RxBinding](https://github.com/JakeWharton/RxBinding) to capture the the View clicks.

##### **Starting the Flow**

Combining all the pieces together... 

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.feedbacktree.flow.core.startFlow
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import io.reactivex.disposables.Disposable

class LoginActivity : AppCompatActivity() {

    var disposable: Disposable? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      
      	val viewRegistry = ViewRegistry(LoginLayoutBinder) // 1
      
        disposable = startFlow(
          input = "developer@feedbacktree.com", // 2
          flow = LoginFlow,
          viewRegistry = viewRegistry,
          onOutput = {
            // Do something with the output if you want
          })
    }
  
    override fun onPause() {
        super.onPause()
        // 3 
        if (isFinishing) {
            disposable?.dispose() 
            disposable = null
        }
    }
}
```

1. A `ViewRegistry` is a lookup table that FeedbackTree uses to create the corresponding layout when some `Screen` is produced by the `Flow`. We are registering the `LoginLayoutBinder` `companion object` from the previous section to the `viewRegistry` variable. 
2. Start the `LoginFlow` with the last email used.
3. Terminate the flow when the activity finishes.

### Where to Go From Here?

In the next tutorial will create a demo application where we will see the reason there is a "Tree" in FeedbackTree.