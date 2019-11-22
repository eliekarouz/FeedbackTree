/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.views.modals

import android.app.Dialog
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import com.feedbacktree.flow.ui.core.Named
import com.feedbacktree.flow.ui.core.modals.Modal

/**
 * @param extra optional hook to allow [DialogRegistry]s to associate extra data with this dialog,
 * e.g. its content view. Not considered for equality.
 */
data class DialogRef<ModalT : Modal>(
    val modal: ModalT,
    val dialog: Dialog,
    val extra: Any? = null
) {
    internal fun save(): KeyAndBundle {
        val saved = dialog.window!!.saveHierarchyState()
        return KeyAndBundle(
            Named.keyFor(
                modal
            ), saved
        )
    }

    internal fun restore(keyAndBundle: KeyAndBundle) {
        if (Named.keyFor(modal) == keyAndBundle.compatibilityKey) {
            dialog.window!!.restoreHierarchyState(keyAndBundle.bundle)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DialogRef<*>

        if (dialog != other.dialog) return false

        return true
    }

    override fun hashCode(): Int {
        return dialog.hashCode()
    }
}

internal data class KeyAndBundle(
    val compatibilityKey: String,
    val bundle: Bundle
) : Parcelable {
    override fun describeContents(): Int = 0

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int
    ) {
        parcel.writeString(compatibilityKey)
        parcel.writeBundle(bundle)
    }

    companion object CREATOR : Parcelable.Creator<KeyAndBundle> {
        override fun createFromParcel(parcel: Parcel): KeyAndBundle {
            val key = parcel.readString()!!
            val bundle = parcel.readBundle(KeyAndBundle::class.java.classLoader)!!
            return KeyAndBundle(key, bundle)
        }

        override fun newArray(size: Int): Array<KeyAndBundle?> = arrayOfNulls(size)
    }
}

internal class SavedState : View.BaseSavedState {
    constructor(
        superState: Parcelable?,
        dialogBundles: List<KeyAndBundle>
    ) : super(superState) {
        this.dialogBundles = dialogBundles
    }

    constructor(source: Parcel) : super(source) {
        @Suppress("UNCHECKED_CAST")
        this.dialogBundles = mutableListOf<KeyAndBundle>().apply {
            source.readTypedList(
                this,
                KeyAndBundle
            )
        }
    }

    val dialogBundles: List<KeyAndBundle>

    override fun writeToParcel(
        out: Parcel,
        flags: Int
    ) {
        super.writeToParcel(out, flags)
        out.writeTypedList(dialogBundles)
    }

    companion object CREATOR : Parcelable.Creator<SavedState> {
        override fun createFromParcel(source: Parcel): SavedState =
            SavedState(source)

        override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
    }
}