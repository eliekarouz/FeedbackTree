/*
 * Created by eliek on 12/28/2020
 * Copyright (c) 2020 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.tutorialsroot

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

    view.actionBarTitle = "Feedback Tree Tutorials"

    val adapter = TutorialsAdapter()
    val recyclerView: RecyclerView = view.findViewById(R.id.tutorialsRecyclerView)
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
            _events.onNext(row.onClickEvent)
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