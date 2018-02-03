package com.digitalreasoning.langpredict

import java.util.*

object LanguageCatalog {
    private val map = TreeMap<String, LanguageProfile>()

    fun contains(lang: String) = map.containsKey(lang)

    fun get(lang: String) = map[lang]

    fun add(json: String) {
        LanguageProfileFactory.fromJson(json).let { prof ->
            synchronized(map) {
                map.put(prof.name, prof)
            }
        }
    }
}
