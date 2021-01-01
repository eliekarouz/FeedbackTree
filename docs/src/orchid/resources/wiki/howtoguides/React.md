The most used factory of feedback loops is `react`. Like the `bind` operator, it can be used to perform **non-UI** feedback loops or even **UI** feedback loops. The former is more frequently used in practice. Here's how to use it:

```kotlin
fun reactFeedback(): Feedback<State, Event> = react<State, Query, Event>(
    query = { state ->
				// return either a Query or null
    },
    effects = { query: Query ->
				// Use the query to perform side effect, network calls, bluetooth, database...
				// return an Observable<Event>
    }
)
```

### The Rules

- Each time the flow enters a new state, the query is evaluated. 
- The effect kickstarts only when the query is **not** null.
- Eventhough the state might be updating while an effect is running, the effect won't be triggered again as long as the query value is the **same**. 
- If the query evaluates to a different value, the previous effect is disposed and a new effect with the new value kickstarts.

### Example

Here's an example from the [Login Tutorial](../tutorials/Login)

```kotlin
fun loginFeedback(): Feedback<State, Event> = react<State, LoginQuery, Event>(
    query = { state ->
        if (state.isLoggingIn) {
            LoginQuery(email = state.email, password = state.password)
        } else {
            null
        }
    },
    effects = { queryResult ->
        val authenticationSuccess: Observable<Boolean> = AuthenticationManager.login(
            email = queryResult.email,
            password = queryResult.password
        )
        authenticationSuccess.map { loginSucceeded ->
            Event.ReceivedLogInResponse(loginSucceeded)
        }
    }
)
```