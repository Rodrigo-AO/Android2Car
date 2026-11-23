package com.android2car.service

import android.app.Activity
import android.content.Intent
import androidx.core.content.IntentCompat
import com.android2car.domain.model.ScreenCaptureConsent

class MediaProjectionConsent(val resultCode: Int, val data: Intent) : ScreenCaptureConsent {

    fun writeTo(intent: Intent): Intent =
        intent.putExtra(EXTRA_RESULT_CODE, resultCode).putExtra(EXTRA_DATA, data)

    companion object {
        private const val EXTRA_RESULT_CODE = "com.android2car.extra.RESULT_CODE"
        private const val EXTRA_DATA = "com.android2car.extra.DATA"

        fun fromActivityResult(resultCode: Int, data: Intent?): MediaProjectionConsent? =
            if (resultCode == Activity.RESULT_OK && data != null) MediaProjectionConsent(resultCode, data) else null

        fun readFrom(intent: Intent): MediaProjectionConsent? {
            val data = IntentCompat.getParcelableExtra(intent, EXTRA_DATA, Intent::class.java) ?: return null
            return fromActivityResult(intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED), data)
        }
    }
}
