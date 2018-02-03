package com.digitalreasoning.langpredict.profilegen

import com.digitalreasoning.langpredict.LangDetectException
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
    fun loadFromWikipediaAbstract(lang: String, file: File): LanguageProfileGenerator {

        val profile = LanguageProfileGenerator(lang)

        try {
            BufferedReader(InputStreamReader(getInstream(file), "UTF-8")).use { br ->
                val tagextractor = TagExtractor("abstract", 100)

                var reader: XMLStreamReader? = null
                try {
                    reader = XMLInputFactory.newInstance().createXMLStreamReader(br)!!
                    while (reader.hasNext()) {
                        when (reader.next()) {
                            XMLStreamReader.START_ELEMENT -> tagextractor.setTag(reader.name.toString())
                            XMLStreamReader.CHARACTERS -> tagextractor.add(reader.text)
                            XMLStreamReader.END_ELEMENT -> {
                                val text = tagextractor.closeTag()
                                text?.let { profile.update(it) }
                            }
                        }
                    }
                } catch (ex: XMLStreamException) {
                    throw LangDetectException("Training database file '" + file.name + "' is an invalid XML.", ex)
                } finally {
                    reader?.close()
                }
                println(lang + ":" + tagextractor.count())
            }
        } catch (ex: IOException) {
            throw LangDetectException("Can't open training database file '" + file.name + "'", ex)
        }

        return profile
    }

    private fun getInstream(file: File): InputStream {
        val instream: InputStream = FileInputStream(file)
        return if (file.name.endsWith(".gz")) GZIPInputStream(instream) else instream
    }


    /**
     * Load text file with UTF-8 and generate its language profile
     * @param lang target language name
     * @param file target file path
     * @return Language profile instance
     */
    fun loadFromText(lang: String, file: File): LanguageProfileGenerator {

        val profile = LanguageProfileGenerator(lang)

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
