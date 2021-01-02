/*
 * Created by eliek on 1/2/2021
 * Copyright (c) 2021 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.resources

import com.feedbacktree.flow.core.Feedback
import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.advance
import com.feedbacktree.flow.core.reactSet
import com.feedbacktree.utils.update
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

val ResourcesFlow = Flow<Unit, State, Event, Unit, FilesScreen>(
    initialState = {
        State(
            fileResources = listOf(
                FileResource("Url1", "Tutorial1.pdf", localUrl = null),
                FileResource("Url2", "Tutorial2.pdf", localUrl = null),
            ),
            resourceUrlsToDownload = setOf()
        )
    },
    stepper = { state, event ->
        when (event) {
            is Event.UserRequestedResource -> state.copy(
                resourceUrlsToDownload = state.resourceUrlsToDownload + event.fileResource.url
            ).advance()
            is Event.LoadedResource -> state.copy(
                resourceUrlsToDownload = state.resourceUrlsToDownload - event.url,
                fileResources = state.fileResources.update(
                    condition = { it.url == event.url },
                    newValue = { it.copy(localUrl = event.localUrl) }
                )
            ).advance()
        }
    },
    feedbacks = listOf(downloadResourcesFeedback()),
    render = { state, context ->
        FilesScreen(state, context.sink)
    }
)

data class FileResource(
    val url: String,
    val name: String,
    val localUrl: String?
)

data class State(
    val fileResources: List<FileResource>,
    val resourceUrlsToDownload: Set<String>
)

sealed class Event {
    data class UserRequestedResource(val fileResource: FileResource) : Event()
    data class LoadedResource(val url: String, val localUrl: String) : Event()
}

data class FilesScreen(
    private val state: State,
    val sink: (Event) -> Unit
) {
    data class Row(
        val name: String,
        val onClick: Event
    )

    val rows: List<Row> = state.fileResources
        .sortedBy { it.name }
        .map { fileResource ->
            Row(fileResource.name, Event.UserRequestedResource(fileResource))
        }
}

// Feedbacks

@Suppress("RemoveExplicitTypeArguments")
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

@Suppress("UNUSED_PARAMETER")
private fun downloadResource(url: String): Observable<String> =
    Observable.just("").delay(2, TimeUnit.SECONDS)
