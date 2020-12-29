/*
 * Created by eliek on 12/28/2020
 * Copyright (c) 2020 eliekarouz. All rights reserved.
 */

package com.feedbacktree.utils

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

/**
 * An EditText that can be used for two-way binding. Using the standard EditText will create infinite
 * loops.
 * The reason is that modifying the text programmatically would trigger "text changes" events.
 * So when the EditText.text is updated, a new State will be generated, a new Screen will be produced from the new state,
 * and the EditText in the layout will be updated which will emit again another "text changes" event.
 */
class FTEditText : AppCompatEditText {

    private val textWatchers = mutableListOf<TextWatcher>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        android.R.attr.editTextStyle
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun addTextChangedListener(watcher: TextWatcher?) {
        watcher?.let(textWatchers::add)
        super.addTextChangedListener(watcher)
    }

    override fun removeTextChangedListener(watcher: TextWatcher?) {
        watcher?.let(textWatchers::remove)
        super.removeTextChangedListener(watcher)
    }

    var text: String
        get() = super.getText()?.toString() ?: ""
        set(value) {
            updateText(value)
        }

    /**
     * Updates an EditText without firing text-change events.
     * This is needed for two-way binding
     */
    private fun updateText(newText: String) {
        if (newText == super.getText()?.toString()) return
        val watchers = textWatchers.toMutableList()
        watchers.forEach(::removeTextChangedListener)
        super.getText()?.replace(0, super.getText()!!.length, newText)
        watchers.forEach(::addTextChangedListener)
    }
}