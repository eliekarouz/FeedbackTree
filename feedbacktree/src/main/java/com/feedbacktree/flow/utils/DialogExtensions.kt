/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.utils

import android.app.Dialog

internal fun Dialog.logAndShow(tag: String) {
    logVerbose("Showing dialog $tag")
    show()
}