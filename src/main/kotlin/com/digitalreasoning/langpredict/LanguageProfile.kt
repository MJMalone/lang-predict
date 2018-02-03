package com.digitalreasoning.langpredict

import com.google.gson.annotations.SerializedName

data class LanguageProfile(
    @SerializedName("name") val name: String,
    @SerializedName("n_words") val ngramCounts: List<Int>,
    @SerializedName("freq") val frequencyMap: Map<String, Int>
)
