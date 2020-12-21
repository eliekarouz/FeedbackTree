package com.feedbacktree.example.flows.root

data class DemoScreen(
    val state: State,
    val sink: (Event) -> Unit
) {

    data class Row(
        val title: String,
        val onClickEvent: Event
    )

    val rows: List<Row> = state.demoOptions.mapIndexed { index, demo ->
            Row(
                title = "${index + 1}. ${demo.title}",
                onClickEvent = Event.SelectedDemo(demo)
            )
        }
}