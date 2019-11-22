/*
 * Created by eliek on 9/26/2019
 * Copyright (c) 2019 eliekarouz. All rights reserved.
 */

package com.feedbacktree.flow.core

import io.reactivex.Observable

interface IFlow<InputT, OutputT> {
    fun run(input: InputT): Observable<OutputT>
}