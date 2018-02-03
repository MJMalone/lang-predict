package com.digitalreasoning.langpredict

import spock.lang.Specification
import spock.lang.Unroll

class LanguagePredictionTest extends Specification {

    @Unroll
    def "some actual language predictions should work"() {
        setup:
        def prof = DetectorFactory.@Companion.loadProfile(new File("src/main/resources/profiles"))
        prof.setSeed(1)

        expect:
        final Detector detector = DetectorFactory.@Companion.create(prof)
        detector.append(testText)
        detector.detect() == prediction

        where:
        testText | prediction
        "What a very fine day it is today my good sir." | "en"
        "Eh bien, qu'est-ce que c'est que ça, alors?"   | "fr"
        "Mein Luftkissenfahrzeug ist voller Aale!"      | "de"
        "¡Feliz Navidad y próspero año nuevo!"          | "es"
        "Il mio aeroscivolante è pieno di anguille!"    | "it"
        "O meu hovercraft está cheio de enguias"        | "pt"
        "Nói một thứ tiếng thì không bao giờ đủ"        | "vi"
        "मेरी मँडराने वाली नाव सर्पमीनों से भरी हैं"                  | "hi"
        "Wesołych świąt i szczęśliwego Nowego Roku"     | "pl"
        "کیا آپ میر ے ساتھ ناچنا پسند کریں گی"          | "ur"
        "حَوّامتي مُمْتِلئة بِأَنْقَلَيْسون"                       | "ar"
        "જલ્દી ઠીક થઇ જાવ તેવી શુભકામના"                    | "gu"
        "Můžeme se prosím podívat na ten účet?"         | "cs"
        "Mijn luchtkussenboot zit vol paling"           | "nl"
        "你會不會講國語"                                  | "zh-tw"
        "你会不会讲国语"                                  | "zh-cn"
        "テーブルの予約を取りたいのですが"                  | "ja"
        "אפשׁר לדבּר יותר לאט?"                            | "he"
        "화장실이 어디예요?"                                | "ko"
        "உங்கள் உடல் விரைவாக குணம் அடையட்டும்"           | "ta"
        "Min svävare är full med ål"                    | "sv"
        "Nawa'y pagpalain ka ng Diyos ng marami pang kaarawan" | "tl"
        "నాతో నాట్యం చేసే కుతూహలం ఉన్నదా?"                | "te"
        "Вы не могли бы говорить помедленнее?"          | "ru"
    }

}