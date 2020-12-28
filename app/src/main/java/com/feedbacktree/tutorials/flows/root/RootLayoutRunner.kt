package com.feedbacktree.tutorials.flows.root

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.feedbacktree.flow.ui.views.LayoutBinder
import com.feedbacktree.tutorials.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

val RootLayoutBinder = LayoutBinder.create(
    layoutId = R.layout.root_menu,
    sink = DemoScreen::sink
) { view ->

    (view.context as Activity).title = "FeedbackTree"

    val adapter = RootAdapter()

    val recyclerView: RecyclerView = view.findViewById(R.id.demosRecyclerView)
    recyclerView.layoutManager = LinearLayoutManager(view.context)
    recyclerView.adapter = adapter

    bind { screen ->
        subscriptions = listOf(
            screen.map { it.rows }.subscribe { adapter.updateDataSet(it) }
        )
        events = listOf(
            adapter.events
        )
    }
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