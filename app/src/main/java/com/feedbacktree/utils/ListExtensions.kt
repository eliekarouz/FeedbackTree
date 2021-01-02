/*
 * Created by eliek on 1/2/2021
 * Copyright (c) 2021 eliekarouz. All rights reserved.
 */

package com.feedbacktree.utils

fun <T> List<T>.update(
    condition: (T) -> Boolean,
    newValue: (oldT: T) -> T
): List<T> {
    return map {
        if (condition(it)) {
            newValue(it)
        } else {
            it
        }
    }
}