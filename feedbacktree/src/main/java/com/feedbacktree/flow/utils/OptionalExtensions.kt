/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

import org.notests.rxfeedback.Optional

val <T> T?.asOptional: Optional<T>
    get() {
        return if (this != null) {
            Optional.Some(this)
        } else {
            Optional.None()
        }
    }
val <T> Optional<T>.valueOrNull: T?
    get() {
        return when (this) {
            is Optional.Some -> this.data
            is Optional.None -> null
        }
    }
