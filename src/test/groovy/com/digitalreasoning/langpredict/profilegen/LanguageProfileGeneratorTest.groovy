/**
 * 
 */
package com.digitalreasoning.langpredict.profilegen

import spock.lang.Specification

class LanguageProfileGeneratorTest extends Specification {

    def "testLangProfile"() {
        setup:
        LanguageProfileGenerator profile = new LanguageProfileGenerator("langname")
        expect:
        profile.name == "langname"
    }

    def "testLangProfileStringInt"() {
        setup:
        LanguageProfileGenerator profile = new LanguageProfileGenerator("en")

        expect:
        profile.name == "en"
    }

    def "testAdd"() {
        LanguageProfileGenerator profileGenerator = new LanguageProfileGenerator("en")

        when:
        profileGenerator.add("a")
        then:
        profileGenerator.generate().frequencyMap.get("a") == 1

        when:
        profileGenerator.add("a")
        then:
        profileGenerator.generate().frequencyMap.get("a") == 2
        profileGenerator.omitLessFreq()
    }

    def "test add with unknown name"() {
        setup:
        LanguageProfileGenerator profileGenerator = new LanguageProfileGenerator("xyz")
        profileGenerator.add("a")

        expect:
        profileGenerator.name == "xyz"
        profileGenerator.generate().frequencyMap.get("a") == 1
    }

    def "testAddIllegally2"() {
        setup:
        LanguageProfileGenerator profileGenerator = new LanguageProfileGenerator("en")
        profileGenerator.add("a")
        profileGenerator.add("")  // Illegal (string's length of parameter must be between 1 and 3) but ignore
        profileGenerator.add("abcd")  // as well

        def map = profileGenerator.generate().frequencyMap

        expect:
        map.get("a") == 1
        map.get("") == null
        map.get("abcd") == null
    }
    
    def "testOmitLessFreq"() {
        setup:
        final LanguageProfileGenerator profileGenerator = new LanguageProfileGenerator("en")
        final String[] grams = "a b c \u3042 \u3044 \u3046 \u3048 \u304a \u304b \u304c \u304d \u304e \u304f".split(" ")

        (1..5).each {
            grams.each { profileGenerator.add(it) }
        }

        profileGenerator.add("\u3050")

        def map1 = profileGenerator.generate().frequencyMap

        expect:
        map1.get("a") == 5
        map1.get("\u3042") == 5
        map1.get("\u3050") == 1

        when:
        profileGenerator.omitLessFreq()

        def map2 = profileGenerator.generate().frequencyMap

        then:
        map2.get("a") == null
        map2.get("\u3042") == 5
        map2.get("\u3050") == null
    }

    def "testOmitLessFreqIllegally"() {
        when:
        LanguageProfileGenerator profile = new LanguageProfileGenerator("x")
        profile.omitLessFreq()  // ignore

        then:
        noExceptionThrown()
    }

}
