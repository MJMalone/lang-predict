package com.digitalreasoning.langpredict.profilegen

import com.digitalreasoning.langpredict.LanguageProfile
import com.digitalreasoning.langpredict.util.CharacterNormalizer
import com.digitalreasoning.langpredict.util.NGRAM_SIZE
import com.digitalreasoning.langpredict.util.NGramGenerator
import java.util.*

class LanguageProfileGenerator @JvmOverloads constructor(
        val name: String,
        private val minimumFrequency: Int = 2,
        private val lessFrequencyRatio: Int = 100000) {

    val frequencyMap = TreeMap<String, Int>()
    val ngramCounts = List(NGRAM_SIZE){0}.toMutableList()

    fun generate() = LanguageProfile(name, ngramCounts, frequencyMap)

    /**
     * Eliminate below less frequency n-grams and noise Latin alphabets
     */
    fun omitLessFreq() {
        var threshold = ngramCounts[0] / lessFrequencyRatio
        if (threshold < minimumFrequency) threshold = minimumFrequency

        val keys = frequencyMap.keys
        var roman = 0
        run {
            val i = keys.iterator()
            while (i.hasNext()) {
                val key = i.next()
                val count = frequencyMap[key]
                if (count?:0 <= threshold) {
                    ngramCounts[key.length - 1] -= count?:0
                    i.remove()
                } else {
                    if (key.matches("^[A-Za-z]$".toRegex())) {
                        roman += count?:0
                    }
                }
            }
        }

        // roman check
        if (roman < ngramCounts[0] / 3) {
            val keys2 = frequencyMap.keys
            val i = keys2.iterator()
            while (i.hasNext()) {
                val key = i.next()
                if (key.matches(".*[A-Za-z].*".toRegex())) {
                    ngramCounts[key.length - 1] -= frequencyMap[key]?:0
                    i.remove()
                }
            }

        }
    }

    /**
     * Add n-gram to profile
     * @param gram
     */
    fun add(gram: String) {
        val len = gram.length
        if (len < 1 || len > NGRAM_SIZE) return   // Illegal
        ++ngramCounts[len - 1]
        frequencyMap.put(gram, frequencyMap.getOrDefault(gram,0) + 1)
    }

    /**
     * Update the language profile with (fragmented) text.
     * Extract n-grams from text and add their frequency into the profile.
     * @param text (fragmented) text to extract n-grams
     */
    fun update(text: String?) {
        text?.let {
            val normText = CharacterNormalizer.normalize_vi(text)
            NGramGenerator().generate(normText).forEach { add(it) }
        }
    }
}
