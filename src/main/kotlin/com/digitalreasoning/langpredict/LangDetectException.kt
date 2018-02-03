package com.digitalreasoning.langpredict

class LangDetectException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
