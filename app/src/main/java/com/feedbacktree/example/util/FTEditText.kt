package com.feedbacktree.example.util

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import io.reactivex.Observable

/**
 * An EditText that can be used for two-way binding. Using the standard EditText will create infinite
 * loops.
 * The reason is that modifying the text programmatically would trigger "text changes" events.
 * So when the EditText.text is updated, a new State will be generated, a new ViewModel will be produced from the new state,
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


    /**
     * Updates an EditText without firing text-change events.
     * This is needed for two-way binding
     */
    fun updateText(newText: String) {
        if (newText == text.toString()) return
        val watchers = textWatchers.toMutableList()
        watchers.forEach(::removeTextChangedListener)
        text?.replace(0, text!!.length, newText)
        watchers.forEach(::addTextChangedListener)
    }
}

fun FTEditText.ftTextChanges(): Observable<String> {
    return Observable.create { emitter ->
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                emitter.onNext(s.toString())
            }

            override fun beforeTextChanged(
                text: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        addTextChangedListener(textWatcher)

        emitter.setCancellable {
            removeTextChangedListener(textWatcher)
        }
    }
}