/*
 * Created by eliek on 12/28/2020
 * Copyright (c) 2020 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials.flows.tutorialsroot

enum class Tutorial {
    Counter,
    Login;

    val title: String
        get() = when (this) {
            Counter -> "Counter"
            Login -> "Login"
        }
}