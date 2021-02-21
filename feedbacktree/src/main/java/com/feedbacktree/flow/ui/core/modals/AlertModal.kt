/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.core.modals

data class AlertModal(
    val buttons: Map<Button, String> = emptyMap(),
    val message: String = "",
    val title: String = "",
    val cancelable: Boolean = true,
    val contentScreen: Any? = null,
    val onEvent: (Event) -> Unit
) : Modal {
    enum class Button {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    sealed class Event {
        data class ButtonClicked(val button: Button) : Event()

        object Canceled : Event()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AlertModal

        return buttons == other.buttons &&
                message == other.message &&
                title == other.title &&
                cancelable == other.cancelable
    }

    override fun hashCode(): Int {
        var result = buttons.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + cancelable.hashCode()
        return result
    }

    companion object {
        /**
         * DSL constructor for Alert Modals
         * @return AlertModal
         */
        operator fun <Event> invoke(
            sink: (Event) -> Unit,
            build: AlertModalBuilder<Event>.() -> Unit
        ): AlertModal {
            val builder = AlertModalBuilder<Event>().apply {
                build()
            }
            return AlertModal(
                buttons = builder.buttons.map {
                    it.key to it.value.first
                }.toMap(),
                title = builder.title,
                message = builder.message,
                cancelable = builder.cancelEvent != null,
                contentScreen = builder.contentScreen,
                onEvent = { alertModalEvent ->
                    when (alertModalEvent) {
                        is AlertModal.Event.ButtonClicked -> {
                            builder.buttons[alertModalEvent.button]?.let {
                                sink(it.second)
                            }
                        }
                        AlertModal.Event.Canceled -> {
                            builder.cancelEvent?.let(sink)
                        }
                    }
                }

            )
        }

        class AlertModalBuilder<Event>(
            internal val buttons: MutableMap<Button, Pair<String, Event>> = mutableMapOf(),
            var title: String = "",
            var message: String = "",
            var cancelEvent: Event? = null,
            var contentScreen: Any? = null,
        ) {

            fun positive(title: String, event: Event) {
                buttons[Button.POSITIVE] = title to event
            }

            fun negative(title: String, event: Event) {
                buttons[Button.NEGATIVE] = title to event
            }

            fun neutral(title: String, event: Event) {
                buttons[Button.NEUTRAL] = title to event
            }
        }
    }
}

