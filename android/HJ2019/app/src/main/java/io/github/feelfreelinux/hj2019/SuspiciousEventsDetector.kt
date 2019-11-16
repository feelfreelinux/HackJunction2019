package io.github.feelfreelinux.hj2019

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

data class Scenario(val keywords: List<String>, val hint: String)


val SCENARIOS = listOf(
    Scenario(listOf(
        "school",
        "address",
        "home",
        "live"
    ), "Don't show reveal personal information to strangers!")
)

val SCENARIOS_OUTPUT_ANALYZED = listOf(
    Scenario(listOf(
        "school",
        "address",
        "home",
        "live"
    ), "Don't show reveal personal information to strangers!"),
    Scenario(listOf(
        "how old are you",
        "how many years do you have",
        "when have you borned"
    ), "Don't give your age to strangers!"),
    Scenario(listOf(
        "Send",
        "nudes"
    ), "This doesn’t seems good, maybe leave this chat"),
    Scenario(listOf(
        "Are",
        "your",
        "parents",
        "home"
    ), "This is not a question that you are supposed to respond to"),
    Scenario(listOf(
        "Download this for free",
        "candies",
        "diamonds"
    ), "Remember! Do not download anything from weird sites!"),
    Scenario(listOf(
        "Confirm that you are a human via sms"
    ), "This is a scam, if you’ll give your phone number, you can lost your money!"),
    Scenario(listOf(
        "Union",
        "transfer"
    ), "Don't transfer money to strangers! If you want to buy something online - ask your parent")
)


class SuspiciousEventsDetector {
    companion object {

        fun eventTyped(data: String, context: Context) {
            data.split(" ").forEach mainForEach@{
                    splittedWord ->
                SCENARIOS.forEach {
                    scenario ->
                    scenario.keywords.forEach {
                        keyword ->
                        if (JaroDistance.calculate(keyword.toLowerCase(), splittedWord.toLowerCase()) > 0.9) {
                            val intent = Intent(context, FloatingBalloon::class.java)

                            intent.putExtra("HINT", scenario.hint)
                            ContextCompat.startForegroundService(context, intent)
                            return@mainForEach
                        }
                    }
                }
            }
        }

        fun textViewFound(id: String, data: String, context: Context) {
            if (id == "com.instagram.android:id/direct_text_message_text_view" || id ==  "com.zhiliaoapp.musically:id/eyx") {
                data.split(" ").forEach mainForEach@{
                        splittedWord ->
                    SCENARIOS_OUTPUT_ANALYZED.forEach {
                            scenario ->
                        scenario.keywords.forEach {
                                keyword ->
                            if (JaroDistance.calculate(keyword.toLowerCase(), splittedWord.toLowerCase()) > 0.9) {
                                val intent = Intent(context, FloatingBalloon::class.java)

                                intent.putExtra("HINT", scenario.hint)
                                ContextCompat.startForegroundService(context, intent)
                                return@mainForEach
                            }
                        }
                    }
                }
            }
        }
    }
}