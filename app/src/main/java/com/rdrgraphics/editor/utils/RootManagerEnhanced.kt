package com.rdrgraphics.editor.utils

import android.util.Log
import com.topjohnwu.superuser.Shell
import java.io.File

object RootManagerEnhanced {
    private const val TAG = "RDRGraphicsEditor"
    private const val GAME_PACKAGE = "com.netflix.NGP.Kamo"
    private const val GRAPHICS_PATH = "/data/user/0/$GAME_PACKAGE/files/graphics.xml"
    private const val LANGUAGE_PATH = "/storage/emulated/0/Android/data/$GAME_PACKAGE/files/netflix.dat"
    
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
            val isRoot = Shell.getShell().isRoot
            Log.d(TAG, "Root available: $isRoot")
            isRoot
        } catch (e: Exception) {
            Log.e(TAG, "Root check failed", e)
            false
        }
    }

    fun isGameInstalled(): Boolean {
        return try {
            val result = Shell.cmd("pm list packages | grep $GAME_PACKAGE").exec()
            val installed = result.isSuccess && result.out.isNotEmpty()
            Log.d(TAG, "Game installed: $installed")
            installed
        } catch (e: Exception) {
            Log.e(TAG, "Game check failed", e)
            false
        }
    }

    fun backupGraphicsConfig(): String? {
        return try {
            if (!fileExists(GRAPHICS_PATH)) {
                Log.w(TAG, "Graphics file doesn't exist, nothing to backup")
                return null
            }
            
            val timestamp = System.currentTimeMillis()
            val backupPath = "/sdcard/Download/graphics_backup_$timestamp.xml"
            
            val result = Shell.cmd("cp '$GRAPHICS_PATH' '$backupPath'").exec()
            if (result.isSuccess) {
                Log.i(TAG, "Backup created: $backupPath")
                backupPath
            } else {
                Log.e(TAG, "Backup failed: ${result.err.joinToString()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backup exception", e)
            null
        }
    }

    fun writeGraphicsConfig(content: String): Boolean {
        return try {
            Log.i(TAG, "Starting writeGraphicsConfig")
            
            if (!isGameInstalled()) {
                Log.e(TAG, "Game is not installed")
                return false
            }

            backupGraphicsConfig()
            
            val tempFile = File.createTempFile("graphics", ".xml")
            tempFile.writeText(content)
            Log.d(TAG, "Temp file created: ${tempFile.absolutePath}")
            
            val targetDir = "/data/user/0/$GAME_PACKAGE/files"
            val commands = arrayOf(
                "mkdir -p '$targetDir'",
                "cp '${tempFile.absolutePath}' '$GRAPHICS_PATH'",
                "chmod 644 '$GRAPHICS_PATH'",
                "chown \$(stat -c '%u:%g' '$targetDir') '$GRAPHICS_PATH'"
            )
            
            Log.d(TAG, "Executing commands: ${commands.joinToString(" && ")}")
            val result = Shell.cmd(*commands).exec()
            
            tempFile.delete()
            
            if (result.isSuccess) {
                Log.i(TAG, "Graphics config written successfully")
                Log.d(TAG, "Command output: ${result.out.joinToString("\n")}")
                
                val verification = verifyFileContent(GRAPHICS_PATH, content)
                if (!verification) {
                    Log.e(TAG, "Content verification failed!")
                }
                return verification
            } else {
                Log.e(TAG, "Write failed: ${result.err.joinToString("\n")}")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "writeGraphicsConfig exception", e)
            e.printStackTrace()
            false
        }
    }

    fun updateLanguageOnly(newLanguage: String): Boolean {
        return try {
            Log.i(TAG, "Updating language to: $newLanguage")
            
            val readResult = Shell.cmd("cat '$LANGUAGE_PATH'").exec()
            if (!readResult.isSuccess || readResult.out.isEmpty()) {
                Log.e(TAG, "Failed to read language file")
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
            
            val targetDir = "/storage/emulated/0/Android/data/$GAME_PACKAGE/files"
            val commands = arrayOf(
                "mkdir -p '$targetDir'",
                "cp '${tempFile.absolutePath}' '$LANGUAGE_PATH'",
                "chmod 644 '$LANGUAGE_PATH'",
                "chown \$(stat -c '%u:%g' '$targetDir') '$LANGUAGE_PATH'"
            )
            
            val writeResult = Shell.cmd(*commands).exec()
            
            tempFile.delete()
            
            if (writeResult.isSuccess) {
                Log.i(TAG, "Language updated successfully")
                return true
            } else {
                Log.e(TAG, "Language update failed: ${writeResult.err.joinToString()}")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateLanguageOnly exception", e)
            e.printStackTrace()
            false
        }
    }

    fun readGraphicsConfig(): String? {
        return try {
            Log.d(TAG, "Reading graphics config from: $GRAPHICS_PATH")
            val result = Shell.cmd("cat '$GRAPHICS_PATH'").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                val content = result.out.joinToString("\n")
                Log.d(TAG, "Graphics config read successfully (${content.length} chars)")
                content
            } else {
                Log.w(TAG, "Graphics config not found or empty")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "readGraphicsConfig exception", e)
            e.printStackTrace()
            null
        }
    }

    fun readLanguageConfig(): String? {
        return try {
            val result = Shell.cmd("cat '$LANGUAGE_PATH'").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                result.out.joinToString("\n")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "readLanguageConfig exception", e)
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
            Log.e(TAG, "getCurrentLanguage exception", e)
            e.printStackTrace()
            null
        }
    }

    private fun fileExists(path: String): Boolean {
        return try {
            val result = Shell.cmd("test -f '$path' && echo 'exists'").exec()
            result.isSuccess && result.out.firstOrNull() == "exists"
        } catch (e: Exception) {
            false
        }
    }

    private fun verifyFileContent(path: String, expectedContent: String): Boolean {
        return try {
            val result = Shell.cmd("cat '$path'").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                val actualContent = result.out.joinToString("\n")
                val matches = actualContent.trim() == expectedContent.trim()
                
                if (!matches) {
                    Log.e(TAG, "Content mismatch!")
                    Log.e(TAG, "Expected length: ${expectedContent.length}")
                    Log.e(TAG, "Actual length: ${actualContent.length}")
                    Log.e(TAG, "First 100 chars expected: ${expectedContent.take(100)}")
                    Log.e(TAG, "First 100 chars actual: ${actualContent.take(100)}")
                }
                
                matches
            } else {
                Log.e(TAG, "Verification read failed")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Verification exception", e)
            false
        }
    }

    fun getErrorLog(): String {
        return try {
            val result = Shell.cmd("logcat -d -s $TAG:* *:E | tail -100").exec()
            result.out.joinToString("\n")
        } catch (e: Exception) {
            e.toString()
        }
    }

    fun getDetailedSystemInfo(): String {
        return try {
            val sb = StringBuilder()
            
            sb.appendLine("=== System Information ===")
            sb.appendLine("Root available: ${isRootAvailable()}")
            sb.appendLine("Game installed: ${isGameInstalled()}")
            sb.appendLine("Graphics file exists: ${fileExists(GRAPHICS_PATH)}")
            sb.appendLine("Language file exists: ${fileExists(LANGUAGE_PATH)}")
            
            val selinuxResult = Shell.cmd("getenforce").exec()
            if (selinuxResult.isSuccess) {
                sb.appendLine("SELinux mode: ${selinuxResult.out.joinToString()}")
            }
            
            val magiskResult = Shell.cmd("magisk -v").exec()
            if (magiskResult.isSuccess) {
                sb.appendLine("Magisk version: ${magiskResult.out.joinToString()}")
            }
            
            val gameDataResult = Shell.cmd("ls -la /data/user/0/$GAME_PACKAGE/").exec()
            if (gameDataResult.isSuccess) {
                sb.appendLine("\nGame data directory:")
                sb.appendLine(gameDataResult.out.joinToString("\n"))
            }
            
            sb.toString()
        } catch (e: Exception) {
            "Error getting system info: ${e.message}"
        }
    }
}
