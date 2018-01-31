package com.digitalreasoning.langpredict

import net.arnx.jsonic.JSON
import net.arnx.jsonic.JSONException
import java.io.*
import java.util.*

/**
 *
 * LangDetect Command Line Interface
 *
 *
 * This is a command line interface of Language Detection Library "LandDetect".
 *
 *
 * @author Nakatani Shuyo
 */
class Command {

    /** for Command line easy parser  */
    private val opt_with_value = HashMap<String, String>()
    private val values = HashMap<String, String?>()
    private val opt_without_value = HashSet<String>()
    private val arglist = ArrayList<String>()

    /**
     * Command line easy parser
     * @param args command line arguments
     */
    private fun parse(args: Array<String>) {
        var i = 0
        while (i < args.size) {
            if (opt_with_value.containsKey(args[i])) {
                val key = opt_with_value[args[i]]!!
                values.put(key, args[i + 1])
                ++i
            } else if (args[i].startsWith("-")) {
                opt_without_value.add(args[i])
            } else {
                arglist.add(args[i])
            }
            ++i
        }
    }

    private fun addOpt(opt: String, key: String, value: String?) {
        opt_with_value.put(opt, key)
        values.put(key, value)
    }

    private operator fun get(key: String): String? {
        return values[key]
    }

    private fun getLong(key: String): Long? {
        val value = values[key] ?: return null
        try {
            return java.lang.Long.valueOf(value)
        } catch (e: NumberFormatException) {
            return null
        }

    }

    private fun getDouble(key: String, defaultValue: Double): Double {
        try {
            return java.lang.Double.valueOf(values[key])
        } catch (e: NumberFormatException) {
            return defaultValue
        }

    }

    private fun hasOpt(opt: String): Boolean {
        return opt_without_value.contains(opt)
    }


    /**
     * File search (easy glob)
     * @param directory directory path
     * @param pattern   searching file pattern with regular representation
     * @return matched file
     */
    private fun searchFile(directory: File, pattern: String): File? {
        for (file in directory.listFiles()!!) {
            if (file.name.matches(pattern.toRegex())) return file
        }
        return null
    }


    /**
     * load profiles
     * @return false if load success
     */
    private fun loadProfile(): Boolean {
        val profileDirectory = get("directory")!! + "/"
        try {
            DetectorFactory.loadProfile(profileDirectory)
            val seed = getLong("seed")
            if (seed != null) DetectorFactory.setSeed(seed)
            return false
        } catch (e: LangDetectException) {
            System.err.println("ERROR: " + e.message)
            return true
        }

    }

    /**
     * Generate Language Profile from Wikipedia Abstract Database File
     *
     * <pre>
     * usage: --genprofile -d [abstracts directory] [language names]
    </pre> *
     *
     */
    fun generateProfile() {
        val directory = File(get("directory")!!)
        for (lang in arglist) {
            val file = searchFile(directory, lang + "wiki-.*-abstract\\.xml.*")
            if (file == null) {
                System.err.println("Not Found abstract xml : lang = " + lang)
                continue
            }

            var os: FileOutputStream? = null
            try {
                val profile = GenProfile.loadFromWikipediaAbstract(lang, file)
                profile.omitLessFreq()

                val profile_path = File(get("directory") + "/profiles/" + lang)
                os = FileOutputStream(profile_path)
                JSON.encode(profile, os)
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: LangDetectException) {
                e.printStackTrace()
            } finally {
                try {
                    if (os != null) os.close()
                } catch (e: IOException) {
                }

            }
        }
    }

    /**
     * Generate Language Profile from Text File
     *
     * <pre>
     * usage: --genprofile-text -l [language code] [text file path]
    </pre> *
     *
     */
    private fun generateProfileFromText() {
        if (arglist.size != 1) {
            System.err.println("Need to specify text file path")
            return
        }
        val file = File(arglist[0])
        if (!file.exists()) {
            System.err.println("Need to specify existing text file path")
            return
        }

        val lang = get("lang")
        if (lang == null) {
            System.err.println("Need to specify langage code(-l)")
            return
        }

        var os: FileOutputStream? = null
        try {
            val profile = GenProfile.loadFromText(lang, file)
            profile.omitLessFreq()

            val profile_path = File(lang)
            os = FileOutputStream(profile_path)
            JSON.encode(profile, os)
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: LangDetectException) {
            e.printStackTrace()
        } finally {
            try {
                if (os != null) os.close()
            } catch (e: IOException) {
            }

        }
    }

