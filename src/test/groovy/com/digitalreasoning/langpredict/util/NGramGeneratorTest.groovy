/**
 * 
 */
package com.digitalreasoning.langpredict.util

import spock.lang.Specification

class NGramGeneratorTest extends Specification {

    def "should return an empty result list for an empty string"() {
        expect:
        new NGramGenerator().generate(input) == []

        where:
        input << ["", "   ", " \n \t "]
    }

    def "should return expected results for a single character"() {
        expect:
        new NGramGenerator().generate(input).toSet() == output.toSet()

        where:
        input | output
        "A"   | [" A", "A", "A ", " A "]
        "你"  | [" 乘", "乘", "乘 ", " 乘 "]
    }

    def "should return expected results for two characters"() {
        expect:
        new NGramGenerator().generate(input).toSet() == output.toSet()

        where:
        input | output
        "AB"  | [" A", "A", " AB", "AB", "AB ", "B", "B "]
        "你會" | [" 乘", "乘", " 乘亞", "乘亞", "乘亞 ", "亞", "亞 "]
    }

    def "should return expected results for three characters"() {
        expect:
        new NGramGenerator().generate(input).toSet() == output.toSet()

        where:
        input   | output
        "ABC"   | [] // All caps words longer than 2 letters are excluded
        "abc"   | [" a", "a", " ab", "ab", "b", "abc", "c", "bc", "bc ", "c", "c "]
        "你會不" | [" 乘", "乘", " 乘亞", "乘亞", "亞", "乘亞三", "三", "亞三", "亞三 ", "三", "三 "]
    }

    def "should return expected results for longer strings"() {
        expect:
        new NGramGenerator().generate(input).toSet() == output.toSet()

        where:
        input                                                                              | output
        "THIS TEXT CONTAINS ONLY ALL-CAPS WORDS WITH MORE THAN TWO CHARACTERS"             | []

        "THIS TEXT CONTAINS ALL-CAPS WORDS OF *MOSTLY* MORE THAN TWO CHARACTERS"           | [
                " O", "O", " OF", "OF", "OF ", "F", "F "
        ]

        "  abcde fg h i  j  k  l  mn  o p   q  are CAPS Right? "                           | [
                " R", " Ri", " a", " ab", " ar", " f", " fg", " h", " h ", " i", " i ", " j", " j ", " k", " k ",
                " l", " l ", " m", " mn", " o", " o ", " p", " p ", " q", " q ", "R", "Ri", "Rig", "a", "ab", "abc",
                "ar", "are", "b", "bc", "bcd", "c", "cd", "cde", "d", "de", "de ", "e", "e ", "f", "fg", "fg ",
                "g", "g ", "gh", "ght", "h", "h ", "ht", "ht ", "i", "i ", "ig", "igh", "j", "j ", "k", "k ",
                "l", "l ", "m", "mn", "mn ", "n", "n ", "o", "o ", "p", "p ", "q", "q ", "r", "re", "re ", "t", "t "
        ]

        "This is yet another (really???) TEST of ***   THE    *** ngram generation code!!" | [
                " T", " Th", " a", " an", " c", " co", " g", " ge", " i", " is", " n", " ng", " o", " of", " r",
                " re", " y", " ye", "T", "Th", "Thi", "a", "al", "all", "am", "am ", "an", "ano", "at", "ati", "c",
                "co", "cod", "d", "de", "de ", "e", "e ", "ea", "eal", "en", "ene", "er", "er ", "era", "et", "et ",
                "f", "f ", "g", "ge", "gen", "gr", "gra", "h", "he", "her", "hi", "his", "i", "io", "ion", "is",
                "is ", "l", "ll", "lly", "ly", "ly ", "m", "m ", "n", "n ", "ne", "ner", "ng", "ngr", "no", "not",
                "o", "od", "ode", "of", "of ", "on", "on ", "ot", "oth", "r", "r ", "ra", "ram", "rat", "re", "rea",
                "s", "s ", "t", "t ", "th", "the", "ti", "tio", "y", "y ", "ye", "yet"
        ]
    }

}