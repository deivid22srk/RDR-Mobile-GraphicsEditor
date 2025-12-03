package com.rdrgraphics.editor.data

import android.util.Log

object XmlParser {
    private const val TAG = "XmlParser"
    
    data class ParsedXml(
        val header: String,
        val fields: List<XmlField>,
        val footer: String,
        val allLines: List<String>
    )
    
    fun parseGraphicsXml(content: String): ParsedXml? {
        try {
            val lines = content.lines()
            if (lines.isEmpty()) return null
            
            val fields = mutableListOf<XmlField>()
            val headerLines = mutableListOf<String>()
            val footerLines = mutableListOf<String>()
            
            var inBody = false
            var lineNumber = 0
            
            for (line in lines) {
                lineNumber++
                
                if (line.trim().startsWith("<?xml") || line.trim().startsWith("<rage__GraphicsOptions")) {
                    headerLines.add(line)
                    if (line.contains("<rage__GraphicsOptions")) {
                        inBody = true
                    }
                    continue
                }
                
                if (line.trim().startsWith("</rage__GraphicsOptions")) {
                    footerLines.add(line)
                    inBody = false
                    continue
                }
                
                if (line.trim().isEmpty() || !inBody) {
                    continue
                }
                
                val field = parseField(line, lineNumber)
                if (field != null) {
                    fields.add(field)
                } else {
                    Log.w(TAG, "Could not parse line $lineNumber: $line")
                }
            }
            
            return ParsedXml(
                header = headerLines.joinToString("\n"),
                fields = fields,
                footer = footerLines.joinToString("\n"),
                allLines = lines
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML", e)
            return null
        }
    }
    
    private fun parseField(line: String, lineNumber: Int): XmlField? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("<") || !trimmed.endsWith("/>")) {
            return null
        }
        
        val nameMatch = Regex("<([^\\s>]+)").find(trimmed)
        val valueMatch = Regex("value=\"([^\"]+)\"").find(trimmed)
        
        if (nameMatch == null || valueMatch == null) {
            return null
        }
        
        val name = nameMatch.groupValues[1]
        val valueStr = valueMatch.groupValues[1]
        
        return when {
            valueStr.equals("true", ignoreCase = true) || valueStr.equals("false", ignoreCase = true) -> {
                XmlField.BooleanField(
                    name = name,
                    value = valueStr.toBoolean(),
                    originalLine = line,
                    lineNumber = lineNumber
                )
            }
            valueStr.contains(".") -> {
                val value = valueStr.toFloatOrNull() ?: return null
                XmlField.FloatField(
                    name = name,
                    value = value,
                    min = inferFloatMin(name, value),
                    max = inferFloatMax(name, value),
                    originalLine = line,
                    lineNumber = lineNumber
                )
            }
            else -> {
                val value = valueStr.toIntOrNull() ?: return null
                XmlField.IntField(
                    name = name,
                    value = value,
                    min = inferIntMin(name, value),
                    max = inferIntMax(name, value),
                    originalLine = line,
                    lineNumber = lineNumber
                )
            }
        }
    }
    
    private fun inferIntMin(name: String, currentValue: Int): Int {
        return when {
            name.contains("Quality", ignoreCase = true) -> 0
            name.contains("Preset", ignoreCase = true) -> 0
            name.contains("Resolution", ignoreCase = true) -> if (name.contains("X")) 640 else 360
            name.contains("FrameRate", ignoreCase = true) -> 0
            name.contains("Vsync", ignoreCase = true) -> 0
            name.contains("Monitor", ignoreCase = true) -> 1
            name.contains("Reflex", ignoreCase = true) -> 0
            name.contains("DLSS", ignoreCase = true) -> 0
            name.contains("FSR", ignoreCase = true) -> 0
            name.contains("AntiAliasing", ignoreCase = true) -> 0
            name.contains("Filtering", ignoreCase = true) -> 0
            name.contains("Blur", ignoreCase = true) && !name.contains("Strength") -> 0
            name.contains("Shadow", ignoreCase = true) -> 0
            name.contains("ScaleIndex", ignoreCase = true) -> 0
            name.contains("Framerate", ignoreCase = true) -> 0
            else -> (currentValue - 100).coerceAtLeast(0)
        }
    }
    
    private fun inferIntMax(name: String, currentValue: Int): Int {
        return when {
            name.contains("Quality", ignoreCase = true) -> 4
            name.contains("Preset", ignoreCase = true) -> 4
            name.contains("ResolutionX", ignoreCase = true) -> 3840
            name.contains("ResolutionY", ignoreCase = true) -> 2160
            name.contains("FrameRate", ignoreCase = true) -> 240
            name.contains("Vsync", ignoreCase = true) -> 1
            name.contains("Monitor", ignoreCase = true) -> 4
            name.contains("Reflex", ignoreCase = true) -> 2
            name.contains("DLSS", ignoreCase = true) -> 4
            name.contains("FSR", ignoreCase = true) -> 4
            name.contains("AntiAliasing", ignoreCase = true) -> 4
            name.contains("Filtering", ignoreCase = true) -> 16
            name.contains("Blur", ignoreCase = true) && !name.contains("Strength") -> 2
            name.contains("Shadow", ignoreCase = true) -> 4
            name.contains("ScaleIndex", ignoreCase = true) -> 10
            name.contains("Framerate", ignoreCase = true) -> 240
            else -> (currentValue + 100).coerceAtMost(10000)
        }
    }
    
    private fun inferFloatMin(name: String, currentValue: Float): Float {
        return when {
            name.contains("Strength", ignoreCase = true) -> 0f
            name.contains("Sharpness", ignoreCase = true) -> 0f
            name.contains("Percentage", ignoreCase = true) -> 0.1f
            name.contains("Brightness", ignoreCase = true) -> 50f
            name.contains("White", ignoreCase = true) -> 50f
            name.contains("Distance", ignoreCase = true) -> 0f
            name.contains("Radius", ignoreCase = true) -> 50f
            name.contains("Factor", ignoreCase = true) -> 0.1f
            name.contains("Detail", ignoreCase = true) -> 0.1f
            name.contains("Rate", ignoreCase = true) -> 0f
            else -> (currentValue - 50f).coerceAtLeast(0f)
        }
    }
    
    private fun inferFloatMax(name: String, currentValue: Float): Float {
        return when {
            name.contains("Strength", ignoreCase = true) -> 1f
            name.contains("Sharpness", ignoreCase = true) -> 1f
            name.contains("Percentage", ignoreCase = true) -> 2f
            name.contains("Brightness", ignoreCase = true) -> 2000f
            name.contains("White", ignoreCase = true) -> 500f
            name.contains("Distance", ignoreCase = true) -> 500f
            name.contains("Radius", ignoreCase = true) -> 500f
            name.contains("Factor", ignoreCase = true) -> 5f
            name.contains("Detail", ignoreCase = true) -> 5f
            name.contains("Rate", ignoreCase = true) -> 500f
            else -> (currentValue + 50f).coerceAtMost(1000f)
        }
    }
    
    fun buildModifiedXml(parsed: ParsedXml): String {
        val modifiedLines = parsed.allLines.toMutableList()
        
        for (field in parsed.fields.filter { it.isModified }) {
            if (field.lineNumber > 0 && field.lineNumber <= modifiedLines.size) {
                modifiedLines[field.lineNumber - 1] = field.toXmlLine()
            }
        }
        
        return modifiedLines.joinToString("\n")
    }
}
