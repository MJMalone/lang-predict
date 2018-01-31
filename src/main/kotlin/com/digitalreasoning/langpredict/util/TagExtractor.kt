package com.digitalreasoning.langpredict.util

/**
 * [TagExtractor] is a class which extracts inner texts of specified tag.
 * Users don't use this class directly.
 * @author Nakatani Shuyo
 */
class TagExtractor(internal var target_: String?, internal var threshold_: Int) {
    internal var buf: StringBuffer = StringBuffer()
    internal var tag: String? = null
    private var count: Int = 0

    init {
        count = 0
        clear()
    }

    fun count(): Int {
        return count
    }

    fun clear() {
        buf = StringBuffer()
        tag = null
    }

    fun setTag(tag: String?) {
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
        clear()
        return st
    }

}
