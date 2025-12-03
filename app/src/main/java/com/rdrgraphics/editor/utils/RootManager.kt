package com.rdrgraphics.editor.utils

import android.util.Log
import com.rdrgraphics.editor.data.XmlParser
import com.topjohnwu.superuser.Shell
import java.io.File

object RootManager {
    private const val TAG = "RootManager"
    const val GRAPHICS_PATH = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
    init {
        Shell.enableVerboseLogging = true
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }

    fun isRootAvailable(): Boolean {
        return try {
            Shell.getShell().isRoot
        } catch (e: Exception) {
            false
        }
    }

    fun writeGraphicsConfig(content: String): Boolean {
        return try {
            val tempFile = File.createTempFile("graphics", ".xml")
            tempFile.writeText(content)
            
            val result = Shell.cmd(
                "mkdir -p /data/user/0/com.netflix.NGP.Kamo/files",
                "cp '${tempFile.absolutePath}' '$GRAPHICS_PATH'",
                "chmod 644 '$GRAPHICS_PATH'",
                "chown $(stat -c '%u:%g' /data/user/0/com.netflix.NGP.Kamo/files) '$GRAPHICS_PATH'"
            ).exec()
            
            tempFile.delete()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Error writing graphics config", e)
            false
        }
    }
    
    fun writePartialGraphicsConfig(parsedXml: XmlParser.ParsedXml): Boolean {
        return try {
            val modifiedContent = XmlParser.buildModifiedXml(parsedXml)
            writeGraphicsConfig(modifiedContent)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing partial graphics config", e)
            false
        }
    }

    fun updateLanguageOnly(newLanguage: String): Boolean {
        return try {
            val path = "/storage/emulated/0/Android/data/com.netflix.NGP.Kamo/files/netflix.dat"
            
            val readResult = Shell.cmd("cat '$path'").exec()
            if (!readResult.isSuccess || readResult.out.isEmpty()) {
                return false
            }
            
            val currentContent = readResult.out.joinToString("\n")
            val lines = currentContent.lines().toMutableList()
            
            var languageLineFound = false
            for (i in lines.indices) {
                if (lines[i].startsWith("LANGUAGE=")) {
                    lines[i] = "LANGUAGE=$newLanguage"
                    languageLineFound = true
                    break
                }
            }
            
            if (!languageLineFound) {
                lines.add("LANGUAGE=$newLanguage")
            }
            
            val newContent = lines.joinToString("\n")
            val tempFile = File.createTempFile("netflix", ".dat")
            tempFile.writeText(newContent)
            
            val writeResult = Shell.cmd(
                "mkdir -p /storage/emulated/0/Android/data/com.netflix.NGP.Kamo/files",
                "cp '${tempFile.absolutePath}' '$path'",
                "chmod 644 '$path'",
                "chown $(stat -c '%u:%g' /storage/emulated/0/Android/data/com.netflix.NGP.Kamo/files) '$path'"
            ).exec()
            
            tempFile.delete()
            writeResult.isSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readGraphicsConfig(): String? {
        return try {
            val result = Shell.cmd("cat '$GRAPHICS_PATH'").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                result.out.joinToString("\n")
            } else {
                Log.w(TAG, "Could not read graphics config")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading graphics config", e)
            null
        }
    }
    
    fun parseGraphicsConfig(): XmlParser.ParsedXml? {
        return try {
            val content = readGraphicsConfig() ?: return null
            XmlParser.parseGraphicsXml(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing graphics config", e)
            null
        }
    }

    fun readLanguageConfig(): String? {
        return try {
            val path = "/storage/emulated/0/Android/data/com.netflix.NGP.Kamo/files/netflix.dat"
            val result = Shell.cmd("cat '$path'").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                result.out.joinToString("\n")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getCurrentLanguage(): String? {
        return try {
            val content = readLanguageConfig() ?: return null
            content.lines().firstOrNull { it.startsWith("LANGUAGE=") }
                ?.substringAfter("LANGUAGE=")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getErrorLog(): String {
        return try {
            val result = Shell.cmd("logcat -d -s RDRGraphicsEditor:* *:E").exec()
            result.out.joinToString("\n")
        } catch (e: Exception) {
            e.toString()
        }
    }
}
