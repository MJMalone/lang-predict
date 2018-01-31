package com.digitalreasoning.langpredict.util

import java.lang.Character.UnicodeBlock
import java.util.*
import java.util.regex.Pattern

class NGram {

    private var grams_: StringBuffer? = null
    private var capitalword_: Boolean = false

    /**
     * Constructor.
     */
    init {
        grams_ = StringBuffer(" ")
        capitalword_ = false
    }

    /**
     * Append a character into ngram buffer.
     * @param ch
     */
    fun addChar(ch: Char) {
        var result = ch
        result = normalize(result)
        val lastchar = grams_!![grams_!!.length - 1]
        if (lastchar == ' ') {
            grams_ = StringBuffer(" ")
            capitalword_ = false
            if (result == ' ') return
        } else if (grams_!!.length >= N_GRAM) {
            grams_!!.deleteCharAt(0)
        }
        grams_!!.append(result)

        if (Character.isUpperCase(result)) {
            if (Character.isUpperCase(lastchar)) capitalword_ = true
        } else {
            capitalword_ = false
        }
    }

    /**
     * Get n-Gram
     * @param n length of n-gram
     * @return n-Gram String (null if it is invalid)
     */
    operator fun get(n: Int): String? {
        if (capitalword_) return null
        val len = grams_!!.length
        if (n < 1 || n > 3 || len < n) return null
        if (n == 1) {
            val ch = grams_!![len - 1]
            return if (ch == ' ') null else Character.toString(ch)
        } else {
            return grams_!!.substring(len - n, len)
        }
    }

