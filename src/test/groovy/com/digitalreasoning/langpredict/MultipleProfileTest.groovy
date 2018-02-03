/**
 * 
 */
package com.digitalreasoning.langpredict

import com.digitalreasoning.langpredict.profilegen.LanguageProfileGenerator
import spock.lang.Specification

class MultipleProfileTest extends Specification {

    private DetectorProfiles profile1
    private DetectorProfiles profile2

    def "setup"() {
        final ArrayList<String> sample_data1 = new ArrayList<String>()
        final ArrayList<String> sample_data2 = new ArrayList<String>()

        final LanguageProfileGenerator profile_en = new LanguageProfileGenerator("en")
        profile_en.update("This is a pen.")
        final String json_en = LanguageProfileFactory.INSTANCE.toJson(profile_en.generate())
        sample_data1.add(json_en)
        sample_data2.add(json_en)

        final LanguageProfileGenerator profile_it = new LanguageProfileGenerator("it")
        profile_it.update("Sono un studente.")
        final String json_it = LanguageProfileFactory.INSTANCE.toJson(profile_it.generate())
        sample_data1.add(json_it)

        final  LanguageProfileGenerator profile_fr = new LanguageProfileGenerator("fr")
        profile_fr.update("Je suis japonais.")
        final String json_fr = LanguageProfileFactory.INSTANCE.toJson(profile_fr.generate())
        sample_data2.add(json_fr)

        profile1 = DetectorFactory.@Companion.loadProfile(sample_data1)
        profile2 = DetectorFactory.@Companion.loadProfile(sample_data2)
    }

    def "testMultiProfile1"() {
        setup:
        profile1.setSeed(1)
        profile2.setSeed(1)
        final Detector detector1 = DetectorFactory.@Companion.create(profile1)
        final Detector detector2 = DetectorFactory.@Companion.create(profile2)
        final String text = "is"
        detector1.append(text)
        detector2.append(text)

        expect:
        detector1.detect() == "en"
        detector2.detect() == "en"
    }

    def "testMultiProfile2"() {
        setup:
        profile1.setSeed(1)
        profile2.setSeed(1)
        final Detector detector1 = DetectorFactory.@Companion.create(profile1)
        final Detector detector2 = DetectorFactory.@Companion.create(profile2)
        final String text = "sono"
        detector1.append(text)
        detector2.append(text)

        expect:
        detector1.detect() == "it"
        detector2.detect() == "fr"
    }

    def "testMultiProfile3"() {
        setup:
        profile1.setSeed(1)
        profile2.setSeed(1)
        final Detector detector1 = DetectorFactory.@Companion.create(profile1)
        final Detector detector2 = DetectorFactory.@Companion.create(profile2)
        final String text = "suis"
        detector1.append(text)
        detector2.append(text)

        expect:
        detector1.detect() == "en"
        detector2.detect() == "fr"
    }
}
