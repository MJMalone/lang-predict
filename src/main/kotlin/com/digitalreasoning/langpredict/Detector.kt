package com.digitalreasoning.langpredict

import com.digitalreasoning.langpredict.util.NGram
import java.io.IOException
import java.io.Reader
import java.lang.Character.UnicodeBlock
import java.util.*
import java.util.regex.Pattern

/**
 * [Detector] class is to detect language from specified text.
 * Its instance is able to be constructed via the factory class [DetectorFactory].
 *
 *
 * After appending a target text to the [Detector] instance with [.append] or [.append],
 * the detector provides the language detection results for target text via [.detect] or [.getProbabilities].
 * [.detect] method returns a single language name which has the highest probability.
 * [.getProbabilities] methods returns a list of multiple languages and their probabilities.
 *
 *
 * The detector has some parameters for language detection.
 * See [.setAlpha], [.setMaxTextLength] and [.setPriorMap].
 *
 * <pre>
 * import java.util.ArrayList;
 * import Detector;
 * import DetectorFactory;
 * import Language;
 *
 * class LangDetectSample {
 * public void init(String profileDirectory) throws LangDetectException {
 * DetectorFactory.loadProfile(profileDirectory);
 * }
 * public String detect(String text) throws LangDetectException {
 * Detector detector = DetectorFactory.create();
 * detector.append(text);
 * return detector.detect();
 * }
 * public ArrayList<Language> detectLangs(String text) throws LangDetectException {
 * Detector detector = DetectorFactory.create();
 * detector.append(text);
 * return detector.getProbabilities();
 * }
 * }
</Language></pre> *
 *
 *
 *  * 4x faster improvement based on Elmer Garduno's code. Thanks!
 *
 *
 * @author Nakatani Shuyo
 * @see DetectorFactory
 */
class Detector
/**
 * Constructor.
 * Detector instance can be constructed via [DetectorFactory.create].
 * @param profiles [DetectorFactory] instance (only DetectorFactory inside)
 */
