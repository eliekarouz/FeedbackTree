package com.feedbacktree.tutorials.managers

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

object AuthenticationManager {
    fun login(email: String, password: String): Observable<Boolean> {
        // simulate network call here...
        return Observable.just(true)
            .delaySubscription(2, TimeUnit.SECONDS) // To simulate a network call
    }
}