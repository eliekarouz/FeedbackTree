When the number of effects you want to kickstart is dynamic, you can use the `reactSet` operator which does pretty much the same thing as `react` but allows you to run simultaneously multiple effects. 

```kotlin
fun reactFeedback(): Feedback<State, Event> = reactSet<State, Query, Event>(
    query = { state ->
				// return either a Set<Query>
    },
    effects = { query: Query ->
				// Use the query to perform side effect, network calls, bluetooth, database...
				// return an Observable<Event>
    }
)
```

### The Rules

- Each time the flow enters a new state, the query is evaluated and a `Set<Query>` is returned.
- The operator compares the **old** query with the **new** query:
  - Effects are not interrupted for elements in the new query that were present in the old query.
  - Effects are canceled for elements present in old query but not in new query.
  - In case new elements are present in new query (and not in old query) they are being passed to the effects lambda and resulting effects are being performed
- The effects will run in parallel.

### Example

Let's take an example where the user is presented with a list of files that they can download:

```kotlin
data class FileResource(
    val url: String,
    val name: String,
    val localUrl: String?
)

data class State(
    val fileResources: List<FileResource>, // The list of all files
    val resourceUrlsToDownload: Set<String> // The resouce files being downloaded
)

sealed class Event {
    data class UserRequestedResource(val fileResource: FileResource) : Event()
    data class LoadedResource(val url: String, val localUrl: String) : Event()
}

private fun downloadResourcesFeedback(): Feedback<State, Event> = reactSet<State, String, Event>(
    query = { state ->
        state.resourceUrlsToDownload
    },
    effects = { url -> 
        downloadResource(url).map { localUrl ->
            Event.LoadedResource(url, localUrl)
        }
    }
)

```

You can check the complete Flow <a href="https://github.com/eliekarouz/FeedbackTree/tree/master/app/src/main/java/com/feedbacktree/tutorials/flows/resources/ResoucesFlow.kt" target="_blank">here</a>.