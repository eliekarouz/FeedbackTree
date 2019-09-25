/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import com.feedbacktree.flow.utils.logVerbose

/**
 * [Reducer.create] allows you to build a reducer (State, Event) -> State using DSL syntax.
 *
 * Below is a simple example on how to use this DSL to build reducers. There is also an example on
 * you would have done it without this DSL.
 * Future enhancements can be made to this DSL is to auto generate documentation (mermaid diagrams)
 * of the corresponding state machine.
 *
 * <pre>
 * <code>
 * private data class User(val name: String)
 *
 * private sealed class State {
 *     object Loading : State()
 *     data class Loaded(val users: List<User>) : State()
 * }
 *
 * private sealed class Event {
 *     data class Loaded(val data: List<User>) : Event()
 *     object Refresh : Event()
 * }
 *
 * private fun usageOfDSL() {
 *     val reduce: (State, Event) -> State = Reducer.create {
 *         state<State.Loading> {
 *             on<Event.Loaded> { event ->
 *                 State.Loaded(event.data) // You get a compile error if you don't return a state here
 *             }
 *         }
 *         state<State.Loaded> {
 *             on<Event.Refresh> {
 *                 // Note that users in the Loaded State are directly accessible inside this lambda
 *                 // So you can simply write:
 *                 print(users)
 *                 State.Loading
 *             }
 *         }
 *     }
 *
 *     // reduce function is usually automatically called from RxJava code.
 *     // But here's a how to call it manually.
 *
 *     val state: State = State.Loading
 *     val state1 = reduce(state, Event.Loaded(listOf()))
 *     val state2 = reduce(state1, Event.Refresh)
 * }
 *
 *
 * fun equivalentVersionWithBoilerplate() {
 *     fun reduce(state: State, event: Event): State {
 *         when (state) {
 *             State.Loading -> {
 *                 if (event is Event.Loaded) {
 *                     return State.Loaded(event.data)
 *                 }
 *             }
 *             is State.Loaded -> {
 *                 if (event is Event.Refresh) {
 *                     // Note that users in the Loaded State are NOT directly accessible inside this block
 *                     print(state.users)
 *                     return State.Loading
 *                 }
 *             }
 *         }
 *         println("WARNING: Transition not found")
 *         return state
 *     }
 *
 *     // reduce function is usually automatically called from RxJava code.
 *     // But here's a how to call it manually.
 *
 *     val state: State = State.Loading
 *     val state1 = reduce(state, Event.Loaded(listOf()))
 *     val state2 = reduce(state1, Event.Refresh)
 * }
 * </code>
 * </pre>
 *
 * @param State
 * @param Event
 */
class Reducer<State, Event> private constructor() {

    val stateTransitions = mutableMapOf<Matcher<State, State>, SubStateReducers<out State, Event>>()

    inner class SubStateReducers<S : State, Event>(val transitions: MutableMap<Matcher<Event, Event>, S.(Event) -> State> = mutableMapOf()) {
        inline fun <reified E : Event> on(noinline transition: S.(E) -> State) {
            val matcher = Matcher<Event, E>(E::class.java)
            transitions[matcher] = { event -> transition(event as E) }
        }

        fun transition(state: State, event: Event): State? {
            val transition = transitions.filter {
                it.key.matches(event)
            }.values.firstOrNull()
            @Suppress("UNCHECKED_CAST")
            return transition?.invoke(state as S, event)
        }
    }

    inline fun <reified S : State> state(build: SubStateReducers<S, Event>.() -> Unit) {
        val substateReducers = SubStateReducers<S, Event>()
        val matcher = Matcher<State, S>(S::class.java)
        stateTransitions[matcher] = substateReducers
        substateReducers.build()
    }

    class Matcher<T, out U : T>(private val clazz: Class<U>) {
        fun matches(state: T): Boolean = clazz.isInstance(state)
    }

    companion object {
        fun <State, Event> create(build: Reducer<State, Event>.() -> Unit): (State, Event) -> State {
            val reducer = Reducer<State, Event>()
            reducer.build()
            return { state, event ->
                val transition = reducer.stateTransitions.filter {
                    it.key.matches(state)
                }.values.firstOrNull()
                transition?.transition(state, event) ?: run {
                    logVerbose("WARNING: Transition not found")
                    state // stay on the same state
                }
            }
        }
    }
}