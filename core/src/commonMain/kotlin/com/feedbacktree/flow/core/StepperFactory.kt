/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import kotlin.reflect.KClass

/**
 * [StepperFactory.create] allows you to build a [Stepper] ([StateT], [EventT]) -> [Step] using DSL syntax.
 *
 * Below is a simple example on how to use this DSL to build [Stepper]s. There is also an example on
 * you would have done it without this DSL.
 * Future enhancements can be made to this DSL is to auto generate documentation (mermaid diagrams)
 * of the corresponding state machine.
 *
 * The example below describes a flow used to select a User.
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
 *     object SelectedUser(val user: User): Event()
 * }
 *
 * // Usage of the DSL
 * val stepper: (State, Event) -> Step<State, User> = StepperBuilder.create {
 *     state<State.Loading> {
 *         on<Event.Loaded> { event ->
 *             // You get a compile error if you don't return a state here
 *             State.Loaded(event.data)
 *                .enterState()
 *         }
 *     }
 *     state<State.Loaded> {
 *         on<Event.Refresh> {
 *             // Note that users in the Loaded State are directly accessible inside this lambda
 *             // So you can simply write:
 *             print(users)
 *             State.Loading
 *                .enterState()
 *         }
 *         on<Event.SelectedUser> { event ->
 *              endFlowWith(event.user)
 *         }
 *     }
 * }
 *
 *
 * // Equivalent Version With Boilerplate
 * fun reduce(state: State, event: Event): Step<State, User> {
 *     when (state) {
 *         State.Loading -> {
 *             if (event is Event.Loaded) {
 *                 return State.Loaded(event.data)
 *                     .refreshState()
 *             }
 *         }
 *         is State.Loaded -> {
 *             if (event is Event.Refresh) {
 *                 // Note that users in the Loaded State are NOT directly accessible inside this block
 *                 print(state.users)
 *                 return State.Loading
 *                      .refreshState()
 *             }
 *             if (event is Event.SelectedUser) {
 *                  return endFlowWith(event.user)
 *             }
 *         }
 *     }
 *     println("WARNING: Transition not found")
 *     return state
 * }
 * </code>
 * </pre>
 *
 * @param StateT
 * @param EventT
 */
class StepperFactory<StateT : Any, EventT : Any, OutputT : Any> private constructor() {

    val stateTransitions =
        mutableMapOf<Matcher<StateT, StateT>, SubStateSteppers<out StateT, EventT, OutputT>>()

    inner class SubStateSteppers<S : StateT, EventT : Any, OutputT : Any>(val transitions: MutableMap<Matcher<EventT, EventT>, S.(EventT) -> Step<StateT, OutputT>> = mutableMapOf()) {
        inline fun <reified E : EventT> on(noinline transition: S.(E) -> Step<StateT, OutputT>) {
            val matcher =
                Matcher<EventT, E>(E::class)
            transitions[matcher] = { event -> transition(event as E) }
        }

        fun transition(state: StateT, event: EventT): Step<StateT, OutputT>? {
            val transition = transitions.filter {
                it.key.matches(event)
            }.values.firstOrNull()
            @Suppress("UNCHECKED_CAST")
            return transition?.invoke(state as S, event)
        }
    }

    inline fun <reified S : StateT> state(build: SubStateSteppers<S, EventT, OutputT>.() -> Unit) {
        val substateSteppers = SubStateSteppers<S, EventT, OutputT>()
        val matcher =
            Matcher<StateT, S>(S::class)
        stateTransitions[matcher] = substateSteppers
        substateSteppers.build()
    }

    class Matcher<T : Any, out U : T>(private val clazz: KClass<U>) {
        fun matches(state: T): Boolean = clazz.isInstance(state)
    }

    companion object {
        fun <StateT : Any, EventT : Any, OutputT : Any> create(build: StepperFactory<StateT, EventT, OutputT>.() -> Unit): (StateT, EventT) -> Step<StateT, OutputT> {
            val stepper =
                StepperFactory<StateT, EventT, OutputT>()
            stepper.build()
            return { state, event ->
                val transition = stepper.stateTransitions.filter {
                    it.key.matches(state)
                }.values.firstOrNull()
                transition?.transition(state, event) ?: run {
                    Step.State<StateT, OutputT>(state = state) // stay on the same state
                }
            }
        }
    }
}