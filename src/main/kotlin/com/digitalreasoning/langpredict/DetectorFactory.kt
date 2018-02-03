package com.digitalreasoning.langpredict

import java.io.File

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
    internal fun loadProfileImpl(json_profiles: List<String>): DetectorProfiles {
        val langsize = json_profiles.size
        return json_profiles.foldIndexed(DetectorProfiles()) { index, profiles, json ->
            val profile = LanguageProfileFactory.fromJson(json)
            profiles.addProfile(profile, index, langsize)
            profiles
        }
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
            return instance_.loadProfileImpl(
                    profileDirectory.listFiles().map { it.readText(Charsets.UTF_8) }
            )
        }

        /**
         * Load profiles from specified directory.
         * This method must be called once before language detection.
         */
        @Throws(LangDetectException::class)
        fun loadProfile(json_profiles: List<String>): DetectorProfiles {
            return instance_.loadProfileImpl(json_profiles)
        }

        /**
         * Construct Detector instance
         *
         * @return Detector instance
         * @throws LangDetectException
         */
        fun create(profiles: DetectorProfiles): Detector {
            if (profiles.langList.isEmpty())
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
        fun create(profiles: DetectorProfiles, alpha: Double): Detector {
            val detector = create(profiles)
            detector.setAlpha(alpha)
            return detector
        }

    }
}

