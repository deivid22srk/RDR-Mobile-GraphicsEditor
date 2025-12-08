package com.rdrgraphics.editor.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.rdrgraphics.editor.data.XmlParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object FileManager {
    private const val TAG = "FileManager"
    
    fun readXmlFile(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading file: $uri", e)
            null
        }
    }
    
    fun parseXmlFile(context: Context, uri: Uri): XmlParser.ParsedXml? {
        return try {
            val content = readXmlFile(context, uri) ?: return null
            XmlParser.parseGraphicsXml(content)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML file: $uri", e)
            null
        }
    }
    
    fun writeXmlFile(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(content)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error writing XML file: $uri", e)
            false
        }
    }
    
    fun writePartialXmlFile(context: Context, uri: Uri, parsedXml: XmlParser.ParsedXml): Boolean {
        return try {
            val modifiedContent = XmlParser.buildModifiedXml(parsedXml)
            writeXmlFile(context, uri, modifiedContent)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing partial XML file: $uri", e)
            false
        }
    }
    
    fun getFileName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        cursor.getString(displayNameIndex)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file name: $uri", e)
            uri.lastPathSegment
        }
    }
}
