package org.schabi.newpipe.extractor.stream

import java.io.Serializable
import java.util.Objects

class Description(content: String?, val type: Int) : Serializable {
    @JvmField
    val content: String? = null

    init {
        if (content == null) {
            this.content = ""
        } else {
            this.content = content
        }
    }

    public override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that: Description = o as Description
        return type == that.type && Objects.equals(content, that.content)
    }

    public override fun hashCode(): Int {
        return Objects.hash(content, type)
    }

    companion object {
        val HTML: Int = 1
        val MARKDOWN: Int = 2
        @JvmField
        val PLAIN_TEXT: Int = 3
        @JvmField
        val EMPTY_DESCRIPTION: Description = Description("", PLAIN_TEXT)
    }
}
