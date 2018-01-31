package com.digitalreasoning.langpredict.util

import spock.lang.Specification

class TagExtractorTest extends Specification {

    def "testTagExtractor1"() {
        setup:
        final TagExtractor extractor = new TagExtractor(null, 0)

        expect:
        extractor.target_ == null
        extractor.threshold_ == 0
    }

    def "testTagExtractor2"() {
        setup:
        final TagExtractor extractor = new TagExtractor("abstract", 10)

        expect:
        extractor.target_ == "abstract"
        extractor.threshold_ == 10
    }

    def "testSetTag"() {
        setup:
        final TagExtractor extractor = new TagExtractor(null, 0);

        when:
        extractor.setTag("")
        then:
        extractor.tag == ""

        when:
        extractor.setTag(null)
        then:
        extractor.tag == null
    }

    def "testAdd"() {
        setup:
        final TagExtractor extractor = new TagExtractor(null, 0)

        when:
        extractor.add("")
        extractor.add(null)

        then:
        noExceptionThrown()
    }

    def "testCloseTag"() {
        setup:
        final TagExtractor extractor = new TagExtractor(null, 0)

        when:
        extractor.closeTag()

        then:
        noExceptionThrown()
    }

    def "testNormalScenario"() {
        setup:
        final TagExtractor extractor = new TagExtractor("abstract", 10)

        expect:
        extractor.count() == 0

        when:
        final LangProfile profile = new LangProfile("en")

        // normal
        extractor.setTag("abstract")
        extractor.add("This is a sample text.")
        profile.update(extractor.closeTag())

        then:
        extractor.count() == 1
        profile.n_words[0] == 17  // Thisisasampletext
        profile.n_words[1] == 22  // _T, Th, hi, ...
        profile.n_words[2] == 17  // _Th, Thi, his, ...

        when:
        // too short
        extractor.setTag("abstract")
        extractor.add("sample")
        profile.update(extractor.closeTag())

        then:
        extractor.count() == 1

        when:
        // other tags
        extractor.setTag("div")
        extractor.add("This is a sample text which is enough long.")
        profile.update(extractor.closeTag())

        then:
        extractor.count() == 1
    }

    def "testClear"() {
        setup:
        final TagExtractor extractor = new TagExtractor("abstract", 10)

        when:
        extractor.setTag("abstract")
        extractor.add("This is a sample text.")

        then:
        extractor.buf.toString() == "This is a sample text."
        extractor.tag == "abstract"

        when:
        extractor.clear()

        then:
        extractor.buf.toString() ==  ""
        extractor.tag == null
    }
}
