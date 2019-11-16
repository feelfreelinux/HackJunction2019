package io.github.feelfreelinux.hj2019

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

data class Scenario(val keywordsList: List<List<String>>, val hint: String)


val SCENARIOS = listOf(
    Scenario(
        listOf(
            listOf("school"),
            listOf("address"),
            listOf("home"),
            listOf("live")
        ), "Don't show reveal personal information to strangers!"
    )
)

val SCENARIOS_OUTPUT_ANALYZED = listOf(
    Scenario(
        listOf(
            listOf("school"),
            listOf("address"),
            listOf("home"),
            listOf("live")
        ), "Don't show reveal personal information to strangers!"
    ),
    Scenario(
        listOf(
            listOf("how old are you"),
            listOf("how many years do you have"),
            listOf("when have you borned")
        ), "Don't give your age to strangers!"
    ),
    Scenario(
        listOf(
            listOf("Send"),
            listOf("nudes")
        ), "This doesn’t seems good, maybe leave this chat"
    ),
    Scenario(
        listOf(
            listOf("Are"),
            listOf("your"),
            listOf("parents"),
            listOf("home")
        ), "This is not a question that you are supposed to respond to"
    ),
    Scenario(
        listOf(
            listOf("Download this for free"),
            listOf("candies"),
            listOf("diamonds")
        ), "Remember! Do not download anything from weird sites!"
    ),
    Scenario(
        listOf(
            listOf("Confirm that you are a human via sms")
        ), "This is a scam, if you’ll give your phone number, you can lost your money!"
    ),
    Scenario(
        listOf(
            listOf("Union"),
            listOf("transfer")
        ), "Don't transfer money to strangers! If you want to buy something online - ask your parent"
    )
)


class SuspiciousEventsDetector {
    companion object {

        fun eventTyped(data: String, context: Context) {
            SCENARIOS.forEach { scenario ->
                if (checkKeywords(scenario, data)) {
                    val intent = Intent(context, FloatingBalloon::class.java)

                    intent.putExtra("HINT", scenario.hint)
                    ContextCompat.startForegroundService(context, intent)
                    return
                }
            }
        }

        fun textViewFound(id: String, data: String, context: Context) {
            if (id == "com.instagram.android:id/direct_text_message_text_view" || id == "com.zhiliaoapp.musically:id/eyx") {
                SCENARIOS_OUTPUT_ANALYZED.forEach { scenario ->
                    if (checkKeywords(scenario, data)) {
                        val intent = Intent(context, FloatingBalloon::class.java)

                        intent.putExtra("HINT", scenario.hint)
                        ContextCompat.startForegroundService(context, intent)
                        return
                    }
                }
            }
        }

        private fun checkKeywords(scenario: Scenario, data: String): Boolean {
            val splittedWords = data.split(" ").map { word -> word.toLowerCase() }
            scenario.keywordsList.forEach { keywords ->
                splittedWords.forEach { word ->
                    if (keywords.map { kw -> JaroDistance.calculate(kw.toLowerCase(), word) }.all { d -> d > 0.9 }) {
                        return true
                    }
                }
            }
            return false
        }
    }
}