package com.feedbacktree.example.flows.root

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.feedbacktree.example.R
import com.feedbacktree.flow.core.Bindings
import com.feedbacktree.flow.core.Feedback
import com.feedbacktree.flow.core.bind
import com.feedbacktree.flow.ui.views.LayoutRunner
import com.feedbacktree.flow.ui.views.core.ViewBinding
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class RootLayoutBinder(view: View) : LayoutRunner<DemoScreen, Event> {

    private val adapter = RootAdapter()
    private val recyclerView: RecyclerView = view.findViewById(R.id.demosRecyclerView)

    init {
        (view.context as Activity).title = "FeedbackTree"

        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = adapter
    }

    override fun feedbacks(): List<Feedback<DemoScreen, Event>> {
        return listOf(bindUI())
    }

    private fun bindUI(): Feedback<DemoScreen, Event> = bind { screen ->
        val subscriptions: List<Disposable> = listOf(
            screen.map { it.rows }.subscribe { adapter.updateDataSet(it) }
        )
        val events: List<Observable<Event>> = listOf(
            adapter.events
        )
        return@bind Bindings(subscriptions, events)
    }

    companion object : ViewBinding<DemoScreen> by LayoutRunner.bind(
        R.layout.root_menu, ::RootLayoutBinder, DemoScreen::sink
    )
}

private class RootAdapter(private var rows: List<DemoScreen.Row> = listOf()) :
    RecyclerView.Adapter<RootAdapter.ViewHolder>() {

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
            _events.onNext(row.onClickEvent)
        }
    }

    override fun getItemCount(): Int {
        return rows.size
    }

    fun updateDataSet(rows: List<DemoScreen.Row>) {
        this.rows = rows
        notifyDataSetChanged()
    }
}