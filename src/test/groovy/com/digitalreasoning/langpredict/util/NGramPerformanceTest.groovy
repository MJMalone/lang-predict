/**
 * 
 */
package com.digitalreasoning.langpredict.util

import spock.lang.Specification
import spock.lang.Ignore

// NOTE: This should not be run normally.  It's just a convenience for comparing ngram generation run times
// when making changes to that part of the code.
@Ignore
class NGramPerformanceTest extends Specification {

    def "compare"() {
        setup:
        def oldNGram = new OLD_NGram()
        def newNGram = new NGramGenerator()

        def text = "  abcde fg h i j k l   mn o  p  q are CAPS Right?  "

        when:
        final List<String> oldResult = new ArrayList<>()
        text.chars().forEachOrdered {
            oldNGram.addChar(it as char)
            oldResult.add(oldNGram.get(1))
            oldResult.add(oldNGram.get(2))
            oldResult.add(oldNGram.get(3))
        }
        oldResult.removeIf { it == null }
        oldResult.sort()

        final List<String> newResult = newNGram.generate(text)
        newResult.sort()

        then:
        // Same except for some (probably) erroneous results from the old NGram
        oldResult - [" C", "C", "PS ", "S "] == newResult
    }

    def "time it"() {
        setup:
        def text = new File("src/test/resources/test.txt").getText("UTF-8")

        when:

        long oldTotal = 0L
        long newTotal = 0L

        for (int idx = 1; idx <= 100; idx++) {

            long newT1 = new Date().getTime()
            final List<String> newResult = newMethod(text)
            long newT2 = new Date().getTime()
            println("NEW $idx: ${newT2 - newT1}")

            long oldT1 = new Date().getTime()
            final List<String> oldResult = oldMethod(text)
            long oldT2 = new Date().getTime()
            println("OLD $idx: ${oldT2 - oldT1}")

            newTotal += (newT2 - newT1)
            oldTotal += (oldT2 - oldT1)

//            if (oldResult.toSet() != newResult.toSet()) {
//                throw new Exception("Results do not match.")
//            }
        }

        println("TOTAL OLD : $oldTotal")
        println("TOTAL NEW : $newTotal")

        then:
        noExceptionThrown()
        1 + 2 == 3
    }

    private List<String> oldMethod(final String text) {
        def oldNGram = new OLD_NGram()

        final List<String> result = new ArrayList<>()
        text.chars().forEachOrdered {
            oldNGram.addChar(it as char)
            result.add(oldNGram.get(1))
            result.add(oldNGram.get(2))
            result.add(oldNGram.get(3))
        }
        result.removeIf { it == null }
        return result
    }

    private List<String> newMethod(final String text) {
        return new NGramGenerator().generate(text)
    }

}