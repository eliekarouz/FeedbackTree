package com.feedbacktree.flow.utils

import android.app.Dialog

fun Dialog.logAndShow(tag: String) {
    logVerbose("Showing dialog $tag")
    show()
}