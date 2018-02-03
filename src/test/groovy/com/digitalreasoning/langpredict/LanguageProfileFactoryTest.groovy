package com.digitalreasoning.langpredict

import spock.lang.Specification

class LanguageProfileFactoryTest extends Specification {
    def "fromJson should work as expected"() {
        when:
        def profile = LanguageProfileFactory.INSTANCE.fromJson(
                '{"name":"test","n_words":[12,30,13],"freq":{"a":11,"b":7,"c":17}}'
        )

        then:
        profile.name == "test"
        profile.ngramCounts == [12, 30, 13]
        profile.frequencyMap == [ "a":11, "b":7, "c":17 ]
    }

    def "toJson should work as expected"() {
        when:
        def profile = new LanguageProfile("test", [12, 30, 13], [ "a":11, "b":7, "c":17 ])
        def json = LanguageProfileFactory.INSTANCE.toJson(profile)

        then:
        json == '{"name":"test","n_words":[12,30,13],"freq":{"a":11,"b":7,"c":17}}'
    }
}
