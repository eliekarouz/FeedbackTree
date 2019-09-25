package com.feedbacktree.flow.ui.core.modals

interface Modal

data class ModalContainerScreen<BaseScreen : Any, ModalScreen : Modal>(
    val baseScreen: BaseScreen,
    val modals: List<ModalScreen>
) {

    constructor(baseScreen: BaseScreen, modal: ModalScreen?) : this(
        baseScreen,
        listOfNotNull(modal)
    )
}