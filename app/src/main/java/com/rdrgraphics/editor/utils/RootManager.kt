package com.rdrgraphics.editor.utils

import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileOutputStream

object RootManager {
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
            false
        }
    }

    fun writeGraphicsConfig(content: String): Boolean {
        return try {
            val path = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
            val file = SuFile.open(path)
            
            file.parentFile?.let {
                if (!it.exists()) {
                    Shell.cmd("mkdir -p ${it.absolutePath}").exec()
                }
            }
            
            SuFileOutputStream.open(file).use { output ->
                output.write(content.toByteArray())
                output.flush()
            }
            
            Shell.cmd("chmod 644 $path").exec()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun writeLanguageConfig(content: String): Boolean {
        return try {
            val path = "/storage/emulated/0/Android/data/com.netflix.NGP.Kamo/files/netflix.dat"
            val file = SuFile.open(path)
            
            file.parentFile?.let {
                if (!it.exists()) {
                    Shell.cmd("mkdir -p ${it.absolutePath}").exec()
                }
            }
            
            SuFileOutputStream.open(file).use { output ->
                output.write(content.toByteArray())
                output.flush()
            }
            
            Shell.cmd("chmod 644 $path").exec()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readGraphicsConfig(): String? {
        return try {
            val path = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
            val file = SuFile.open(path)
            if (file.exists()) {
                file.readText()
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
            val file = SuFile.open(path)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