    /**
     * Language detection test for each file (--detectlang option)
     *
     * <pre>
     * usage: --detectlang -d [profile directory] -a [alpha] -s [seed] [test file(s)]
    </pre> *
     *
     */
    fun detectLang() {
        if (loadProfile()) return
        for (filename in arglist) {
            var `is`: BufferedReader? = null
            try {
                `is` = BufferedReader(InputStreamReader(FileInputStream(filename), "utf-8"))

                val detector = DetectorFactory.create(getDouble("alpha", DEFAULT_ALPHA))
                if (hasOpt("--debug")) detector.setVerbose()
                detector.append(`is`)
                println(filename + ":" + detector.probabilities)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: LangDetectException) {
                e.printStackTrace()
            } finally {
                try {
                    if (`is` != null) `is`.close()
                } catch (e: IOException) {
                }

            }

        }
    }

    /**
     * Batch Test of Language Detection (--batchtest option)
     *
     * <pre>
     * usage: --batchtest -d [profile directory] -a [alpha] -s [seed] [test data(s)]
    </pre> *
     *
     * The format of test data(s):
     * <pre>
     * [correct language name]\t[text body for test]\n
    </pre> *
     *
     */
    fun batchTest() {
        if (loadProfile()) return
        val result = HashMap<String, ArrayList<String>>()
        for (filename in arglist) {
            var `is`: BufferedReader? = null
            try {
                `is` = BufferedReader(InputStreamReader(FileInputStream(filename), "utf-8"))
                while (`is`.ready()) {
                    val line = `is`.readLine()
                    val idx = line.indexOf('\t')
                    if (idx <= 0) continue
                    val correctLang = line.substring(0, idx)
                    val text = line.substring(idx + 1)

                    val detector = DetectorFactory.create(getDouble("alpha", DEFAULT_ALPHA))
                    detector.append(text)
                    var lang = ""
                    try {
                        lang = detector.detect() ?: ""
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (!result.containsKey(correctLang)) result.put(correctLang, ArrayList())
                    result[correctLang]!!.add(lang)
                    if (hasOpt("--debug")) println(correctLang + "," + lang + "," + if (text.length > 100) text.substring(0, 100) else text)
                }

            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: LangDetectException) {
                e.printStackTrace()
            } finally {
                try {
                    if (`is` != null) `is`.close()
                } catch (e: IOException) {
                }

            }

            val langlist = ArrayList(result.keys)
            Collections.sort(langlist)

            var totalCount = 0
            var totalCorrect = 0
            for (lang in langlist) {
                val resultCount = HashMap<String, Int>()
                var count = 0
                val list = result[lang] ?: emptyList<String>()
                for (detectedLang in list) {
                    ++count
                    if (resultCount.containsKey(detectedLang)) {
                        resultCount.put(detectedLang, resultCount[detectedLang]?:0 + 1)
                    } else {
                        resultCount.put(detectedLang, 1)
                    }
                }
                val correct = if (resultCount.containsKey(lang)) resultCount[lang] else 0
                val rate = correct?:0 / count.toDouble()
                println(String.format("%s (%d/%d=%.2f): %s", lang, correct, count, rate, resultCount))
                totalCorrect += correct?:0
                totalCount += count
            }
            println(String.format("total: %d/%d = %.3f", totalCorrect, totalCount, totalCorrect / totalCount.toDouble()))

        }

    }

    companion object {
        /** smoothing default parameter (ELE)  */
        private val DEFAULT_ALPHA = 0.5

        /**
         * Command Line Interface
         * @param args command line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val command = Command()
            command.addOpt("-d", "directory", "./")
            command.addOpt("-a", "alpha", "" + DEFAULT_ALPHA)
            command.addOpt("-s", "seed", null)
            command.addOpt("-l", "lang", null)
            command.parse(args)

            if (command.hasOpt("--genprofile")) {
                command.generateProfile()
            } else if (command.hasOpt("--genprofile-text")) {
                command.generateProfileFromText()
            } else if (command.hasOpt("--detectlang")) {
                command.detectLang()
            } else if (command.hasOpt("--batchtest")) {
                command.batchTest()
            }
        }
    }

}
