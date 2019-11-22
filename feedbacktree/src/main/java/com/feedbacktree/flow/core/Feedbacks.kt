/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

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
fun <StateT, QueryT, EventT> react(
    query: (StateT) -> QueryT?,
    areEqual: (lhs: QueryT, rhs: QueryT) -> Boolean = { lhs, rhs -> lhs == rhs },
    effects: (QueryT) -> Observable<EventT>
) = org.notests.rxfeedback.react(
    query = { state: StateT -> query(state).asOptional },
    areEqual = areEqual,
    effects = effects
)
