package com.feedbacktree.flow.utils

import asOptional
import io.reactivex.Observable
import org.notests.rxfeedback.Optional

/**
 * The returned stream will emit value(T) if value(T) is not null.
 *
 * @param transform method to produce U? for each T emitted.
 * @return
 */
fun <T, U> Observable<T>.collect(transform: (T) -> U?): Observable<U> {
    return this.map { t -> transform(t).asOptional }
        .filter { it is Optional.Some }
        .map { it as Optional.Some; it.data }
}