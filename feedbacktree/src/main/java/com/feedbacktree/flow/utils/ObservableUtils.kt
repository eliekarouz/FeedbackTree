/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.utils

import io.reactivex.Observable

/**
 * The returned stream will emit value(T) if value(T) is not null.
 *
 * @param transform method to produce U? for each T emitted.
 * @return
 */
fun <T, U> Observable<T>.mapNotNull(transform: (T) -> U?): Observable<U> {
    return this.map { t -> transform(t).asOptional }
        .filter { it is Optional.Some }
        .map { it as Optional.Some; it.data }
}