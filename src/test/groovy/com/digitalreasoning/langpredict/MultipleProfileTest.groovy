/**
 * 
 */
package com.digitalreasoning.langpredict

import com.digitalreasoning.langpredict.util.LangProfile
import net.arnx.jsonic.JSON
import spock.lang.Specification

class MultipleProfileTest extends Specification {

    private DetectorProfiles profile1
    private DetectorProfiles profile2

    def "setup"() {
        final ArrayList<String> sample_data1 = new ArrayList<String>()
        final ArrayList<String> sample_data2 = new ArrayList<String>()

        final LangProfile profile_en = new LangProfile("en")
        profile_en.update("This is a pen.")
        final String json_en = JSON.encode(profile_en)
        sample_data1.add(json_en)
        sample_data2.add(json_en)

        final LangProfile profile_it = new LangProfile("it")
        profile_it.update("Sono un studente.")
        final String json_it = JSON.encode(profile_it)
        sample_data1.add(json_it)

        final  LangProfile profile_fr = new LangProfile("fr")
        profile_fr.update("Je suis japonais.")
        final String json_fr = JSON.encode(profile_fr)
        sample_data2.add(json_fr)

        profile1 = DetectorFactory.@Companion.loadProfile(sample_data1)
        profile2 = DetectorFactory.@Companion.loadProfile(sample_data2)
    }

    def "testMultiProfile1"() {
        setup:
        DetectorFactory.@Companion.setSeed(1)
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
        DetectorFactory.@Companion.setSeed(1)
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
        DetectorFactory.@Companion.setSeed(1)
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
