package com.digitalreasoning.langpredict

import com.digitalreasoning.langpredict.profilegen.LanguageProfileGenerator
import spock.lang.Specification

class DetectorProfilesTest extends Specification {

    private static final String TRAINING_EN = "a a a b b c c d e"
    private static final String TRAINING_FR = "a b b c c c d d d"
    private static final String TRAINING_JA = "\u3042 \u3042 \u3042 \u3044 \u3046 \u3048 \u3048"

    private DetectorProfiles profiles = new DetectorProfiles()

    def "setup"() {
        final LanguageProfileGenerator profile_en = new LanguageProfileGenerator("en")
        for (final String w : TRAINING_EN.split(" ")) {
            profile_en.add(w)
        }
        profiles.addProfile(profile_en.generate(), 0, 3)

        final LanguageProfileGenerator profile_fr = new LanguageProfileGenerator("fr")
        for (final String w : TRAINING_FR.split(" ")) {
            profile_fr.add(w)
        }
        profiles.addProfile(profile_fr.generate(), 1, 3)

        final LanguageProfileGenerator profile_ja = new LanguageProfileGenerator("ja")
        for (final String w : TRAINING_JA.split(" ")) {
            profile_ja.add(w)
        }
        profiles.addProfile(profile_ja.generate(), 2, 3)
    }

    def "testDetector1"() throws LangDetectException {
        setup:
        final Detector detect = DetectorFactory.@Companion.create(profiles)
        detect.append("a")

        expect:
        detect.detect() == "en"
    }

    def "testDetector2"() throws LangDetectException {
        setup:
        final Detector detect = DetectorFactory.@Companion.create(profiles)
        detect.append("b d")

        expect:
        detect.detect() == "fr"
    }

    def "testDetector3"() throws LangDetectException {
        setup:
        final Detector detect = DetectorFactory.@Companion.create(profiles)
        detect.append("d e")

        expect:
        detect.detect() == "en"
    }

    def "testDetector4"() throws LangDetectException {
        setup:
        final Detector detect = DetectorFactory.@Companion.create(profiles)
        detect.append("\u3042\u3042\u3042\u3042a")

        expect:
        detect.detect() == "ja"
    }
    
    def "testLangList"() {
        setup:
        final List<String> langList = profiles.getLangList()

        expect:
        langList.size() == 3
        langList.get(0) == "en"
        langList.get(1) == "fr"
        langList.get(2) == "ja"
    }

    def "testLangListException"() throws LangDetectException {
        setup:
        final List<String> langList = profiles.getLangList()

        when:
        langList.add("hoge")

        then:
        thrown(UnsupportedOperationException)
    }

}
