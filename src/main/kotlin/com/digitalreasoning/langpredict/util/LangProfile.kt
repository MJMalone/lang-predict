package com.digitalreasoning.langpredict.util

import java.util.*

/**
 * [LangProfile] is a Language Profile Class.
 * Users don't use this class directly.
 *
 * @author Nakatani Shuyo
 */
class LangProfile {
    var name: String? = null
    var freq = HashMap<String, Int>()
    var n_words = IntArray(NGram.N_GRAM)

    /**
     * Constructor for JSONIC
     */
    constructor() {}

    /**
     * Normal Constructor
     * @param name language name
     */
    constructor(name: String) {
        this.name = name
    }

    /**
     * Add n-gram to profile
     * @param gram
     */
    fun add(gram: String?) {
        if (name == null || gram == null) return    // Illegal
        val len = gram.length
        if (len < 1 || len > NGram.N_GRAM) return   // Illegal
        ++n_words[len - 1]
        freq.put(gram, freq.getOrDefault(gram,0) + 1)
    }

    /**
     * Eliminate below less frequency n-grams and noise Latin alphabets
     */
    fun omitLessFreq() {
        if (name == null) return    // Illegal
        var threshold = n_words[0] / LESS_FREQ_RATIO
        if (threshold < MINIMUM_FREQ) threshold = MINIMUM_FREQ

        val keys = freq.keys
        var roman = 0
        run {
            val i = keys.iterator()
            while (i.hasNext()) {
                val key = i.next()
                val count = freq[key]
                if (count?:0 <= threshold) {
                    n_words[key.length - 1] -= count?:0
                    i.remove()
                } else {
                    if (key.matches("^[A-Za-z]$".toRegex())) {
                        roman += count?:0
                    }
                }
            }
        }

        // roman check
        if (roman < n_words[0] / 3) {
            val keys2 = freq.keys
            val i = keys2.iterator()
            while (i.hasNext()) {
                val key = i.next()
                if (key.matches(".*[A-Za-z].*".toRegex())) {
                    n_words[key.length - 1] -= freq[key]?:0
                    i.remove()
                }
            }

        }
    }

    /**
     * Update the language profile with (fragmented) text.
     * Extract n-grams from text and add their frequency into the profile.
     * @param text (fragmented) text to extract n-grams
     */
    fun update(text: String?) {
        var text: String? = text ?: return
        text = NGram.normalize_vi(text!!)
        val gram = NGram()
        for (i in 0 until text.length) {
            gram.addChar(text[i])
            for (n in 1..NGram.N_GRAM) {
                add(gram[n])
            }
        }
    }

    companion object {
        private val MINIMUM_FREQ = 2
        private val LESS_FREQ_RATIO = 100000
    }
}
