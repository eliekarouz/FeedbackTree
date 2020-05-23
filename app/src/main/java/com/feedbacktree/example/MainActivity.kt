/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.feedbacktree.example.flows.testexamples.fullscreen.FullScreenModalFlow
import com.feedbacktree.example.flows.testexamples.testExamplesViewRegistry
import com.feedbacktree.flow.core.startModalsFlow
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.flow.ui.views.modals.DialogRegistry
import io.reactivex.disposables.Disposable

class MainActivity : AppCompatActivity() {

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        disposable = startFlow(RootFlow, onOutput = {
//            finish()
//        }, viewRegistry = appViewRegistry)

        disposable = startModalsFlow(
            Unit,
            FullScreenModalFlow,
            testExamplesViewRegistry,
            DialogRegistry.registry()
        )
            .subscribe()
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
