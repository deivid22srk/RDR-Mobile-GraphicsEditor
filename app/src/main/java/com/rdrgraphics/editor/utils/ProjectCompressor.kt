package com.rdrgraphics.editor.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ProjectCompressor {
    private const val TAG = "ProjectCompressor"
    
    suspend fun compressProject(context: Context, onProgress: (Float) -> Unit = {}): File? = withContext(Dispatchers.IO) {
        try {
            val projectRoot = File(context.filesDir, "../../..").canonicalFile
            val outputFile = File(context.cacheDir, "RDR-Graphics-Editor-${System.currentTimeMillis()}.zip")
            
            val filesToCompress = getAllFiles(projectRoot)
            val totalSize = filesToCompress.sumOf { it.length() }
            var compressedSize = 0L
            
            ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
                filesToCompress.forEach { file ->
                    try {
                        val relativePath = file.relativeTo(projectRoot).path
                        val zipEntry = ZipEntry(relativePath)
                        zipOut.putNextEntry(zipEntry)
                        
                        FileInputStream(file).use { input ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                zipOut.write(buffer, 0, bytesRead)
                            }
                        }
                        
                        zipOut.closeEntry()
                        compressedSize += file.length()
                        onProgress(compressedSize.toFloat() / totalSize)
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not compress file: ${file.path}", e)
                    }
                }
            }
            
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Compression error", e)
            null
        }
    }
    
    private fun getAllFiles(dir: File): List<File> {
        val files = mutableListOf<File>()
        
        dir.listFiles()?.forEach { file ->
            if (shouldInclude(file)) {
                if (file.isDirectory) {
                    files.addAll(getAllFiles(file))
                } else {
                    files.add(file)
                }
            }
        }
        
        return files
    }
    
    private fun shouldInclude(file: File): Boolean {
        val name = file.name
        val path = file.path
        
        if (name.startsWith(".") && name.length > 1) {
            return true
        }
        
        val excludedDirs = listOf(
            "build",
            ".gradle",
            ".idea",
            "captures",
            "local.properties"
        )
        
        return excludedDirs.none { excluded ->
            path.contains("/$excluded/") || path.endsWith("/$excluded")
        }
    }
}
