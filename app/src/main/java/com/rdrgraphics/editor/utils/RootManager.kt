package com.rdrgraphics.editor.utils

import android.util.Log
import com.rdrgraphics.editor.data.FileItem
import com.rdrgraphics.editor.data.XmlParser
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import java.io.File

object RootManager {
    private const val TAG = "RootManager"
    const val GRAPHICS_PATH = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
    
    init {
        Shell.enableVerboseLogging = false
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
            Log.e(TAG, "Error checking root", e)
            false
        }
    }

    fun writeGraphicsConfig(content: String): Boolean {
        return writeXmlFile(GRAPHICS_PATH, content)
    }
    
    fun writePartialGraphicsConfig(parsedXml: XmlParser.ParsedXml): Boolean {
        return writePartialXmlFile(GRAPHICS_PATH, parsedXml)
    }
    
    fun writePartialXmlFile(path: String, parsedXml: XmlParser.ParsedXml): Boolean {
        return try {
            val modifiedContent = XmlParser.buildModifiedXml(parsedXml)
            writeXmlFile(path, modifiedContent)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing partial XML file: $path", e)
            false
        }
    }
    
    fun writeXmlFile(path: String, content: String): Boolean {
        return try {
            val file = SuFile.open(path)
            
            val dir = file.parentFile
            if (dir != null && !dir.exists()) {
                dir.mkdirs()
            }
            
            SuFileOutputStream.open(file).use { output ->
                output.write(content.toByteArray(Charsets.UTF_8))
            }
            
            file.setReadable(true, false)
            file.setWritable(true, false)
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error writing XML file: $path", e)
            false
        }
    }

    fun readGraphicsConfig(): String? {
        return readXmlFile(GRAPHICS_PATH)
    }
    
    fun readXmlFile(path: String): String? {
        return try {
            val file = SuFile.open(path)
            if (!file.exists() || !file.canRead()) {
                Log.w(TAG, "File does not exist or cannot be read: $path")
                return null
            }
            
            SuFileInputStream.open(file).use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading file: $path", e)
            null
        }
    }
    
    fun parseGraphicsConfig(): XmlParser.ParsedXml? {
        return parseXmlFile(GRAPHICS_PATH)
    }
    
    fun parseXmlFile(path: String): XmlParser.ParsedXml? {
        return try {
            val content = readXmlFile(path) ?: return null
            XmlParser.parseGraphicsXml(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML file: $path", e)
            null
        }
    }

    fun updateLanguageOnly(newLanguage: String): Boolean {
        return try {
            val path = "/storage/emulated/0/Android/data/com.netflix.NGP.Kamo/files/netflix.dat"
            val file = SuFile.open(path)
            
            if (!file.exists()) {
                return false
            }
            
            val currentContent = SuFileInputStream.open(file).use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            }
            
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
            
            SuFileOutputStream.open(file).use { output ->
                output.write(newContent.toByteArray(Charsets.UTF_8))
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating language", e)
            false
        }
    }

    fun readLanguageConfig(): String? {
        return try {
            val path = "/storage/emulated/0/Android/data/com.netflix.NGP.Kamo/files/netflix.dat"
            readXmlFile(path)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading language config", e)
            null
        }
    }

    fun getCurrentLanguage(): String? {
        return try {
            val content = readLanguageConfig() ?: return null
            content.lines().firstOrNull { it.startsWith("LANGUAGE=") }
                ?.substringAfter("LANGUAGE=")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current language", e)
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
    
    fun listDirectory(path: String): List<FileItem> {
        return try {
            val dir = SuFile.open(path)
            
            if (!dir.exists() || !dir.isDirectory) {
                Log.w(TAG, "Directory does not exist or is not a directory: $path")
                return emptyList()
            }
            
            val files = dir.listFiles() ?: return emptyList()
            
            val items = files.mapNotNull { file ->
                try {
                    val name = file.name
                    val fullPath = file.absolutePath
                    val isDirectory = file.isDirectory
                    val size = if (isDirectory) 0L else file.length()
                    val isXml = name.endsWith(".xml", ignoreCase = true)
                    
                    val permissions = buildString {
                        append(if (isDirectory) 'd' else '-')
                        append(if (file.canRead()) 'r' else '-')
                        append(if (file.canWrite()) 'w' else '-')
                        append(if (file.canExecute()) 'x' else '-')
                        append("------")
                    }
                    
                    FileItem(
                        name = name,
                        path = fullPath,
                        isDirectory = isDirectory,
                        size = size,
                        permissions = permissions,
                        isXml = isXml
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error processing file: ${file.name}", e)
                    null
                }
            }
            
            items.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name })
        } catch (e: Exception) {
            Log.e(TAG, "Error listing directory: $path", e)
            emptyList()
        }
    }
    
    fun directoryExists(path: String): Boolean {
        return try {
            val dir = SuFile.open(path)
            dir.exists() && dir.isDirectory
        } catch (e: Exception) {
            Log.e(TAG, "Error checking directory: $path", e)
            false
        }
    }
}
