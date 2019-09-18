package com.feedbacktree.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.feedbacktree.example.flows.root.RootFlow
import com.feedbacktree.example.ui.appViewRegistry
import com.feedbacktree.flow.startFlow
import com.feedbacktree.flow.ui.HandlesBack
import io.reactivex.disposables.Disposable

class MainActivity : AppCompatActivity() {

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposable = startFlow(RootFlow, onResult = {
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
