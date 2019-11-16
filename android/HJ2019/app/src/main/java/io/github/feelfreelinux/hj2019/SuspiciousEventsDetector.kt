package io.github.feelfreelinux.hj2019

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

data class Scenario(val keywordsList: List<List<String>>, val hint: String)


val SCENARIOS = listOf(
    Scenario(
        listOf(
            listOf("My", "school"),
            listOf("My", "address"),
            listOf("home"),
            listOf("My", "street"),
            listOf("live", "I")
        ), "Never share your personal information on the Internet \uD83D\uDE31 \uD83D\uDE31 \uD83D\uDE31"),
    Scenario(
        listOf(
            listOf("Union", "Western", "transfer"),
            listOf("transfer", "money"),
            listOf("transfer", "cash"),
            listOf("paypal", "money")
        ), "\uD83D\uDED1 Stop! Ask your parents if this payment is safe!"
    )
)

val SCENARIOS_OUTPUT_ANALYZED = listOf(
    Scenario(
        listOf(
            listOf("Your", "school"),
            listOf("Your", "address"),
            listOf("Your", "home"),
            listOf("Your", "street"),
            listOf("live", "You"),
            listOf("Your", "age")
        ), "It's not safe to share your personal information on the Internet \uD83D\uDE15"),

    Scenario(
        listOf(
            listOf("You", "moron"),
            listOf("hate", "you")
        ), "This person seems to be aggressive \uD83D\uDC7A, it's a good idea to block them and talk with your parents."
    ),

    Scenario(
        listOf(
            listOf("Bank", "number"),
            listOf("login", "account"),
            listOf("login", "password"),
            listOf("password", "account")
        ), "This person may be a fraud \uD83E\uDD14. Discuss it with your parents!"
    ),
    Scenario(
        listOf(
            listOf("sms", "confirm")
        ), "This may be a scam \uD83D\uDE21! If you give your phone number, you can lose your money."
    ),
    Scenario(
        listOf(
            listOf("Union", "Western", "transfer"),
            listOf("transfer", "money"),
                    listOf("transfer", "cash")
    ), "STOP \uD83D\uDED1! Ask your parents if this payment is safe!"
    )
)


class SuspiciousEventsDetector {
    companion object {
        var lastScenario: String? = null
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
            if (id == "com.android.vending:id/right_button" && data == "Install") {
                var hint = "Ask your parent if this app is proper for you."
                if (lastScenario != hint) {
                    lastScenario = hint
                    val intent = Intent(context, FloatingBalloon::class.java)

                    intent.putExtra("HINT", hint)
                    ContextCompat.startForegroundService(context, intent)
                    return
                }
                return
            }

            if (id == "com.android.chrome:id/url_bar") {
                if (data.startsWith("http:")) {
                    var hint = "This site is not secure. Please, avoid it"
                    if (lastScenario != hint) {
                        lastScenario = hint
                        val intent = Intent(context, FloatingBalloon::class.java)

                        intent.putExtra("HINT", hint)
                        ContextCompat.startForegroundService(context, intent)
                        return
                    }
                }
            }

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
            val splittedWords = data.split(" ").map { word -> word.toLowerCase() } // i live
            Log.v("TAG", splittedWords.joinToString(separator = ",") )
            scenario.keywordsList.forEach { keywords ->
                // keywords = I, live
                if (splittedWords.containsAll(keywords.map { it.toLowerCase() })) {
                        Log.v("TAG", "Last scenario: " + lastScenario ?: "")
                        if (!(lastScenario?.contains( scenario.hint) ?: false)){
                            lastScenario = scenario.hint
                            return true
                        }
                    }


            }
            return false
        }

        fun List<String>.containsAllJaro(list: List<String> ): Boolean {
            list.forEach {
                if (!testIfSuits(it)) {
                    return false
                }
            }

            return true
        }

        fun List<String>.testIfSuits(other: String): Boolean {
            forEach {

                if (JaroDistance.calculate(it.toLowerCase(), other.toLowerCase()) > 0.9) {
                    return true
                }
            }
            return false
        }
    }
}