@file:Suppress("DEPRECATION")

package com.feedbacktree.flow.ui.views

import android.view.View
import androidx.annotation.LayoutRes
import com.feedbacktree.flow.core.Bindings
import com.feedbacktree.flow.core.Feedback
import com.feedbacktree.flow.ui.views.core.ViewBinding
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import io.reactivex.Observable

class LayoutBinderBuilder<ScreenT : Any, EventT : Any>(
    val feedbacks: MutableList<Feedback<ScreenT, EventT>> = mutableListOf()
) {
    fun bind(bindings: (Observable<ScreenT>) -> (Bindings<EventT>)) {
        feedbacks.add(com.feedbacktree.flow.core.bind(bindings))
    }

    fun <QueryT : Any> react(
        query: (ScreenT) -> QueryT?,
        areEqual: (QueryT, QueryT) -> Boolean,
        effects: (QueryT) -> Observable<EventT>
    ) {
        feedbacks.add(com.feedbacktree.flow.core.react(query, areEqual, effects))
    }

    fun <QueryT : Any> react(
        query: (ScreenT) -> QueryT?,
        effects: (QueryT) -> Observable<EventT>
    ) {
        feedbacks.add(com.feedbacktree.flow.core.react(query, effects))
    }

    fun <QueryT : Any> reactSet(
        query: (ScreenT) -> Set<QueryT>,
        effects: (QueryT) -> Observable<EventT>
    ) {
        feedbacks.add(com.feedbacktree.flow.core.reactSet(query, effects))
    }

    fun <QueryT : Any, QueryID> react(
        queries: (ScreenT) -> Map<QueryID, QueryT>,
        effects: (initial: QueryT, queryObservable: Observable<QueryT>) -> Observable<EventT>
    ) {
        feedbacks.add(com.feedbacktree.flow.core.react(queries, effects))
    }
}

object LayoutBinder {
    @Suppress("FunctionName")
    inline fun <reified ScreenT : Any, reified EventT : Any> create(
        @LayoutRes layoutId: Int,
        noinline sink: (ScreenT) -> (EventT) -> Unit,
        noinline build: (LayoutBinderBuilder<ScreenT, EventT>).(View) -> Unit
    ): ViewBinding<ScreenT> = create(
        layoutId = layoutId,
        sink = sink,
        build = { view, _ ->
            build(view)
        }
    )


    @Suppress("FunctionName")
    inline fun <reified ScreenT : Any, reified EventT : Any> create(
        @LayoutRes layoutId: Int,
        noinline sink: (ScreenT) -> (EventT) -> Unit,
        noinline build: (LayoutBinderBuilder<ScreenT, EventT>).(View, ViewRegistry) -> Unit
    ): ViewBinding<ScreenT> {
        return LayoutRunner.bind(
            layoutId = layoutId,
            constructor = { view, viewRegistry ->
                val builder = LayoutBinderBuilder<ScreenT, EventT>()
                build(builder, view, viewRegistry)

                object : LayoutRunner<ScreenT, EventT> {
                    override fun feedbacks(): List<Feedback<ScreenT, EventT>> = builder.feedbacks
                }
            },
            sink = sink
        )
    }
}

