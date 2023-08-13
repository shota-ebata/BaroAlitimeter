package com.ebata_shota.baroalitimeter.domain.extensions

import android.os.Bundle
import com.ebata_shota.baroalitimeter.domain.model.content.FirebaseAnalyticsCustomEvent.USER_ACTION
import com.ebata_shota.baroalitimeter.domain.model.content.UserActionEvent
import com.google.firebase.analytics.FirebaseAnalytics

fun FirebaseAnalytics.logUserActionEvent(userActionEvent: UserActionEvent) {
    val bundle = Bundle().also {
        userActionEvent.param(it)
    }
    this.logEvent(USER_ACTION, bundle)
}