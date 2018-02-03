package com.digitalreasoning.langpredict

import com.google.gson.Gson

object LanguageProfileFactory {
    private val gson = Gson()

    fun fromJson(json: String) = gson.fromJson(json, LanguageProfile::class.java)!!

    fun toJson(profile: LanguageProfile) = gson.toJson(profile)
}