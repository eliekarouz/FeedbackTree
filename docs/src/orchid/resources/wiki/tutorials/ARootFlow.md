In this tutorial, you will learn how to kickstart the **Counter** and **Login** flows that you created in the previous tutorials from a screen that shows you the list of tutorials. Basically, you will understand why there is "Tree" in FeedbackTree.

<div style:"text-align: center;"> 
  <img style: src="/FeedbackTree/assets/media/tutorials_screenshot.png" width="250"/> 
</div>

<br>

### Getting Started

You can download the **starter** project from here. It includes:

- The flows that were built in the previous tutorials with some adjustements that we will discuss below.
- The XML layout to display the screen above.

### The Tutorials Flow

In the **tutorialsroot** package under the **flows** package, create a new Kotlin file called **TutorialsFlow.kt**, then. add the code below to it:

```kotlin
import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.advance
import com.feedbacktree.tutorials.flows.counter.CounterFlow
import com.feedbacktree.tutorials.flows.login.LoginFlow

data class State(
    val tutorials: List<Tutorial> = listOf(
        Tutorial.Counter, Tutorial.Login
    ), // 1
    val selectedTutorial: Tutorial? = null // 2
)

sealed class Event {
    data class SelectedTutorial(val tutorial: Tutorial) : Event()
    object CompletedTutorial : Event()
}

val TutorialsFlow = Flow<Unit, State, Event, Nothing, Any>(
    id = "TutorialsFlow",
    initialState = { State() },
    stepper = { state, event ->
        when (event) {
            is Event.SelectedTutorial -> state.copy(selectedTutorial = event.tutorial).advance()
            Event.CompletedTutorial -> state.copy(selectedTutorial = null).advance()
        }
    },
    feedbacks = listOf(),
    render = { state, context ->
        when (state.selectedTutorial) {
            null -> TutorialsScreen(state, context.sink) // 3
	          // 4
            Tutorial.Counter -> context.renderChild(CounterFlow, onResult = { 
                context.sendEvent(Event.CompletedTutorial)
            })
            Tutorial.Login -> context.renderChild(input = "", flow = LoginFlow, onResult = {
                context.sendEvent(Event.CompletedTutorial)
            })
        }
    }
)

data class TutorialsScreen(
    val state: State,
    val sink: (Event) -> Unit
) {

    data class Row(
        val title: String,
        val onClickEvent: Event
    )

    val rows: List<Row> = state.tutorials.mapIndexed { index, demo ->
            Row(
                title = "${index + 1}. ${demo.title}",
                onClickEvent = Event.SelectedTutorial(demo)
            )
        }
}
```

Here's the breakdown of the code:

1. The state holds the list of tutorials that the user can select. 

2. The `selectedTutorial` becomes not null when the user selects a tutorial. You can take a closer look at the stepper for more details.

3. When the no tutorial is selected render the `TutorialsScreen`. FeedbackTree will take care of inflating the Tutorials layout.

4. When a tutorial is selected, you call `context.renderChild` to kickstart a child flow:

   - `context.renderChild` returns what the `render` function of the child flow returns. For instance,
     - The `render` method of `CounterFlow`, produces `CounterScreens`. Thus, the `context.renderChild(CounterFlow...)`will return `CounterScreens`
     - The render method of the `LoginFlow` produces `LoginScreens`.Thus, the `context.renderChild(..., LoginFlow, ...)` will return `LoginScreens`
     - The returned screen by the subflows is returned back by the `render` method of the `TutorialsFlow`. FeedbackTree will display in these case either the `Counter` layout or the the `Login` layout.
   - `context.renderChild` takes the input of the Flow:
     - The input of the `CounterFlow` is Unit, you don't have to specify anything when calling `context.renderChild` or even starting the flow from an Activity just like in the Counter tutorial.
     - The input of the LoginFlow is a String, the email to start the flow with. Calling `context.renderChild` requires you to specify the `input`.
   - We have seen in previous tutorials, that a flow can complete using the `endFlow/endFlowWith()` methods. the `onResult` block is used to collect the output `flow`. 
     - When a flow completes, you send an `Event.CompletedTutorial` to update the state through `context.sendEvent`. 
     - Once the `CompletedTutorial` event is sent, the state gets updated and the `render` is called again. This time, the `selectedTutorial` is null, and, the `TutorialsScreen` is displayed.

   

### The Tutorials UI

Create a new file called **TutorialsLayoutBinder.kt** and add the code below it:

```kotlin
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.tutorials.R
import com.feedbacktree.utils.actionBarTitle
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

val TutorialsLayoutBinder = LayoutBinder.create(
    layoutId = R.layout.root_menu,
    sink = TutorialsScreen::sink
) { view ->

    view.actionBarTitle = "Feedback Tree Tutorials" // 1

   // 2
    val adapter = TutorialsAdapter()
    val recyclerView: RecyclerView = view.findViewById(R.id.tutorialsRecyclerView)
    recyclerView.layoutManager = LinearLayoutManager(view.context)
    recyclerView.adapter = adapter

    bind { screen ->
        subscriptions = listOf(
            screen.map { it.rows }.subscribe { adapter.updateDataSet(it) } // 3
        )
        events = listOf(
            adapter.events // 4
        )
    }
}

private class TutorialsAdapter(private var rows: List<TutorialsScreen.Row> = listOf()) :
    RecyclerView.Adapter<TutorialsAdapter.ViewHolder>() {

    private val _events = PublishSubject.create<Event>()
    val events: Observable<Event> = _events

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(android.R.layout.simple_list_item_1, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val row = rows[position]
        viewHolder.textView.text = row.title
        viewHolder.itemView.setOnClickListener {
            _events.onNext(row.onClickEvent) // 5
        }
    }

    override fun getItemCount(): Int {
        return rows.size
    }

    fun updateDataSet(rows: List<TutorialsScreen.Row>) {
        this.rows = rows
        notifyDataSetChanged()
    }
}
```

