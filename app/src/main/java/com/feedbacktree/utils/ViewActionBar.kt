/*
 * Created by eliek on 12/28/2020
 * Copyright (c) 2020 eliekarouz. All rights reserved.
 */

package com.feedbacktree.utils

import android.view.View
import androidx.appcompat.widget.Toolbar
import com.feedbacktree.tutorials.R

val View.actionBar: Toolbar?
    get() = this.findViewById(R.id.ft_toolbar)

var View.actionBarTitle: String
    get() = actionBar?.title.toString()
    set(value) {
        actionBar?.title = value
    }
