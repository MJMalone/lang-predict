package com.digitalreasoning.langpredict.profilegen

/**
 * [TagExtractor] is a class which extracts inner texts of specified tag.
 * Users don't use this class directly.
 * @author Nakatani Shuyo
 */
class TagExtractor(internal var target_: String, internal var threshold_: Int) {
    private val buf: StringBuffer = StringBuffer()
    internal var tag: String? = null
    private var count: Int = 0

    fun count(): Int {
        return count
    }

    fun setTag(tag: String) {
        this.tag = tag
    }

    fun add(line: String?) {
        if (tag === target_ && line != null) {
            buf.append(line)
        }
    }

    fun closeTag(): String? {
        var st: String? = null
        if (tag === target_ && buf.length > threshold_) {
            st = buf.toString()
            ++count
        }
        buf.setLength(0) // clear the buffer
        tag = null
        return st
    }

}