Let's take a closer look at the code above:

1. You can set the `ActionBar` title property through this extension. Basically this `View` extension method does nothing but access the Activity from the `view.context` like this `this.context as AppCompatActivity).supportActionBar`
2. This code sets the `Adapter` to the recycler view.
3. Every time a new `TutorialsScreen` is produced by the flow, the `rows` data classes will be sent to the adapter and the `recyclerView` will refresh its content.
4. Forward adapter events like user clicks to the flow.
5. You can notice that the `TutorialsScreen.Row` data class has the `onClickEvent` already there, so when the user clicks on a row that event is just ready to be sent to the flow.

##### App View Registry

In the **tutorials** package create a Kotlin file called **AppViewRegistry.kt** and add the code below to it:

```kotlin
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import com.feedbacktree.tutorials.flows.counter.CounterLayoutBinder
import com.feedbacktree.tutorials.flows.login.LoginLayoutBinder
import com.feedbacktree.tutorials.flows.tutorialsroot.TutorialsLayoutBinder

val appViewRegistry = ViewRegistry(
    TutorialsLayoutBinder,
    CounterLayoutBinder,
    LoginLayoutBinder
)
```

You can notice that we created a variable called `appViewRegistry` that contains the different `Binders` that we created so far.
You need to add all the `LayoutBinders` into one registry that we are going to use when we start the "root" flow from the activity.

##### Starting The Root Flow

In the **MainActivity.kt**, let's paste this code and run the app:

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.feedbacktree.flow.core.startFlow
import com.feedbacktree.tutorials.flows.tutorialsroot.TutorialsFlow
import io.reactivex.disposables.Disposable

class MainActivity : AppCompatActivity() {

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposable = startFlow(flow = TutorialsFlow, viewRegistry = appViewRegistry)
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            disposable?.dispose()
            disposable = null
        }
    }
}
```

### Handling the Back 

When you were trying the app, you tapped on the **Counter** tutorial, you were able to go inside and try the counter but when you tapped on the back button, the app exited instead of going back to the list of Tutorials. Handling backward navigation is going to be the focus of this section:

Let's start by adding overriding `onBackPressed()` of the `MainActivity` .

```kotlin

class MainActivity : AppCompatActivity() {
		...
  
    override fun onBackPressed() {
        if (!HandlesBack.Helper.onBackPressed(findViewById(R.id.workflow_layout))) {
            super.onBackPressed()
        }
    }
}
```

This code will be responsible of forwarding the back-presses into FeedbackTree's rendering layer. If FeedbackTree, doesn't capture the back-presses, the `super.onBackPressed()` is called, and, the activity terminates.

##### Capturing back-presses in the Counter flow

FeedbackTree provides a way to capture back-presses through a `View` extension. In the **CounterLayoutBinder.kt** modify the binding `events` add the necssary code to listen the to back-presses:

```kotlin
events = listOf(
    incrementButton.clicks().map { Event.Increment },
    decrementButton.clicks().map { Event.Decrement },
    view.backPresses().map { Event.BackPressed }, // <- Add this code
)
```

The code obviously won't compile because we need to add a `BackPressed` Event and update the **CounterFlow** accordingly:

```kotlin
sealed class Event {
    object Increment : Event()
    object Decrement : Event()
    object BackPressed : Event() // <- Add this code
}
```

And,

```kotlin
val CounterFlow = Flow<Unit, State, Event, Unit, CounterScreen>(
    initialState = { State(counter = 0) },
    stepper = { state, event ->
        when (event) {
            Event.Increment -> state.copy(
                counter = state.counter + 1
            ).advance()
            Event.Decrement -> state.copy(
                counter = max(0, state.counter - 1)
            ).advance()
            Event.BackPressed -> endFlow() // <- Add this code to end the flow
        }
    },
    feedbacks = listOf(),
    render = { state, context ->
        CounterScreen(state, context.sink)
    }
)
```

Now, if you run the app and tap on back when you are in the **Counter** tutorial, the app will go back to the list of tutorials.
If you tap back again one more time, the app will exit because no view is subscrived to the back-presses and `HandlesBack.Helper.onBackPressed` that we added to the `MainActivity` will return false.

### Where to Go From Here?

In this tutorial you learned how to start sub-flows, to handle back presses, and, to use the view registry with multiple screens.
We recommend to visit the [How-to Guides](../howtoguides/summary.md) to learn the different FeedbackTree use cases and if you want to understand more about FeedbackTree internels the [Reference](../reference/summary.md) guide is a good start.

