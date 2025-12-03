package com.rdrgraphics.editor.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL

object GofileUploader {
    private const val TAG = "GofileUploader"
    private const val API_URL = "https://api.gofile.io"
    
    data class UploadResult(
        val success: Boolean,
        val downloadUrl: String? = null,
        val error: String? = null
    )
    
    suspend fun uploadFile(file: File, onProgress: (Float) -> Unit = {}): UploadResult = withContext(Dispatchers.IO) {
        try {
            val server = getBestServer()
            if (server == null) {
                return@withContext UploadResult(false, error = "Could not get upload server")
            }
            
            val uploadUrl = "https://$server.gofile.io/uploadFile"
            val connection = URL(uploadUrl).openConnection() as HttpURLConnection
            val boundary = "----WebKitFormBoundary${System.currentTimeMillis()}"
            
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            
            val fileSize = file.length()
            var bytesWritten = 0L
            
            connection.outputStream.use { output ->
                output.write("--$boundary\r\n".toByteArray())
                output.write("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"\r\n".toByteArray())
                output.write("Content-Type: application/octet-stream\r\n\r\n".toByteArray())
                
                FileInputStream(file).use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesWritten += bytesRead
                        onProgress(bytesWritten.toFloat() / fileSize)
                    }
                }
                
                output.write("\r\n--$boundary--\r\n".toByteArray())
            }
            
            val responseCode = connection.responseCode
            if (responseCode != 200) {
                return@withContext UploadResult(false, error = "Upload failed with code $responseCode")
            }
            
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            
            if (json.getString("status") == "ok") {
                val data = json.getJSONObject("data")
                val downloadPage = data.getString("downloadPage")
                UploadResult(true, downloadUrl = downloadPage)
            } else {
                UploadResult(false, error = "Upload failed: ${json.optString("message", "Unknown error")}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
            UploadResult(false, error = e.message ?: "Unknown error")
        }
    }
    
    private suspend fun getBestServer(): String? = withContext(Dispatchers.IO) {
        try {
            val connection = URL("$API_URL/getServer").openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            
            if (json.getString("status") == "ok") {
                json.getJSONObject("data").getString("server")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting server", e)
            null
        }
    }
}
