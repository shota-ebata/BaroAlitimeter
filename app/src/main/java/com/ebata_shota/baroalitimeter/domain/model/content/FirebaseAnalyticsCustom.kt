package com.ebata_shota.baroalitimeter.domain.model.content

import android.os.Bundle
import com.ebata_shota.baroalitimeter.domain.model.content.FirebaseAnalyticsCustomParam.ACTION_NAME
import com.ebata_shota.baroalitimeter.domain.model.content.FirebaseAnalyticsCustomParam.ACTION_TYPE

/**
 * @see FirebaseAnalyticsCustomEvent.USER_ACTION
 */
enum class UserActionEvent(
    val param: Bundle.() -> Unit,
) {
    EditTemperature(
        param = {
            putString(ACTION_NAME, "EditTemperature")
        }
    ),
    EditAltitude(
        param = {
            putString(ACTION_NAME, "EditAltitude")
        }
    ),
    DoneEditTemperature(
        param = {
            putString(ACTION_NAME, "DoneEditTemperature")
        }
    ),
    DoneEditAltitude(
        param = {
            putString(ACTION_NAME, "DoneEditAltitude")
        }
    ),
    CancelEditTemperatureByButton(
        param = {
            putString(ACTION_NAME, "CancelEditTemperature")
            putString(ACTION_TYPE, "Button")
        }
    ),
    CancelEditAltitudeByButton(
        param = {
            putString(ACTION_NAME, "CancelEditAltitude")
            putString(ACTION_TYPE, "Button")
        }
    ),
    CancelEditTemperatureByOnBackPressedCallback(
        param = {
            putString(ACTION_NAME, "CancelEditTemperature")
            putString(ACTION_TYPE, "OnBackPressedCallback")
        }
    ),
    CancelEditAltitudeByOnBackPressedCallback(
        param = {
            putString(ACTION_NAME, "CancelEditAltitude")
            putString(ACTION_TYPE, "OnBackPressedCallback")
        }
    ),
    UndoTemperature(
        param = {
            putString(ACTION_NAME, "UndoTemperature")
        }
    ),
    UndoAltitude(
        param = {
            putString(ACTION_NAME, "UndoAltitude")
        }
    ),
    DismissedUndoTemperature(
        param = {
            putString(ACTION_NAME, "DismissedUndoTemperature")
        }
    ),
    DismissedUndoAltitude(
        param = {
            putString(ACTION_NAME, "DismissedUndoAltitude")
        }
    )
}

object FirebaseAnalyticsCustomEvent {
    const val USER_ACTION = "user_action"
}

object FirebaseAnalyticsCustomParam {
    const val ACTION_NAME = "action_name"
    const val ACTION_TYPE = "action_type"
}

