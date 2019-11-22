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
 * @param StateT
 * @param EventT
 */
class Reducer<StateT, EventT> private constructor() {

    val stateTransitions = mutableMapOf<Matcher<StateT, StateT>, SubStateReducers<out StateT, EventT>>()

    inner class SubStateReducers<S : StateT, EventT>(val transitions: MutableMap<Matcher<EventT, EventT>, S.(EventT) -> StateT> = mutableMapOf()) {
        inline fun <reified E : EventT> on(noinline transition: S.(E) -> StateT) {
            val matcher = Matcher<EventT, E>(E::class.java)
            transitions[matcher] = { event -> transition(event as E) }
        }

        fun transition(state: StateT, event: EventT): StateT? {
            val transition = transitions.filter {
                it.key.matches(event)
            }.values.firstOrNull()
            @Suppress("UNCHECKED_CAST")
            return transition?.invoke(state as S, event)
        }
    }

    inline fun <reified S : StateT> state(build: SubStateReducers<S, EventT>.() -> Unit) {
        val substateReducers = SubStateReducers<S, EventT>()
        val matcher = Matcher<StateT, S>(S::class.java)
        stateTransitions[matcher] = substateReducers
        substateReducers.build()
    }

    class Matcher<T, out U : T>(private val clazz: Class<U>) {
        fun matches(state: T): Boolean = clazz.isInstance(state)
    }

    companion object {
        fun <StateT, EventT> create(build: Reducer<StateT, EventT>.() -> Unit): (StateT, EventT) -> StateT {
            val reducer = Reducer<StateT, EventT>()
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