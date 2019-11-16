package io.github.feelfreelinux.hj2019

/**
 * modified Jaro Distance implementation from rosettacode.org/wiki/Jaro_distance
 */

object JaroDistance {
    fun calculate(s: String, t: String): Double {
        val s_len = s.length
        val t_len = t.length

        if (s_len == 0 && t_len == 0) return 1.0

        val match_distance = Math.max(s_len, t_len) / 2 - 1

        val s_matches = BooleanArray(s_len)
        val t_matches = BooleanArray(t_len)

        var matches = 0
        var transpositions = 0

        for (i in 0 until s_len) {
            val start = Math.max(0, i - match_distance)
            val end = Math.min(i + match_distance + 1, t_len)
            for (j in start until end) {
                if (t_matches[j]) continue
                if (s[i] != t[j]) continue
                s_matches[i] = true
                t_matches[j] = true
                matches++
                break
            }
        }

        if (matches == 0) return 0.0

        var k = 0
        for (i in 0 until s_len) {
            if (!s_matches[i]) continue
            while (!t_matches[k]) k++
            if (s[i] != t[k]) transpositions++
            k++
        }

        return (matches.toDouble() / s_len +
                matches.toDouble() / t_len +
                (matches.toDouble() - transpositions / 2.0) / matches) / 3.0
    }

}
