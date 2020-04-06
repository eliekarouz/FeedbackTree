/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.feedbacktree.R
import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.FlowViewModel
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject

/**
 * Extend this class in order to launch a flow inside a fragment.
 * This would allow you to smoothly integrate FeedbackTree to your project.
 */
abstract class FlowFragment<InputT, StateT, OutputT> : Fragment() {

    inner class Parameters(
        val input: InputT,
        val flow: Flow<InputT, StateT, *, OutputT, *>,
        val viewRegistry: ViewRegistry
    )

    private val disposeBag = CompositeDisposable()

    private val _output = PublishSubject.create<OutputT>()

    /**
     * You an subscribe to this variable to collect the output produced by the flow.
     */
    val output: Observable<OutputT> = _output

    /**
     * You will have to provide the different parameters [input][Parameters.input],
     * the [flow][Parameters.flow], and, the [viewRegistry][Parameters.viewRegistry].
     *
     * The fragment will use the different parameters provided to start the flow when the activity is
     * created.
     *
     */
    abstract fun parameters(): Parameters

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return WorkflowLayout(inflater.context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val parameters = parameters()
        val factory = FlowViewModel.Factory(parameters.input, parameters.flow)

        @Suppress("UNCHECKED_CAST")
        val viewModel = ViewModelProviders.of(
            this,
            factory
        )[FlowViewModel::class.java] as FlowViewModel<InputT, StateT, OutputT>

        viewModel.output
            .subscribe {
                _output.onNext(it)
            }
            .addTo(disposeBag)

        (view as WorkflowLayout).apply {
            id = R.id.workflow_layout
            start(viewModel.viewModels, parameters.viewRegistry)
        }
    }

    /**
     * If your workflow needs to manage the back button, override [android.app.Activity.onBackPressed]
     * and call this method, and have its views or [LayoutRunner]s use [HandlesBack].
     *
     * e.g.:
     *
     *    override fun onBackPressed() {
     *      val flowFragment =
     *        supportFragmentManager.findFragmentByTag(MY_WORKFLOW) as? FlowFragment<*, *, *>
     *      if (flowFragment?.onBackPressed() != true) super.onBackPressed()
     *    }
     */
    fun onBackPressed(): Boolean {
        return isVisible && HandlesBack.Helper.onBackPressed(view!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeBag.dispose()
    }
}