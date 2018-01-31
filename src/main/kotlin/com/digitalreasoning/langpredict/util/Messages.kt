package com.digitalreasoning.langpredict.util

import java.util.*

object Messages {
    private val BUNDLE_NAME = "com.digitalreasoning.langpredict.util.messages"
    private val RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME)

    fun getString(key: String): String {
        return RESOURCE_BUNDLE.getString(key)
    }
}
