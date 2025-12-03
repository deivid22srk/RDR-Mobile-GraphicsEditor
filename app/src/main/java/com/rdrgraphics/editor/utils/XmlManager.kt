package com.rdrgraphics.editor.utils

import android.util.Log
import com.topjohnwu.superuser.Shell
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.xml.sax.InputSource

object XmlManager {
    private const val TAG = "RDR_XmlManager"
    private const val GRAPHICS_PATH = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
    
    data class XmlValue(
        val tag: String,
        val value: String
    )
    
    fun readGraphicsXml(): String? {
        return try {
            Log.i(TAG, "Reading graphics.xml from: $GRAPHICS_PATH")
            val result = Shell.cmd("cat '$GRAPHICS_PATH'").exec()
            
            if (result.isSuccess && result.out.isNotEmpty()) {
                val content = result.out.joinToString("\n")
                Log.d(TAG, "Read ${content.length} bytes from graphics.xml")
                content
            } else {
                Log.w(TAG, "Graphics.xml not found or empty")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading graphics.xml", e)
            null
        }
    }
    
    fun parseXmlToMap(xmlContent: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(InputSource(StringReader(xmlContent)))
            
            val root = doc.documentElement
            val nodeList = root.childNodes
            
            for (i in 0 until nodeList.length) {
                val node = nodeList.item(i)
                if (node is Element) {
                    val tagName = node.tagName
                    val value = node.getAttribute("value")
                    if (value.isNotEmpty()) {
                        map[tagName] = value
                        Log.d(TAG, "Parsed: $tagName = $value")
                    }
                }
            }
            
            Log.i(TAG, "Parsed ${map.size} values from XML")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML", e)
        }
        
        return map
    }
    
    fun updateXmlValues(originalXml: String, updates: Map<String, String>): String {
        return try {
            Log.i(TAG, "Updating XML with ${updates.size} changes")
            
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(InputSource(StringReader(originalXml)))
            
            val root = doc.documentElement
            val nodeList = root.childNodes
            
            var changesApplied = 0
            
            for (i in 0 until nodeList.length) {
                val node = nodeList.item(i)
                if (node is Element) {
                    val tagName = node.tagName
                    if (updates.containsKey(tagName)) {
                        val newValue = updates[tagName]!!
                        node.setAttribute("value", newValue)
                        Log.d(TAG, "Updated: $tagName -> $newValue")
                        changesApplied++
                    }
                }
            }
            
            Log.i(TAG, "Applied $changesApplied changes to XML")
            
            // Convert back to string
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no")
            
            val writer = java.io.StringWriter()
            transformer.transform(DOMSource(doc), StreamResult(writer))
            
            writer.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating XML", e)
            originalXml // Return original if update fails
        }
    }
    
    fun writeGraphicsXml(content: String): Boolean {
        return try {
            Log.i(TAG, "Writing graphics.xml")
            
            val tempFile = File.createTempFile("graphics", ".xml")
            tempFile.writeText(content)
            
            Log.d(TAG, "Temp file: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")
            
            val result = Shell.cmd(
                "mkdir -p /data/user/0/com.netflix.NGP.Kamo/files",
                "cp '${tempFile.absolutePath}' '$GRAPHICS_PATH'",
                "chmod 644 '$GRAPHICS_PATH'",
                "chown \$(stat -c '%u:%g' /data/user/0/com.netflix.NGP.Kamo/files) '$GRAPHICS_PATH'",
                "sync"
            ).exec()
            
            Log.i(TAG, "Write result: exit code = ${result.code}")
            
            if (result.isSuccess) {
                // Verify
                val verify = Shell.cmd("head -3 '$GRAPHICS_PATH'").exec()
                if (verify.isSuccess) {
                    Log.d(TAG, "Verification: ${verify.out.joinToString("\n")}")
                }
            } else {
                Log.e(TAG, "Write failed: ${result.err.joinToString("\n")}")
            }
            
            tempFile.delete()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Error writing graphics.xml", e)
            false
        }
    }
}