    companion object {
        private val LATIN1_EXCLUDED: String = Messages.getString("NGram.LATIN1_EXCLUDE")
        val N_GRAM = 3
        val cjk_map: HashMap<Char, Char> = HashMap()

        /**
         * Character Normalization
         * @param ch
         * @return Normalized character
         */
        fun normalize(ch: Char): Char {
            var result = ch
            val block = Character.UnicodeBlock.of(result)
            if (block === UnicodeBlock.BASIC_LATIN) {
                if (result < 'A' || result < 'a' && result > 'Z' || result > 'z') result = ' '
            } else if (block === UnicodeBlock.LATIN_1_SUPPLEMENT) {
                if (LATIN1_EXCLUDED.indexOf(result) >= 0) result = ' '
            } else if (block === UnicodeBlock.LATIN_EXTENDED_B) {
                // normalization for Romanian
                if (result == '\u0219') result = '\u015f'  // Small S with comma below => with cedilla
                if (result == '\u021b') result = '\u0163'  // Small T with comma below => with cedilla
            } else if (block === UnicodeBlock.GENERAL_PUNCTUATION) {
                result = ' '
            } else if (block === UnicodeBlock.ARABIC) {
                if (result == '\u06cc') result = '\u064a'  // Farsi yeh => Arabic yeh
            } else if (block === UnicodeBlock.LATIN_EXTENDED_ADDITIONAL) {
                if (result >= '\u1ea0') result = '\u1ec3'
            } else if (block === UnicodeBlock.HIRAGANA) {
                result = '\u3042'
            } else if (block === UnicodeBlock.KATAKANA) {
                result = '\u30a2'
            } else if (block === UnicodeBlock.BOPOMOFO || block === UnicodeBlock.BOPOMOFO_EXTENDED) {
                result = '\u3105'
            } else if (block === UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                cjk_map.get(result)?.let { result = it }
            } else if (block === UnicodeBlock.HANGUL_SYLLABLES) {
                result = '\uac00'
            }
            return result
        }

        /**
         * Normalizer for Vietnamese.
         * Normalize Alphabet + Diacritical Mark(U+03xx) into U+1Exx .
         * @param text
         * @return normalized text
         */
        fun normalize_vi(text: String): String {
            val m = ALPHABET_WITH_DMARK.matcher(text)
            val buf = StringBuffer()
            while (m.find()) {
                val alphabet = TO_NORMALIZE_VI_CHARS.indexOf(m.group(1))
                val dmark = DMARK_CLASS.indexOf(m.group(2)) // Diacritical Mark
                m.appendReplacement(buf, NORMALIZED_VI_CHARS[dmark].substring(alphabet, alphabet + 1))
            }
            if (buf.isEmpty())
                return text
            m.appendTail(buf)
            return buf.toString()
        }

        private val NORMALIZED_VI_CHARS = arrayOf(Messages.getString("NORMALIZED_VI_CHARS_0300"), Messages.getString("NORMALIZED_VI_CHARS_0301"), Messages.getString("NORMALIZED_VI_CHARS_0303"), Messages.getString("NORMALIZED_VI_CHARS_0309"), Messages.getString("NORMALIZED_VI_CHARS_0323"))
        private val TO_NORMALIZE_VI_CHARS = Messages.getString("TO_NORMALIZE_VI_CHARS")
        private val DMARK_CLASS = Messages.getString("DMARK_CLASS")
        private val ALPHABET_WITH_DMARK = Pattern.compile("([" + TO_NORMALIZE_VI_CHARS + "])(["
                + DMARK_CLASS + "])")

        /**
         * CJK Kanji Normalization Mapping
         */
        private val CJK_CLASS = arrayOf(
                Messages.getString("NGram.KANJI_1_0"),
                Messages.getString("NGram.KANJI_1_2"),
                Messages.getString("NGram.KANJI_1_4"),
                Messages.getString("NGram.KANJI_1_8"),
                Messages.getString("NGram.KANJI_1_11"),
                Messages.getString("NGram.KANJI_1_12"),
                Messages.getString("NGram.KANJI_1_13"),
                Messages.getString("NGram.KANJI_1_14"),
                Messages.getString("NGram.KANJI_1_16"),
                Messages.getString("NGram.KANJI_1_18"),
                Messages.getString("NGram.KANJI_1_22"),
                Messages.getString("NGram.KANJI_1_27"),
                Messages.getString("NGram.KANJI_1_29"),
                Messages.getString("NGram.KANJI_1_31"),
                Messages.getString("NGram.KANJI_1_35"),
                Messages.getString("NGram.KANJI_2_0"),
                Messages.getString("NGram.KANJI_2_1"),
                Messages.getString("NGram.KANJI_2_4"),
                Messages.getString("NGram.KANJI_2_9"),
                Messages.getString("NGram.KANJI_2_10"),
                Messages.getString("NGram.KANJI_2_11"),
                Messages.getString("NGram.KANJI_2_12"),
                Messages.getString("NGram.KANJI_2_13"),
                Messages.getString("NGram.KANJI_2_15"),
                Messages.getString("NGram.KANJI_2_16"),
                Messages.getString("NGram.KANJI_2_18"),
                Messages.getString("NGram.KANJI_2_21"),
                Messages.getString("NGram.KANJI_2_22"),
                Messages.getString("NGram.KANJI_2_23"),
                Messages.getString("NGram.KANJI_2_28"),
                Messages.getString("NGram.KANJI_2_29"),
                Messages.getString("NGram.KANJI_2_30"),
                Messages.getString("NGram.KANJI_2_31"),
                Messages.getString("NGram.KANJI_2_32"),
                Messages.getString("NGram.KANJI_2_35"),
                Messages.getString("NGram.KANJI_2_36"),
                Messages.getString("NGram.KANJI_2_37"),
                Messages.getString("NGram.KANJI_2_38"),
                Messages.getString("NGram.KANJI_3_1"),
                Messages.getString("NGram.KANJI_3_2"),
                Messages.getString("NGram.KANJI_3_3"),
                Messages.getString("NGram.KANJI_3_4"),
                Messages.getString("NGram.KANJI_3_5"),
                Messages.getString("NGram.KANJI_3_8"),
                Messages.getString("NGram.KANJI_3_9"),
                Messages.getString("NGram.KANJI_3_11"),
                Messages.getString("NGram.KANJI_3_12"),
                Messages.getString("NGram.KANJI_3_13"),
                Messages.getString("NGram.KANJI_3_15"),
                Messages.getString("NGram.KANJI_3_16"),
                Messages.getString("NGram.KANJI_3_18"),
                Messages.getString("NGram.KANJI_3_19"),
                Messages.getString("NGram.KANJI_3_22"),
                Messages.getString("NGram.KANJI_3_23"),
                Messages.getString("NGram.KANJI_3_27"),
                Messages.getString("NGram.KANJI_3_29"),
                Messages.getString("NGram.KANJI_3_30"),
                Messages.getString("NGram.KANJI_3_31"),
                Messages.getString("NGram.KANJI_3_32"),
                Messages.getString("NGram.KANJI_3_35"),
                Messages.getString("NGram.KANJI_3_36"),
                Messages.getString("NGram.KANJI_3_37"),
                Messages.getString("NGram.KANJI_3_38"),
                Messages.getString("NGram.KANJI_4_0"),
                Messages.getString("NGram.KANJI_4_9"),
                Messages.getString("NGram.KANJI_4_10"),
                Messages.getString("NGram.KANJI_4_16"),
                Messages.getString("NGram.KANJI_4_17"),
                Messages.getString("NGram.KANJI_4_18"),
                Messages.getString("NGram.KANJI_4_22"),
                Messages.getString("NGram.KANJI_4_24"),
                Messages.getString("NGram.KANJI_4_28"),
                Messages.getString("NGram.KANJI_4_34"),
                Messages.getString("NGram.KANJI_4_39"),
                Messages.getString("NGram.KANJI_5_10"),
                Messages.getString("NGram.KANJI_5_11"),
                Messages.getString("NGram.KANJI_5_12"),
                Messages.getString("NGram.KANJI_5_13"),
                Messages.getString("NGram.KANJI_5_14"),
                Messages.getString("NGram.KANJI_5_18"),
                Messages.getString("NGram.KANJI_5_26"),
                Messages.getString("NGram.KANJI_5_29"),
                Messages.getString("NGram.KANJI_5_34"),
                Messages.getString("NGram.KANJI_5_39"),
                Messages.getString("NGram.KANJI_6_0"),
                Messages.getString("NGram.KANJI_6_3"),
                Messages.getString("NGram.KANJI_6_9"),
                Messages.getString("NGram.KANJI_6_10"),
                Messages.getString("NGram.KANJI_6_11"),
                Messages.getString("NGram.KANJI_6_12"),
                Messages.getString("NGram.KANJI_6_16"),
                Messages.getString("NGram.KANJI_6_18"),
                Messages.getString("NGram.KANJI_6_20"),
                Messages.getString("NGram.KANJI_6_21"),
                Messages.getString("NGram.KANJI_6_22"),
                Messages.getString("NGram.KANJI_6_23"),
                Messages.getString("NGram.KANJI_6_25"),
                Messages.getString("NGram.KANJI_6_28"),
                Messages.getString("NGram.KANJI_6_29"),
                Messages.getString("NGram.KANJI_6_30"),
                Messages.getString("NGram.KANJI_6_32"),
                Messages.getString("NGram.KANJI_6_34"),
                Messages.getString("NGram.KANJI_6_35"),
                Messages.getString("NGram.KANJI_6_37"),
                Messages.getString("NGram.KANJI_6_39"),
                Messages.getString("NGram.KANJI_7_0"),
                Messages.getString("NGram.KANJI_7_3"),
                Messages.getString("NGram.KANJI_7_6"),
                Messages.getString("NGram.KANJI_7_7"),
                Messages.getString("NGram.KANJI_7_9"),
                Messages.getString("NGram.KANJI_7_11"),
                Messages.getString("NGram.KANJI_7_12"),
                Messages.getString("NGram.KANJI_7_13"),
                Messages.getString("NGram.KANJI_7_16"),
                Messages.getString("NGram.KANJI_7_18"),
                Messages.getString("NGram.KANJI_7_19"),
                Messages.getString("NGram.KANJI_7_20"),
                Messages.getString("NGram.KANJI_7_21"),
                Messages.getString("NGram.KANJI_7_23"),
                Messages.getString("NGram.KANJI_7_25"),
                Messages.getString("NGram.KANJI_7_28"),
                Messages.getString("NGram.KANJI_7_29"),
                Messages.getString("NGram.KANJI_7_32"),
                Messages.getString("NGram.KANJI_7_33"),
                Messages.getString("NGram.KANJI_7_35"),
                Messages.getString("NGram.KANJI_7_37"))

        init {
            for (cjk_list in CJK_CLASS) {
                val representative = cjk_list[0]
                for (i in 0 until cjk_list.length) {
                    cjk_map.put(cjk_list[i], representative)
                }
            }
        }
    }

}
