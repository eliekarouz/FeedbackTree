/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.core.modals

interface Modal

data class ModalContainerScreen<BaseViewModelT : Any, ModalT : Modal>(
    val baseScreen: BaseViewModelT,
    val modals: List<ModalT>
) {

    constructor(baseScreen: BaseViewModelT, modal: ModalT?) : this(
        baseScreen,
        listOfNotNull(modal)
    )
}