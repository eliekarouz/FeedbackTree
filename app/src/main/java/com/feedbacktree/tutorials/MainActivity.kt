/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.tutorials

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.feedbacktree.flow.core.startFlow
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.tutorials.flows.tutorialsroot.TutorialsFlow
import io.reactivex.disposables.Disposable

class MainActivity : AppCompatActivity() {

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposable = startFlow(flow = TutorialsFlow, viewRegistry = appViewRegistry)
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            disposable?.dispose()
            disposable = null
        }
    }

    override fun onBackPressed() {
        if (!HandlesBack.Helper.onBackPressed(findViewById(R.id.workflow_layout))) {
            super.onBackPressed()
        }
    }
}
