package com.digitalreasoning.langpredict

import com.digitalreasoning.langpredict.util.LangProfile
import net.arnx.jsonic.JSON
import net.arnx.jsonic.JSONException

import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Language Detector Factory Class
 *
 * This class manages an initialization and constructions of [Detector].
 *
 * Before using language detection library,
 * load profiles with [DetectorFactory.loadProfile] method
 * and set initialization parameters.
 *
 * When the language detection,
 * construct Detector instance via [DetectorFactory.create].
 * See also [Detector]'s sample code.
 *
 *
 *  * 4x faster improvement based on Elmer Garduno's code. Thanks!
 *
 *
 * @see Detector
 *
 * @author Nakatani Shuyo
 */
class DetectorFactory/* package scope */ internal constructor() {
    var profiles: DetectorProfiles = DetectorProfiles()

    private fun loadProfileImpl(profileDirectory: File): DetectorProfiles {
        val listFiles = profileDirectory.listFiles() ?: throw LangDetectException("Not found profile: " + profileDirectory)

        val langsize = listFiles.size
        var index = 0
        for (file in listFiles) {
            if (file.name.startsWith(".") || !file.isFile) continue
            var `is`: FileInputStream? = null
            try {
                `is` = FileInputStream(file)
                val profile = JSON.decode(`is`, LangProfile::class.java)
                profiles!!.addProfile(profile, index, langsize)
                ++index
            } catch (e: JSONException) {
                throw LangDetectException("profile format error in '" + file.name + "'")
            } catch (e: IOException) {
                throw LangDetectException("can't open '" + file.name + "'")
            } finally {
                try {
                    if (`is` != null) `is`.close()
                } catch (e: IOException) {
                }

            }
        }

        return profiles
    }

    /* package scope */ @Throws(LangDetectException::class)
    internal fun loadProfileImpl(json_profiles: List<String>): DetectorProfiles {
        profiles = DetectorProfiles()

        var index = 0
        val langsize = json_profiles.size
        if (langsize < 2)
            throw LangDetectException("Need more than 2 profiles")

        for (json in json_profiles) {
            try {
                val profile = JSON.decode(json, LangProfile::class.java)
                profiles!!.addProfile(profile, index, langsize)
                ++index
            } catch (e: JSONException) {
                throw LangDetectException("profile format error")
            }

        }

        return profiles
    }

    companion object {
        private val instance_ = DetectorFactory()

        /**
         * Load profiles from specified directory.
         * This method must be called once before language detection.
         *
         * @param profileDirectory profile directory path
         */
        @Throws(LangDetectException::class)
        fun loadProfile(profileDirectory: String): DetectorProfiles {
            return loadProfile(File(profileDirectory))
        }

        /**
         * Load profiles from specified directory.
         * This method must be called once before language detection.
         *
         * @param profileDirectory profile directory path
         */
        @Throws(LangDetectException::class)
        fun loadProfile(profileDirectory: File): DetectorProfiles {
            return instance_.loadProfileImpl(profileDirectory)
        }

        /**
         * Load profiles from specified directory.
         * This method must be called once before language detection.
         */
        @Throws(LangDetectException::class)
        fun loadProfile(json_profiles: List<String>): DetectorProfiles {
            return instance_.loadProfileImpl(json_profiles)
        }

        @Throws(LangDetectException::class)
        internal /* package scope */ fun addProfile(profile: LangProfile, index: Int, langsize: Int) {
            instance_.profiles!!.addProfile(profile, index, langsize)
        }


        /**
         * Clear loaded language profiles (re-initialization to be available)
         */
        fun clear() {
            if (instance_.profiles != null) instance_.profiles!!.clear()
        }

        /**
         * Construct Detector instance
         *
         * @return Detector instance
         * @throws LangDetectException
         */
        @Throws(LangDetectException::class)
        @JvmOverloads
        fun create(profiles: DetectorProfiles = instance_.profiles): Detector {
            if (profiles.langList.size == 0)
                throw LangDetectException("need to load profiles")
            return Detector(profiles)
        }

        /**
         * Construct Detector instance with smoothing parameter
         *
         * @param alpha smoothing parameter (default value = 0.5)
         * @return Detector instance
         * @throws LangDetectException
         */
        @Throws(LangDetectException::class)
        fun create(alpha: Double): Detector {
            return create(instance_.profiles, alpha)
        }

        /**
         * Construct Detector instance with smoothing parameter
         *
         * @param alpha smoothing parameter (default value = 0.5)
         * @return Detector instance
         * @throws LangDetectException
         */
        @Throws(LangDetectException::class)
        fun create(profiles: DetectorProfiles?, alpha: Double): Detector {
            val detector = create(profiles!!)
            detector.setAlpha(alpha)
            return detector
        }

        /**
         * Set random seed for coherent detection.
         *
         * Hence language-detection draws random features to reduce bias,
         * it will return incoherent language whenever it detects.
         * When you need coherent detection, you should set a fixed number into seed.
         *
         * @param seed random seed to set
         */
        fun setSeed(seed: Long) {
            instance_.profiles!!.setSeed(seed)
        }

        /**
         * @return languages list in profiles
         */
        val langList: List<String>
            get() = instance_.profiles!!.langList
    }
}
/**
 * Construct Detector instance
 *
 * @return Detector instance
 * @throws LangDetectException
 */
