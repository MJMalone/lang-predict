package com.digitalreasoning.langpredict

import spock.lang.Specification

class DetectorTest extends Specification {

    private static final String JSON_LANG1 = "{\"freq\":{\"A\":3,\"B\":6,\"C\":3,\"AB\":2,\"BC\":1,\"ABC\":2,\"BBC\":1,\"CBA\":1},\"n_words\":[12,3,4],\"name\":\"lang1\"}";
    private static final String JSON_LANG2 = "{\"freq\":{\"A\":6,\"B\":3,\"C\":3,\"AA\":3,\"AB\":2,\"ABC\":1,\"ABA\":1,\"CAA\":1},\"n_words\":[12,5,3],\"name\":\"lang2\"}";

    def "testFactoryFromJsonString"() {
        setup:
        final ArrayList<String> profiles = new ArrayList<String>()
        profiles.add(JSON_LANG1)
        profiles.add(JSON_LANG2)
        def prof = DetectorFactory.@Companion.loadProfile(profiles)
        final List<String> langList = prof.langList

        expect:
        langList.size() == 2
        langList.get(0) == "lang1"
        langList.get(1) == "lang2"
    }

    def "testFactoryFromJsonStringForMultiProfiles"() {
        setup:
        final ArrayList<String> profiles = new ArrayList<String>()
        profiles.add(JSON_LANG1)
        profiles.add(JSON_LANG2)
        final DetectorProfiles profiles2 = DetectorFactory.@Companion.loadProfile(profiles)
        final Detector detector = DetectorFactory.@Companion.create(profiles2)
        detector.append("A")
        String lang = detector.detect()

        expect:
        lang == "lang2"
    }
}