package com.rdrgraphics.editor.utils

import com.topjohnwu.superuser.Shell
import java.io.File

object RootManager {
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
            val path = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
            val tempFile = File.createTempFile("graphics", ".xml")
            tempFile.writeText(content)
            
            android.util.Log.d("RootManager", "Writing to $path from ${tempFile.absolutePath}")
            
            Shell.cmd("setenforce 0 2>/dev/null || true").exec()
            
            val result = Shell.cmd(
                "mkdir -p /data/user/0/com.netflix.NGP.Kamo/files",
                "cp '${tempFile.absolutePath}' '$path'",
                "chmod 666 '$path'",
                "chcon u:object_r:app_data_file:s0 '$path' 2>/dev/null || true"
            ).exec()
            
            Shell.cmd("setenforce 1 2>/dev/null || true").exec()
            
            tempFile.delete()
            
            android.util.Log.d("RootManager", "Write result: ${result.isSuccess}")
            result.isSuccess
        } catch (e: Exception) {
            android.util.Log.e("RootManager", "Error writing graphics config", e)
            e.printStackTrace()
            false
        }
    }

    fun updateGraphicsField(fieldName: String, newValue: String): Boolean {
        return try {
            val path = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
            
            val readResult = Shell.cmd("cat '$path'").exec()
            if (!readResult.isSuccess || readResult.out.isEmpty()) {
                return false
            }
            
            val currentContent = readResult.out.joinToString("\n")
            val lines = currentContent.lines().toMutableList()
            
            var fieldUpdated = false
            for (i in lines.indices) {
                val line = lines[i]
                if (line.contains("<$fieldName") && line.contains("value=")) {
                    val regex = Regex("""(<$fieldName\\s+value=\")[^\"]*(\".*/?>)""")
                    lines[i] = regex.replace(line, "$1$newValue$2")
                    fieldUpdated = true
                    break
                }
            }
            
            if (!fieldUpdated) return false
            
            val newContent = lines.joinToString("\n")
            val tempFile = File.createTempFile("graphics", ".xml")
            tempFile.writeText(newContent)
            
            val writeResult = Shell.cmd(
                "cp '${tempFile.absolutePath}' '$path'",
                "chmod 644 '$path'",
                "chown $(stat -c '%u:%g' /data/user/0/com.netflix.NGP.Kamo/files) '$path'"
            ).exec()
            
            tempFile.delete()
            writeResult.isSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updateMultipleGraphicsFields(updates: Map<String, String>): Boolean {
        return try {
            val path = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
            
            android.util.Log.d("RootManager", "Updating ${updates.size} fields")
            
            val currentContent = readGraphicsConfig()
            if (currentContent == null || currentContent.isEmpty()) {
                android.util.Log.e("RootManager", "Failed to read current config for update")
                return false
            }
            
            var modifiedContent: String = currentContent
            
            for ((fieldName, newValue) in updates) {
                val lines = modifiedContent.lines().toMutableList()
                
                for (i in lines.indices) {
                    val line = lines[i]
                    if (line.contains("<$fieldName") && line.contains("value=")) {
                        val regex = Regex("""(<$fieldName\\s+value=\")[^\"]*(\".*/?>)""")
                        lines[i] = regex.replace(line, "$1$newValue$2")
                        android.util.Log.d("RootManager", "Updated field $fieldName to $newValue")
                        break
                    }
                }
                
                modifiedContent = lines.joinToString("\n")
            }
            
            return writeGraphicsConfig(modifiedContent)
        } catch (e: Exception) {
            android.util.Log.e("RootManager", "Error updating multiple fields", e)
            e.printStackTrace()
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

    fun fileExists(path: String): Boolean {
        return try {
            val result = Shell.cmd(
                "su -c \"test -f '$path' && echo 'exists' || echo 'not_exists'\""
            ).exec()
            
            if (!result.isSuccess) {
                val altResult = Shell.cmd("[ -f '$path' ] && echo 'exists' || echo 'not_exists'").exec()
                return altResult.isSuccess && altResult.out.firstOrNull()?.trim() == "exists"
            }
            
            result.out.firstOrNull()?.trim() == "exists"
        } catch (e: Exception) {
            android.util.Log.e("RootManager", "Error checking file exists", e)
            e.printStackTrace()
            false
        }
    }

    fun readGraphicsConfig(): String? {
        return try {
            val path = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
            
            android.util.Log.d("RootManager", "Attempting to read: $path")
            
            var result = Shell.cmd(
                "su 0 cat '$path' 2>&1"
            ).exec()
            
            android.util.Log.d("RootManager", "Method 1 - su 0 cat: success=${result.isSuccess}, out=${result.out.size}, err=${result.err.size}")
            
            if (!result.isSuccess || result.out.isEmpty()) {
                android.util.Log.d("RootManager", "Trying method 2 - direct cat")
                result = Shell.cmd("cat '$path'").exec()
                android.util.Log.d("RootManager", "Method 2 - cat: success=${result.isSuccess}, out=${result.out.size}")
            }
            
            if (!result.isSuccess || result.out.isEmpty()) {
                android.util.Log.d("RootManager", "Trying method 3 - chcon + cat")
                Shell.cmd(
                    "chcon u:object_r:system_file:s0 '$path' 2>/dev/null || true",
                    "chmod 644 '$path' 2>/dev/null || true"
                ).exec()
                
                result = Shell.cmd("cat '$path'").exec()
                android.util.Log.d("RootManager", "Method 3 - after chcon: success=${result.isSuccess}, out=${result.out.size}")
            }
            
            if (!result.isSuccess || result.out.isEmpty()) {
                android.util.Log.d("RootManager", "Trying method 4 - setenforce + cat")
                Shell.cmd("setenforce 0 2>/dev/null || true").exec()
                result = Shell.cmd("cat '$path'").exec()
                Shell.cmd("setenforce 1 2>/dev/null || true").exec()
                android.util.Log.d("RootManager", "Method 4 - setenforce: success=${result.isSuccess}, out=${result.out.size}")
            }
            
            if (result.isSuccess && result.out.isNotEmpty()) {
                val content = result.out.joinToString("\n")
                android.util.Log.d("RootManager", "Successfully read ${content.length} bytes")
                return content
            } else {
                android.util.Log.e("RootManager", "All methods failed. Errors: ${result.err.joinToString("\n")}")
                android.util.Log.e("RootManager", "Output: ${result.out.joinToString("\n")}")
                return null
            }
        } catch (e: Exception) {
            android.util.Log.e("RootManager", "Exception reading graphics config", e)
            e.printStackTrace()
            null
        }
    }

    fun getDefaultGraphicsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8"?>
<rage__GraphicsOptions v="83.0">
	<Fullscreen value="false"/>
	<Monitor value="1"/>
	<ResolutionX value="1600"/>
	<ResolutionY value="720"/>
	<RefreshRate value="0.000000"/>
	<FrameRateLimit value="0"/>
	<Vsync value="1"/>
	<TripleBuffer value="true"/>
	<DynamicResolution value="true"/>
	<DynamicResolutionTargetFramerate value="0"/>
	<DynamicResolutionMinScaleIndex value="5"/>
	<MinAnisotropicFiltering value="4"/>
	<MotionBlurStyle value="1"/>
	<MotionBlurStrength value="0.100000"/>
	<ShadowQuality value="0"/>
	<ShadowSoftness value="0"/>
	<ShadowBlend value="0"/>
	<WorldStreamingRadius value="100.000000"/>
	<TerrainStreamingFactor value="1.000000"/>
	<TreeLevelOfDetail value="1.000000"/>
	<TreeImposterHighLodStreamingDistance value="0.000000"/>
	<GrassStreamingDistance value="0.000000"/>
	<bFocusPaused value="true"/>
	<bConstrainMouse value="false"/>
	<HDR value="false"/>
	<PeakBrightness value="100.000000"/>
	<PaperWhite value="80.000000"/>
	<aaAntiAliasing value="1"/>
	<FSR3UpscalingQuality value="0"/>
	<FSR3AdditionalSharpness value="0.000000"/>
	<NVIDIAReflex value="0"/>
	<DLSSUpscalingQuality value="0"/>
	<DLSSFrameGeneration value="0"/>
	<MobilePreset value="0"/>
	<ScreenPercentage value="1.000000"/>
</rage__GraphicsOptions>"""
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
