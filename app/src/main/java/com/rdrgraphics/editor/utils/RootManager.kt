package com.rdrgraphics.editor.utils

import android.util.Log
import com.topjohnwu.superuser.Shell
import com.rdrgraphics.editor.data.GraphicsConfig
import java.io.File

object RootManager {
    private const val TAG = "RDR_RootManager"
    
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

    fun writeGraphicsConfig(config: GraphicsConfig): Boolean {
        return try {
            Log.i(TAG, "Starting writeGraphicsConfig with MERGE strategy")
            
            // 1. Try to read existing file first
            val existingXml = XmlManager.readGraphicsXml()
            
            val finalXml = if (existingXml != null) {
                Log.i(TAG, "Found existing graphics.xml, merging changes...")
                
                // Parse existing values
                val existingValues = XmlManager.parseXmlToMap(existingXml)
                Log.d(TAG, "Existing values: ${existingValues.size} entries")
                
                // Create updates map with only changed values
                val updates = config.toUpdateMap()
                Log.d(TAG, "New values: ${updates.size} updates")
                
                // Merge: keep existing values, override with new ones
                XmlManager.updateXmlValues(existingXml, updates)
            } else {
                Log.w(TAG, "No existing graphics.xml found, creating new file")
                // No existing file, create fresh XML
                config.toXml()
            }
            
            // 2. Write the merged/new XML
            val success = XmlManager.writeGraphicsXml(finalXml)
            
            if (success) {
                Log.i(TAG, "✓ Graphics config written successfully")
            } else {
                Log.e(TAG, "✗ Failed to write graphics config")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception in writeGraphicsConfig", e)
            e.printStackTrace()
            false
        }
    }
    
    fun readGraphicsConfig(): GraphicsConfig? {
        return try {
            Log.i(TAG, "Reading current graphics config")
            val xml = XmlManager.readGraphicsXml() ?: return null
            
            val values = XmlManager.parseXmlToMap(xml)
            if (values.isEmpty()) {
                Log.w(TAG, "No values parsed from XML")
                return null
            }
            
            // Convert map to GraphicsConfig
            GraphicsConfig.fromMap(values)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading graphics config", e)
            null
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
            val path = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
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
