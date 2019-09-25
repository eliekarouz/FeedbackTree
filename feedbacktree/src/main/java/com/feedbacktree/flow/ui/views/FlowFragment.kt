package com.feedbacktree.flow.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.feedbacktree.R
import com.feedbacktree.flow.core.Flow
import com.feedbacktree.flow.core.FlowOutput
import com.feedbacktree.flow.core.FlowViewModel
import com.feedbacktree.flow.ui.views.core.HandlesBack
import com.feedbacktree.flow.ui.views.core.ViewRegistry
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject

abstract class FlowFragment<Input, Output> : Fragment() {

    private val disposeBag = CompositeDisposable()

    private val _output = PublishSubject.create<FlowOutput<Output>>()
    val output: Observable<FlowOutput<Output>> = _output

    abstract fun input(): Input
    abstract fun flow(): Flow<Input, *, *, Output, *>
    abstract fun viewRegisry(): ViewRegistry

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return WorkflowLayout(inflater.context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val factory = FlowViewModel.Factory(input(), flow())

        @Suppress("UNCHECKED_CAST")
        val viewModel = ViewModelProviders.of(
            this,
            factory
        )[FlowViewModel::class.java] as FlowViewModel<Input, Output>

        viewModel.output
            .subscribe {
                _output.onNext(it)
            }
            .addTo(disposeBag)

        (view as WorkflowLayout).apply {
            id = R.id.workflow_layout
            start(viewModel.screens, viewRegisry())
        }
    }

    /**
     * If your workflow needs to manage the back button, override [android.app.Activity.onBackPressed]
     * and call this method, and have its views or [LayoutRunner]s use [HandlesBack].
     *
     * e.g.:
     *
     *    override fun onBackPressed() {
     *      val workflowFragment =
     *        supportFragmentManager.findFragmentByTag(MY_WORKFLOW) as? WorkflowFragment<*, *>
     *      if (workflowFragment?.onBackPressed() != true) super.onBackPressed()
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