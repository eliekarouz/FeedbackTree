/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.feedbacktree.example.flows.root.RootFlow
import com.feedbacktree.example.flows.testexamples.fullscreen.ModalsFlow
import com.feedbacktree.example.flows.testexamples.testExamplesViewRegistry
import com.feedbacktree.example.ui.appViewRegistry
import com.feedbacktree.flow.core.startFlow
import com.feedbacktree.flow.core.startModalsFlow
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.flow.ui.views.modals.DialogRegistry
import io.reactivex.disposables.Disposable

class MainActivity : AppCompatActivity() {

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposable = startFlow(RootFlow, onOutput = {
            finish()
        }, viewRegistry = appViewRegistry)
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
