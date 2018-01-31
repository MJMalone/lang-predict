package com.digitalreasoning.langpredict

import spock.lang.Specification

class LanguageTest extends Specification{

    def "the Language object should work as expected for a null Language"() {
        setup:
        final Language lang = new Language(null, 0)

        expect:
        lang.lang == null
        lang.prob == (double) 0.0
        lang.toString() == ""
    }

    def "the Language object should work as expected for a valid Language"() {
        setup:
        final Language lang = new Language("en", (double)1.0)

        expect:
        lang.lang == "en"
        lang.prob == (double)1.0
        lang.toString() ==  "en:1.0"
    }

}
