package com.rdrgraphics.editor.data

sealed class XmlField {
    abstract val name: String
    abstract val originalLine: String
    abstract val lineNumber: Int
    abstract var isModified: Boolean
    
    data class BooleanField(
        override val name: String,
        var value: Boolean,
        override val originalLine: String,
        override val lineNumber: Int,
        override var isModified: Boolean = false
    ) : XmlField()
    
    data class IntField(
        override val name: String,
        var value: Int,
        val min: Int = Int.MIN_VALUE,
        val max: Int = Int.MAX_VALUE,
        override val originalLine: String,
        override val lineNumber: Int,
        override var isModified: Boolean = false
    ) : XmlField()
    
    data class FloatField(
        override val name: String,
        var value: Float,
        val min: Float = Float.MIN_VALUE,
        val max: Float = Float.MAX_VALUE,
        override val originalLine: String,
        override val lineNumber: Int,
        override var isModified: Boolean = false
    ) : XmlField()
    
    fun toXmlLine(): String {
        val indent = originalLine.takeWhile { it.isWhitespace() }
        return when (this) {
            is BooleanField -> "$indent<$name value=\"$value\"/>"
            is IntField -> "$indent<$name value=\"$value\"/>"
            is FloatField -> "$indent<$name value=\"${String.format("%.6f", value)}\"/>"
        }
    }
}
