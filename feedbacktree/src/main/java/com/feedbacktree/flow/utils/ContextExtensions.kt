/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.utils

import android.content.Context
import android.view.Display
import android.view.WindowManager

val Context.windowManager: WindowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

val Context.display: Display get() = windowManager.defaultDisplay
