package com.rdrgraphics.editor.data

import java.text.SimpleDateFormat
import java.util.*

data class LanguageConfig(
    val profileId: String = "Cracked",
    val language: String = "en-US",
    val slots: String = ""
) {
    fun toProperties(): String {
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val currentDate = dateFormat.format(Date())
        
        return """#$currentDate
PROFILE_ID=$profileId
LANGUAGE=$language
SLOTS=$slots
"""
    }
}
