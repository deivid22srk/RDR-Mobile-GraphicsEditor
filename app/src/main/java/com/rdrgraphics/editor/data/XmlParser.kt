package com.rdrgraphics.editor.data

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

data class XmlField(
    val name: String,
    val value: String,
    val type: FieldType,
    val lineNumber: Int
) {
    enum class FieldType {
        BOOLEAN, INTEGER, FLOAT, STRING
    }
    
    fun inferType(value: String): FieldType {
        return when {
            value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true) -> FieldType.BOOLEAN
            value.toIntOrNull() != null -> FieldType.INTEGER
            value.toFloatOrNull() != null -> FieldType.FLOAT
            else -> FieldType.STRING
        }
    }
}

object XmlParser {
    fun parseGraphicsXml(xmlContent: String): Pair<String, List<XmlField>> {
        val fields = mutableListOf<XmlField>()
        var rootTag = ""
        var rootVersion = ""
        
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))
            
            val lines = xmlContent.lines()
            var lineNumber = 0
            
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name
                        if (rootTag.isEmpty() && tagName != "xml") {
                            rootTag = tagName
                            for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i) == "v") {
                                    rootVersion = parser.getAttributeValue(i)
                                }
                            }
                        } else if (rootTag.isNotEmpty() && tagName != rootTag) {
                            val value = parser.getAttributeValue(null, "value") ?: ""
                            
                            for (i in lines.indices) {
                                if (lines[i].contains("<$tagName") && lines[i].contains("value=")) {
                                    lineNumber = i
                                    break
                                }
                            }
                            
                            val fieldType = when {
                                value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true) -> XmlField.FieldType.BOOLEAN
                                value.contains(".") && value.toFloatOrNull() != null -> XmlField.FieldType.FLOAT
                                value.toIntOrNull() != null -> XmlField.FieldType.INTEGER
                                else -> XmlField.FieldType.STRING
                            }
                            
                            fields.add(XmlField(tagName, value, fieldType, lineNumber))
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return Pair(if (rootVersion.isNotEmpty()) "$rootTag|$rootVersion" else rootTag, fields)
    }
    
    fun buildXml(rootTagWithVersion: String, fields: List<XmlField>): String {
        val parts = rootTagWithVersion.split("|")
        val rootTag = parts[0]
        val version = if (parts.size > 1) parts[1] else "83.0"
        
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<$rootTag v=\"$version\">\n")
        
        for (field in fields) {
            val formattedValue = when (field.type) {
                XmlField.FieldType.FLOAT -> {
                    val floatVal = field.value.toFloatOrNull() ?: 0f
                    String.format("%.6f", floatVal)
                }
                else -> field.value
            }
            sb.append("\t<${field.name} value=\"$formattedValue\"/>\n")
        }
        
        sb.append("</$rootTag>")
        return sb.toString()
    }
    
    fun updateField(xmlContent: String, fieldName: String, newValue: String): String {
        val lines = xmlContent.lines().toMutableList()
        
        for (i in lines.indices) {
            val line = lines[i]
            if (line.contains("<$fieldName") && line.contains("value=")) {
                val regex = Regex("""(<$fieldName\s+value=")[^"]*(".*/>)""")
                lines[i] = regex.replace(line, "$1$newValue$2")
                break
            }
        }
        
        return lines.joinToString("\n")
    }
}
