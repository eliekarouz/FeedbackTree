/*
 * Created by eliek on 12/28/2020
 * Copyright (c) 2020 eliekarouz. All rights reserved.
 */

package com.feedbacktree.utils

import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity

val View.actionBar: ActionBar?
    get() = (this.context as AppCompatActivity).supportActionBar

var View.actionBarTitle: String
    get() = actionBar?.title.toString() ?: ""
    set(value) {
        actionBar?.title = value
    }
