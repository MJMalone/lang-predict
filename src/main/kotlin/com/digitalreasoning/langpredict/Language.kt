package com.digitalreasoning.langpredict

import java.util.*

/**
 * [Language] is to store the detected language.
 * [Detector.getProbabilities] returns an [ArrayList] of [Language]s.
 *
 * @see Detector.getProbabilities
 * @author Nakatani Shuyo
 */
class Language(var lang: String?, var prob: Double) {
    override fun toString(): String {
        return if (lang == null) "" else lang + ":" + prob
    }
}
