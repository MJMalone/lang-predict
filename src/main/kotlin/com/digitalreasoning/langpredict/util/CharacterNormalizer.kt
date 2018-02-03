package com.digitalreasoning.langpredict.util

import java.lang.Character.UnicodeBlock.*
import java.util.regex.Pattern

object CharacterNormalizer {
    private val LATIN1_EXCLUDED: String = Messages.getString("NGram.LATIN1_EXCLUDE")

    /**
     * Character normalization function. Takes the original character as its parameter and returns the
     * normalized character.
     * @param ch the original character
     * @return the normalized character
     */
    fun normalize(ch: Char): Char = when (Character.UnicodeBlock.of(ch)) {
        BASIC_LATIN                 -> if (ch !in 'a'..'z' && ch !in 'A'..'Z') ' ' else ch
        LATIN_1_SUPPLEMENT          -> if (LATIN1_EXCLUDED.indexOf(ch) >= 0) ' ' else ch
        LATIN_EXTENDED_B            -> when (ch) { // normalization for Romanian
                                           '\u0219' -> '\u015f' // Small S with comma below => with cedilla
                                           '\u021b' -> '\u0163' // Small T with comma below => with cedilla
                                           else -> ch
                                       }
        GENERAL_PUNCTUATION         -> ' '
        ARABIC                      -> if (ch == '\u06cc') '\u064a' else ch // Farsi yeh => Arabic yeh
        LATIN_EXTENDED_ADDITIONAL   -> if (ch >= '\u1ea0') '\u1ec3' else ch
        HIRAGANA                    -> '\u3042'
        KATAKANA                    -> '\u30a2'
        BOPOMOFO, BOPOMOFO_EXTENDED -> '\u3105'
        CJK_UNIFIED_IDEOGRAPHS      -> cjk_map[ch] ?: ch
        HANGUL_SYLLABLES            -> '\uac00'
        else                        -> ch
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

    private val NORMALIZED_VI_CHARS = arrayOf(
            "0300", "0301", "0303", "0309", "0323"
            ).map { Messages.getString("NORMALIZED_VI_CHARS_${it}") }
    private val TO_NORMALIZE_VI_CHARS = Messages.getString("TO_NORMALIZE_VI_CHARS")
    private val DMARK_CLASS = Messages.getString("DMARK_CLASS")
    private val ALPHABET_WITH_DMARK = Pattern.compile("([${TO_NORMALIZE_VI_CHARS}])([${DMARK_CLASS}])")

    /**
     * CJK Kanji Normalization Mapping
     */
    private val CJK_CLASS = arrayOf(
            "1_0", "1_2","1_4", "1_8", "1_11", "1_12", "1_13", "1_14", "1_16", "1_18", "1_22", "1_27", "1_29",
            "1_31", "1_35", "2_0", "2_1", "2_4", "2_9", "2_10", "2_11", "2_12", "2_13", "2_15", "2_16", "2_18",
            "2_21", "2_22", "2_23", "2_28", "2_29", "2_30", "2_31", "2_32", "2_35", "2_36", "2_37", "2_38", "3_1",
            "3_2", "3_3", "3_4", "3_5", "3_8", "3_9", "3_11", "3_12", "3_13", "3_15", "3_16", "3_18", "3_19",
            "3_22", "3_23", "3_27", "3_29", "3_30", "3_31", "3_32", "3_35", "3_36", "3_37", "3_38", "4_0", "4_9",
            "4_10", "4_16", "4_17", "4_18", "4_22", "4_24", "4_28", "4_34", "4_39", "5_10", "5_11", "5_12", "5_13",
            "5_14", "5_18", "5_26", "5_29", "5_34", "5_39", "6_0", "6_3", "6_9", "6_10", "6_11", "6_12", "6_16",
            "6_18", "6_20", "6_21", "6_22", "6_23", "6_25", "6_28", "6_29", "6_30", "6_32", "6_34", "6_35", "6_37",
            "6_39", "7_0", "7_3", "7_6", "7_7", "7_9", "7_11", "7_12", "7_13", "7_16", "7_18", "7_19", "7_20",
            "7_21", "7_23", "7_25", "7_28", "7_29", "7_32", "7_33", "7_35", "7_37"
            ).map { Messages.getString("NGram.KANJI_${it}") }

    private val cjk_map: Map<Char, Char> = CJK_CLASS.flatMap { cjk_list ->
        cjk_list.toCharArray().map { it to cjk_list[0] }
    }.toMap()

}
