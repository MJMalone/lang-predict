package com.digitalreasoning.langpredict.profilegen

import spock.lang.Specification

class TagExtractorTest extends Specification {

    def "testTagExtractor1"() {
        setup:
        final TagExtractor extractor = new TagExtractor("target", 0)

        expect:
        extractor.target_ == "target"
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
        final TagExtractor extractor = new TagExtractor("abc", 0)

        when:
        extractor.setTag("")
        then:
        extractor.tag == ""

        when:
        extractor.setTag(null)
        then:
        thrown(IllegalArgumentException)
    }

    def "testAdd"() {
        setup:
        final TagExtractor extractor = new TagExtractor("xyz", 0)

        when:
        extractor.add("")
        extractor.add(null)

        then:
        noExceptionThrown()
    }

    def "testCloseTag"() {
        setup:
        final TagExtractor extractor = new TagExtractor("123", 0)

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
        final LanguageProfileGenerator profileGenerator = new LanguageProfileGenerator("en")

        // normal
        extractor.setTag("abstract")
        extractor.add("This is a sample text.")
        profileGenerator.update(extractor.closeTag())
        def profile = profileGenerator.generate()

        then:
        extractor.count() == 1
        profile.ngramCounts[0] == 17  // Thisisasampletext
        profile.ngramCounts[1] == 22  // _T, Th, hi, ...
        profile.ngramCounts[2] == 17  // _Th, Thi, his, ...

        when:
        // too short
        extractor.setTag("abstract")
        extractor.add("sample")
        profileGenerator.update(extractor.closeTag())

        then:
        extractor.count() == 1

        when:
        // other tags
        extractor.setTag("div")
        extractor.add("This is a sample text which is enough long.")
        profileGenerator.update(extractor.closeTag())

        then:
        extractor.count() == 1
    }
}
