package io.github.feelfreelinux.hj2019

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

data class Scenario(
    val keywordsList: List<List<String>>, val id: String, val packageNameDependent: String? = null
)


val SCENARIOS = listOf(
    Scenario(listOf(), "http"),
    Scenario(
        listOf(
            listOf("free", "download")
        ), "fileDownload", "com.android.chrome"
    ),
    Scenario(
        listOf(
            listOf("free", "mp3"),
            listOf("download", "mp3"),
            listOf("download", "audio"),
            listOf("free", "songs")
        ), "mp3Download", "com.android.chrome"
    ),
    Scenario(
        listOf(
            listOf("watch", "video"),
            listOf("download", "movie"),
            listOf("download", "video"),
            listOf("free", "video")
        ), "videoDownload", "com.android.chrome"
    ),
    Scenario(
        listOf(
            listOf("download", "apk"),
            listOf("free", "apk"),
            listOf("download", "app"),
            listOf("free", "coins"),
            listOf("free", "vip")
        ), "appDownload", "com.android.chrome"
    )
)

val SCENARIOS_OUTPUT_ANALYZED = listOf(
    Scenario(
        listOf(
            listOf("bank", "transfer"),
            listOf("send", "money"),
            listOf("sign", "contract"),
            listOf("transfer", "cash"),
            listOf("transfer", "money"),
            listOf("transfer", "deposit"),
            listOf("send", "credit", "card"),
            listOf("credit", "card", "numbers"),
            listOf("transferwise"),
            listOf("paypal"),
            listOf("western", "transfer"),
            listOf("western", "union"),
            listOf("westernunion")
        ), "possibleThief"
    ),
    Scenario(
        listOf(
            listOf("where", "live"),
            listOf("where", "study"),
            listOf("where", "school"),
            listOf("where", "work"),
            listOf("home", "address"),
            listOf("share", "location"),
            listOf("send", "location")
        ), "infoLocation"
    ),
    Scenario(
        listOf(
            listOf("how", "old"),
            listOf("your", "age")
        ), "infoAge"
    ),
    Scenario(
        listOf(
            listOf("you", "moron"),
            listOf("you", "stupid"),
            listOf("hate", "you"),
            listOf("kill", "you"),
            listOf("you", "dumb"),
            listOf("you", "retard"),
            listOf("kill", "yourself"),
            listOf("shut", "up")
        ), "bully"
    )


)


class SuspiciousEventsDetector {
    companion object {
        var lastScenario: String? = null
        fun eventTyped(data: String, context: Context, packageName: String) {
            Log.v("ASD", packageName)

            SCENARIOS.forEach { scenario ->
                if (checkKeywords(scenario, data)) {
                    if (scenario.packageNameDependent == null || scenario.packageNameDependent == packageName) {
                        showHint(scenario.id, context)
                    }
                }
            }
        }

        fun textViewFound(id: String, data: String, context: Context, packageName: String) {
            Log.v("ASD", packageName)
            if (id == "com.android.vending:id/right_button" && data == "Install") {
                showHint("googlePlay", context)
            }

            if (id == "com.android.chrome:id/url_bar") {
                if (data.startsWith("http:")) {
                    showHint("http", context)

                }
            }

            if (id == "com.instagram.android:id/direct_text_message_text_view" || id == "com.zhiliaoapp.musically:id/eyx") {
                SCENARIOS_OUTPUT_ANALYZED.forEach { scenario ->
                    if (checkKeywords(scenario, data)) {
                        if (scenario.packageNameDependent == null || scenario.packageNameDependent == packageName) {
                            showHint(scenario.id, context)
                        }
                        return
                    }
                }
            }
        }

        private fun checkKeywords(scenario: Scenario, data: String): Boolean {
            val words = data.toLowerCase().split(' ').toList()
            // free, ''

            return scenario.keywordsList.any { // free, download
                kw ->
                Log.v("TAG", kw.joinToString(", "))
                kw.all {// free
                    Log.v("TAG", ".all - im in " + it)
                    words.testIfSuits(it)
                }
            }
        }

/*
        private fun checkKeywords(scenario: Scenario, data: String): Boolean {
            val splittedWords = data.split(" ").map { word -> word.toLowerCase() } // i live
            Log.v("TAG", splittedWords.joinToString(separator = ","))
            scenario.keywordsList.forEach { keywords ->
                // keywords = I, live
                if (splittedWords.containsAll(keywords.map { it.toLowerCase() })) {
                    return true
                }


            }
            return false
        }*/

        /*fun List<String>.containsAllJaro(list: List<String>): Boolean {
            list.forEach {
                if (!testIfSuits(it)) {
                    return false
                }
            }

            return true
        }*/

        fun List<String>.testIfSuits(other: String): Boolean {
            forEach {

                if (JaroDistance.calculate (it.toLowerCase(),  other.toLowerCase()) > 0.9 ){
                    return true
                }
            }
            return false
        }

        fun showHint(scenarioId: String, context: Context) {
            if (lastScenario != scenarioId) {
                lastScenario = scenarioId
                val intent = Intent(context, FloatingBalloon::class.java)

                intent.putExtra("SCENARIO_ID", scenarioId)
                ContextCompat.startForegroundService(context, intent)
                return
            }
        }
    }
}