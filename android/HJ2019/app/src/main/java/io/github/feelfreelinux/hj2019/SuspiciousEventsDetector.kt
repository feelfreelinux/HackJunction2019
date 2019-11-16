package io.github.feelfreelinux.hj2019

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class SuspiciousEventsDetector {
    companion object {

        fun eventTyped(data: String) {

        }

        fun textViewFound(id: String, data: String, context: Context) {
            if (id == "com.instagram.android:id/direct_text_message_text_view") {
                val intent = Intent(context, FloatingBalloon::class.java)

                intent.putExtra("HINT", "Instagram view found in tree")
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }
}