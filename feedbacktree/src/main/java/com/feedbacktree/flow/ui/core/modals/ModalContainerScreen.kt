/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.ui.core.modals

data class ModalContainerScreen<BaseScreenT : Any, ModalT : Modal>(
    val baseScreen: BaseScreenT,
    val modals: List<ModalT>
) {

    constructor(baseScreen: BaseScreenT, modal: ModalT?) : this(
        baseScreen,
        listOfNotNull(modal)
    )
}