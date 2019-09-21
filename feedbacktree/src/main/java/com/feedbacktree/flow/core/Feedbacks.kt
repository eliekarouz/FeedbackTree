package com.feedbacktree.flow.core

import asOptional
import io.reactivex.Observable


/**
 * State: State type of the system.
 * Query: Subset of state used to control the feedback loop.
 *
 * When query returns a non-null value, that value is being passed into `effects` lambda to decide which effects should be performed.
 * In case new `query` is different from the previous one, new effects are calculated by using `effects` lambda and then performed.
 *
 * When `query` returns null, feedback loops doesn't perform any effect.
 *
 * @param query Part of state that controls feedback loop.
 * @param areEqual Part of state that controls feedback loop.
 * @param effects Chooses which effects to perform for certain query result.
 * @return Feedback loop performing the effects.
 */
fun <State, Query, Event> react(
    query: (State) -> Query?,
    areEqual: (lhs: Query, rhs: Query) -> Boolean,
    effects: (Query) -> Observable<Event>
) = org.notests.rxfeedback.react(
    query = { state: State -> query(state).asOptional },
    areEqual = areEqual,
    effects = effects
)
