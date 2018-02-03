package com.digitalreasoning.langpredict

import java.util.*

/**
 * Language Profiles for [DetectorFactory]
 *
 *
 * for multiple profiles.
 *
 * @see DetectorFactory
 *
 * @author Nakatani Shuyo
 */
class DetectorProfiles {
    val wordLangProbMap: HashMap<String, DoubleArray> = HashMap()
    val langlist: ArrayList<String> = ArrayList()
    var seed: Long? = null

    /**
     * @return languages list in profiles
     */
    val langList: List<String>
        get() = Collections.unmodifiableList(langlist)

    fun addProfile(profile: LanguageProfile, index: Int, langsize: Int) {
        val lang = profile.name
        if (langlist.contains(lang)) {
            throw LangDetectException("duplicate the same language profile")
        }

        langlist.add(lang)
        for (word in profile.frequencyMap.keys) {
            if (!wordLangProbMap.containsKey(word)) {
                wordLangProbMap.put(word, DoubleArray(langsize))
            }
            val length = word.length
            if (length in 1..3) {
                val prob = profile.frequencyMap[word]!!.toDouble() / profile.ngramCounts[length - 1]
                wordLangProbMap[word]!![index] = prob
            }
        }
    }

    /**
     * Clear profiles
     */
    fun clear() {
        langlist.clear()
        wordLangProbMap.clear()
    }

    /**
     * Set random seed for coherent detection.
     *
     * Hence language-detection draws random features to reduce bias,
     * it will return incoherent language whenever it detects.
     * When you need coherent detection, you should set a fixed number into seed.
     *
     * @param seed random seed to set
     */
    fun setSeed(seed: Long) {
        this.seed = seed
    }
}
