/**
 * 
 */
package com.digitalreasoning.langpredict.util

import spock.lang.Specification

class LangProfileTest extends Specification {

    def "testLangProfile"() {
        setup:
        LangProfile profile = new LangProfile()
        expect:
        profile.name == null
    }

    def "testLangProfileStringInt"() {
        setup:
        LangProfile profile = new LangProfile("en")

        expect:
        profile.name == "en"
    }

    def "testAdd"() {
        LangProfile profile = new LangProfile("en")

        when:
        profile.add("a")
        then:
        profile.freq.get("a") == 1

        when:
        profile.add("a")
        then:
        profile.freq.get("a") == 2
        profile.omitLessFreq()
    }

    def "testAddIllegally1"() {
        setup:
        LangProfile profile = new LangProfile() // Illegal ( available for only JSONIC ) but ignore
        profile.add("a") // ignore

        expect:
        profile.freq.get("a") == null
    }

    def "testAddIllegally2"() {
        setup:
        LangProfile profile = new LangProfile("en")
        profile.add("a")
        profile.add("")  // Illegal (string's length of parameter must be between 1 and 3) but ignore
        profile.add("abcd")  // as well

        expect:
        profile.freq.get("a") == 1
        profile.freq.get("") == null
        profile.freq.get("abcd") == null
    }
    
    def "testOmitLessFreq"() {
        setup:
        final LangProfile profile = new LangProfile("en")
        final String[] grams = "a b c \u3042 \u3044 \u3046 \u3048 \u304a \u304b \u304c \u304d \u304e \u304f".split(" ")

        for (int i=0;i<5;++i) {
            for (final String g : grams) {
                profile.add(g)
            }
        }

        profile.add("\u3050")

        expect:
        profile.freq.get("a") == 5
        profile.freq.get("\u3042") == 5
        profile.freq.get("\u3050") == 1

        when:
        profile.omitLessFreq()

        then:
        profile.freq.get("a") == null
        profile.freq.get("\u3042") == 5
        profile.freq.get("\u3050") == null
    }

    def "testOmitLessFreqIllegally"() {
        when:
        LangProfile profile = new LangProfile()
        profile.omitLessFreq()  // ignore

        then:
        noExceptionThrown()
    }

}
