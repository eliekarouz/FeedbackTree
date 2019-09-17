/*
 * Copyright 2019 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedbacktree.flow.core

/**
 * May show a stack of [AlertScreen] over a [baseScreen].
 *
 * @param B the type of [baseScreen]
 */
data class AlertContainerScreen<B : Any>(
    override val baseScreen: B,
    override val modals: List<AlertScreen> = emptyList()
) : HasModals<B, AlertScreen> {
    constructor(
        baseScreen: B,
        alert: AlertScreen
    ) : this(baseScreen, listOf(alert))

    constructor(
        baseScreen: B,
        vararg alerts: AlertScreen
    ) : this(baseScreen, alerts.toList())
}
