package com.digitalreasoning.langpredict.util

const val NGRAM_SIZE = 3

class NGramGenerator {
    fun generate(text: String): List<String> = text
            .map { CharacterNormalizer.normalize(it) }
            .joinToString("")
            .split(' ')
            .filter { it.length < 3 || it.any { !it.isUpperCase() } }
            .flatMap { getGrams(it) }

    private fun getGrams(word: String): List<String> = when (word.length) {
        0 -> emptyList()
        1 -> listOf(word, " $word", "$word ", " $word ")
        2 -> listOf(word.substring(0,1), word.substring(1,2), " ${word[0]}", "${word[1]} ", word, " $word", "$word ")
        3 -> listOf(
                word.substring(0,1), word.substring(1,2), word.substring(2,3),          // 3 unigrams (a,b,c)
                " ${word[0]}", word.substring(0,2), word.substring(1,3), "${word[2]} ", // 4 bigrams (" a", "ab", "bc", "c ")
                " ${word.substring(0,2)}", word, "${word.substring(1,3)} "              // 3 trigrams (" ab", "abc", "bc "
        )
        else -> getInnerGrams(word) +
                " ${word[0]}" + " ${word[0]}${word[1]}" + // 2 leading grams (" a", " ab")
                "${word[word.length-1]} " + "${word[word.length-2]}${word[word.length-1]} " // 2 trailing grams ("z ", "yz ")
    }

    // Only called on words of length > 3
    private fun getInnerGrams(word: String): List<String> =
            (3..word.length)
                    .flatMap { end ->
                        listOf( word.substring(end - 1, end),
                                word.substring(end - 2, end),
                                word.substring(end - 3, end))
                    } + word.substring(0,1) + word.substring(1,2) + word.substring(0,2)
}

internal class OLD_NGram {

    private var grams: StringBuffer = StringBuffer(" ")
    private var capitalword: Boolean = false

    /**
     * Append a character into ngram buffer.
     * @param ch
     */
    fun addChar(ch: Char) {
        val normCh = CharacterNormalizer.normalize(ch)
        val lastchar = grams.last()
        if (lastchar == ' ') {
            grams = StringBuffer(" ")
            capitalword = false
            if (normCh == ' ') return
        } else if (grams.length >= NGRAM_SIZE) {
            grams.deleteCharAt(0)
        }
        grams.append(normCh)

        if (Character.isUpperCase(normCh)) {
            if (Character.isUpperCase(lastchar)) capitalword = true
        } else {
            capitalword = false
        }
    }

    /**
     * Get n-Gram
     * @param n length of n-gram
     * @return n-Gram String (null if it is invalid)
     */
    operator fun get(n: Int): String? {
        if (capitalword) return null
        val len = grams.length
        if (n < 1 || n > 3 || len < n) return null
        if (n == 1) {
            val ch = grams[len - 1]
            return if (ch == ' ') null else Character.toString(ch)
        } else {
            return grams.substring(len - n, len)
        }
    }

}
