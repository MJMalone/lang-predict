package com.digitalreasoning.langpredict

import com.digitalreasoning.langpredict.util.LangProfile
import com.digitalreasoning.langpredict.util.TagExtractor
import java.io.*
import java.util.zip.GZIPInputStream
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader

/**
 * Load Wikipedia's abstract XML as corpus and
 * generate its language profile in JSON format.
 *
 * @author Nakatani Shuyo
 */
object GenProfile {

    /**
     * Load Wikipedia abstract database file and generate its language profile
     * @param lang target language name
     * @param file target database file path
     * @return Language profile instance
     */
    fun loadFromWikipediaAbstract(lang: String, file: File): LangProfile {

        val profile = LangProfile(lang)

        var br: BufferedReader? = null
        try {
            var `is`: InputStream = FileInputStream(file)
            if (file.name.endsWith(".gz")) `is` = GZIPInputStream(`is`)
            br = BufferedReader(InputStreamReader(`is`, "utf-8"))

            val tagextractor = TagExtractor("abstract", 100)

            var reader: XMLStreamReader? = null
            try {
                val factory = XMLInputFactory.newInstance()
                reader = factory.createXMLStreamReader(br)
                while (reader!!.hasNext()) {
                    when (reader.next()) {
                        XMLStreamReader.START_ELEMENT -> tagextractor.setTag(reader.name.toString())
                        XMLStreamReader.CHARACTERS -> tagextractor.add(reader.text)
                        XMLStreamReader.END_ELEMENT -> {
                            val text = tagextractor.closeTag()
                            if (text != null) profile.update(text)
                        }
                    }
                }
            } catch (e: XMLStreamException) {
                throw LangDetectException("Training database file '" + file.name + "' is an invalid XML.")
            } finally {
                try {
                    if (reader != null) reader.close()
                } catch (e: XMLStreamException) {
                }

            }
            println(lang + ":" + tagextractor.count())

        } catch (e: IOException) {
            throw LangDetectException("Can't open training database file '" + file.name + "'")
        } finally {
            try {
                if (br != null) br.close()
            } catch (e: IOException) {
            }

        }
        return profile
    }


    /**
     * Load text file with UTF-8 and generate its language profile
     * @param lang target language name
     * @param file target file path
     * @return Language profile instance
     */
    fun loadFromText(lang: String, file: File): LangProfile {

        val profile = LangProfile(lang)

        var `is`: BufferedReader? = null
        try {
            `is` = BufferedReader(InputStreamReader(FileInputStream(file), "utf-8"))

            var count = 0
            while (`is`.ready()) {
                val line = `is`.readLine()
                profile.update(line)
                ++count
            }

            println(lang + ":" + count)

        } catch (e: IOException) {
            throw LangDetectException("Can't open training database file '" + file.name + "'")
        } finally {
            try {
                if (`is` != null) `is`.close()
            } catch (e: IOException) {
            }

        }
        return profile
    }
}