(profiles: DetectorProfiles) {

    private val wordLangProbMap: HashMap<String, DoubleArray>
    private val langlist: ArrayList<String>

    private var text: StringBuffer = StringBuffer()
    private var langprob: DoubleArray = DoubleArray(0)

    private var alpha = ALPHA_DEFAULT
    private val n_trial = 7
    private var max_text_length = 10000
    private var priorMap: DoubleArray = DoubleArray(0)
    private var verbose = false
    private val seed: Long?

    /**
     * Get language candidates which have high probabilities
     * @return possible languages list (whose probabilities are over PROB_THRESHOLD, ordered by probabilities descendently
     * @throws LangDetectException
     * code = ErrorCode.CantDetectError : Can't detect because of no valid features in text
     */
    val probabilities: ArrayList<Language>
        get() {
            if (langprob.isEmpty()) detectBlock()

            return sortProbability(langprob)
        }

    init {
        this.wordLangProbMap = profiles.wordLangProbMap
        this.langlist = profiles.langlist
        this.text = StringBuffer()
        this.seed = profiles.seed
    }

    /**
     * Set Verbose Mode(use for debug).
     */
    fun setVerbose() {
        this.verbose = true
    }

    /**
     * Set smoothing parameter.
     * The default value is 0.5(i.e. Expected Likelihood Estimate).
     * @param alpha the smoothing parameter
     */
    fun setAlpha(alpha: Double) {
        this.alpha = alpha
    }

    /**
     * Set prior information about language probabilities.
     * @param priorMap the priorMap to set
     * @throws LangDetectException
     */
    @Throws(LangDetectException::class)
    fun setPriorMap(priorMap: HashMap<String, Double>) {
        this.priorMap = DoubleArray(langlist.size)
        var sump = 0.0
        for (i in this.priorMap!!.indices) {
            val lang = langlist[i]
            if (priorMap.containsKey(lang)) {
                val p = priorMap[lang] ?: 0.0
                if (p < 0) throw LangDetectException("Prior probability must be non-negative.")
                this.priorMap[i] = p
                sump += p
            }
        }
        if (sump <= 0) throw LangDetectException("More one of prior probability must be non-zero.")
        for (i in this.priorMap!!.indices) this.priorMap[i] /= sump
    }

    /**
     * Specify max size of target text to use for language detection.
     * The default value is 10000(10KB).
     * @param max_text_length the max_text_length to set
     */
    fun setMaxTextLength(max_text_length: Int) {
        this.max_text_length = max_text_length
    }


    /**
     * Append the target text for language detection.
     * This method read the text from specified input reader.
     * If the total size of target text exceeds the limit size specified by [Detector.setMaxTextLength],
     * the rest is cut down.
     *
     * @param reader the input reader (BufferedReader as usual)
     * @throws IOException Can't read the reader.
     */
    @Throws(IOException::class)
    fun append(reader: Reader) {
        val buf = CharArray(max_text_length / 2)
        while (text!!.length < max_text_length && reader.ready()) {
            val length = reader.read(buf)
            append(String(buf, 0, length))
        }
    }

    /**
     * Append the target text for language detection.
     * If the total size of target text exceeds the limit size specified by [Detector.setMaxTextLength],
     * the rest is cut down.
     *
     * @param text the target text to append
     */
    fun append(text: String) {
        var text = text
        text = URL_REGEX.matcher(text).replaceAll(" ")
        text = MAIL_REGEX.matcher(text).replaceAll(" ")
        text = NGram.normalize_vi(text)
        var pre: Char = 0.toChar()
        var i = 0
        while (i < text.length && i < max_text_length) {
            val c = text[i]
            if (c != ' ' || pre != ' ') this.text!!.append(c)
            pre = c
            ++i
        }
    }

    /**
     * Cleaning text to detect
     * (eliminate URL, e-mail address and Latin sentence if it is not written in Latin alphabet)
     */
    private fun cleaningText() {
        var latinCount = 0
        var nonLatinCount = 0
        for (i in 0 until text!!.length) {
            val c = text!![i]
            if (c <= 'z' && c >= 'A') {
                ++latinCount
            } else if (c >= '\u0300' && UnicodeBlock.of(c) !== UnicodeBlock.LATIN_EXTENDED_ADDITIONAL) {
                ++nonLatinCount
            }
        }
        if (latinCount * 2 < nonLatinCount) {
            val textWithoutLatin = StringBuffer()
            for (i in 0 until text!!.length) {
                val c = text!![i]
                if (c > 'z' || c < 'A') textWithoutLatin.append(c)
            }
            text = textWithoutLatin
        }

    }

    /**
     * Detect language of the target text and return the language name which has the highest probability.
     * @return detected language name which has most probability.
     * @throws LangDetectException
     * code = ErrorCode.CantDetectError : Can't detect because of no valid features in text
     */
    @Throws(LangDetectException::class)
    fun detect(): String? {
        val probabilities = probabilities
        return if (probabilities.size > 0) probabilities[0].lang else UNKNOWN_LANG
    }

    /**
     * @throws LangDetectException
     */
    @Throws(LangDetectException::class)
    private fun detectBlock() {
        cleaningText()
        val ngrams = extractNGrams()
        if (ngrams.size == 0)
            throw LangDetectException("no features in text")

        langprob = DoubleArray(langlist.size)

        val rand = Random()
        if (seed != null) rand.setSeed(seed)
        for (t in 0 until n_trial) {
            val prob = initProbability()
            val alpha = this.alpha + rand.nextGaussian() * ALPHA_WIDTH

            var i = 0
            while (true) {
                val r = rand.nextInt(ngrams.size)
                updateLangProb(prob, ngrams[r], alpha)
                if (i % 5 == 0) {
                    if (normalizeProb(prob) > CONV_THRESHOLD || i >= ITERATION_LIMIT) break
                    if (verbose) println("> " + sortProbability(prob))
                }
                ++i
            }
            for (j in langprob.indices) langprob[j] += prob[j] / n_trial
            if (verbose) println("==> " + sortProbability(prob))
        }
    }

    /**
     * Initialize the map of language probabilities.
     * If there is the specified prior map, use it as initial map.
     * @return initialized map of language probabilities
     */
    private fun initProbability(): DoubleArray {
        val prob = DoubleArray(langlist.size)
        if (priorMap.isNotEmpty()) {
            for (i in prob.indices) prob[i] = priorMap[i]
        } else {
            for (i in prob.indices) prob[i] = 1.0 / langlist.size
        }
        return prob
    }

    /**
     * Extract n-grams from target text
     * @return n-grams list
     */
    private fun extractNGrams(): ArrayList<String> {
        val list = ArrayList<String>()
        val ngram = NGram()
        for (i in 0 until text.length) {
            ngram.addChar(text[i])
            for (n in 1..NGram.N_GRAM) {
                val w = ngram[n]
                if (w != null && wordLangProbMap.containsKey(w)) list.add(w)
            }
        }
        return list
    }

    /**
     * update language probabilities with N-gram string(N=1,2,3)
     * @param word N-gram string
     */
    private fun updateLangProb(prob: DoubleArray, word: String?, alpha: Double): Boolean {
        if (word == null || !wordLangProbMap.containsKey(word)) return false

        val langProbMap = wordLangProbMap[word]!!
        if (verbose) println(word + "(" + unicodeEncode(word) + "):" + wordProbToString(langProbMap))

        val weight = alpha / BASE_FREQ
        for (i in prob.indices) {
            prob[i] *= weight + langProbMap[i]
        }
        return true
    }

    private fun wordProbToString(prob: DoubleArray): String {
        val formatter = Formatter()
        for (j in prob.indices) {
            val p = prob[j]
            if (p >= 0.00001) {
                formatter.format(" %s:%.5f", langlist[j], p)
            }
        }
        return formatter.toString()
    }

    /**
     * @param probabilities HashMap
     * @return lanugage candidates order by probabilities descendently
     */
    private fun sortProbability(prob: DoubleArray): ArrayList<Language> {
        val list = ArrayList<Language>()
        for (j in prob.indices) {
            val p = prob[j]
            if (p > PROB_THRESHOLD) {
                for (i in 0..list.size) {
                    if (i == list.size || list[i].prob < p) {
                        list.add(i, Language(langlist[j], p))
                        break
                    }
                }
            }
        }
        return list
    }

    companion object {
        private val ALPHA_DEFAULT = 0.5
        private val ALPHA_WIDTH = 0.05

        private val ITERATION_LIMIT = 1000
        private val PROB_THRESHOLD = 0.1
        private val CONV_THRESHOLD = 0.99999
        private val BASE_FREQ = 10000
        private val UNKNOWN_LANG = "unknown"

        private val URL_REGEX = Pattern.compile("https?://[-_.?&~;+=/#0-9A-Za-z]{1,2076}")
        private val MAIL_REGEX = Pattern.compile("[-_.0-9A-Za-z]{1,64}@[-_0-9A-Za-z]{1,255}[-_.0-9A-Za-z]{1,255}")

        /**
         * normalize probabilities and check convergence by the maximun probability
         * @return maximum of probabilities
         */
        private fun normalizeProb(prob: DoubleArray): Double {
            var maxp = 0.0
            var sump = 0.0
            for (i in prob.indices) sump += prob[i]
            for (i in prob.indices) {
                val p = prob[i] / sump
                if (maxp < p) maxp = p
                prob[i] = p
            }
            return maxp
        }

        /**
         * unicode encoding (for verbose mode)
         * @param word
         * @return
         */
        private fun unicodeEncode(word: String): String {
            val buf = StringBuffer()
            for (i in 0 until word.length) {
                val ch = word[i]
                if (ch >= '\u0080') {
                    var st = Integer.toHexString(0x10000 + ch.toInt())
                    while (st.length < 4) st = "0" + st
                    buf.append("\\u").append(st.subSequence(1, 5))
                } else {
                    buf.append(ch)
                }
            }
            return buf.toString()
        }
    }

}
