package com.rdrgraphics.editor.blackbox

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.entity.AppInfo
import java.io.File

object BlackBoxManager {
    private const val TAG = "RDR_BlackBox"
    private const val GAME_PACKAGE = "com.netflix.NGP.Kamo"
    
    private var initialized = false
    
    data class InstallResult(
        val success: Boolean,
        val message: String,
        val packageName: String? = null
    )
    
    fun initialize(context: Context): Boolean {
        return try {
            if (initialized) {
                Log.i(TAG, "BlackBox already initialized")
                return true
            }
            
            Log.i(TAG, "Initializing BlackBox...")
            BlackBoxCore.get().doAttachBaseContext(context, object : BlackBoxCore.OnBlackBoxCallback {
                override fun onCreateProcess(processName: String?) {
                    Log.d(TAG, "Process created: $processName")
                }
                
                override fun onGetComponent(): Class<*>? {
                    return null
                }
                
                override fun onGetApplication(): Class<*>? {
                    return null
                }
            })
            
            BlackBoxCore.get().doCreate()
            
            initialized = true
            Log.i(TAG, "BlackBox initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize BlackBox", e)
            false
        }
    }
    
    fun isGameInstalledInVirtualEnv(): Boolean {
        return try {
            if (!initialized) return false
            
            val installedApps = BlackBoxCore.get().installedApplications
            installedApps.any { it.packageName == GAME_PACKAGE }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if game is installed", e)
            false
        }
    }
    
    fun installApkInVirtualEnv(apkPath: String): InstallResult {
        return try {
            if (!initialized) {
                return InstallResult(false, "BlackBox not initialized")
            }
            
            Log.i(TAG, "Installing APK in virtual environment: $apkPath")
            
            val file = File(apkPath)
            if (!file.exists()) {
                return InstallResult(false, "APK file not found")
            }
            
            BlackBoxCore.get().installPackageAsUser(apkPath, 0)
            
            // Verify installation
            if (isGameInstalledInVirtualEnv()) {
                Log.i(TAG, "Game installed successfully in virtual environment")
                InstallResult(true, "Game installed in virtual environment", GAME_PACKAGE)
            } else {
                InstallResult(false, "Installation completed but verification failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error installing APK", e)
            InstallResult(false, "Installation failed: ${e.message}")
        }
    }
    
    fun installApkFromUri(context: Context, uri: Uri): InstallResult {
        return try {
            // Copy APK to cache
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return InstallResult(false, "Cannot open APK file")
            }
            
            val cacheFile = File(context.cacheDir, "temp_install.apk")
            cacheFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            Log.i(TAG, "APK copied to cache: ${cacheFile.absolutePath}, size: ${cacheFile.length()}")
            
            // Install from cache
            val result = installApkInVirtualEnv(cacheFile.absolutePath)
            
            // Clean up
            cacheFile.delete()
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error installing from URI", e)
            InstallResult(false, "Failed to copy APK: ${e.message}")
        }
    }
    
    fun uninstallGameFromVirtualEnv(): Boolean {
        return try {
            if (!initialized) return false
            
            Log.i(TAG, "Uninstalling game from virtual environment")
            BlackBoxCore.get().uninstallPackageAsUser(GAME_PACKAGE, 0)
            
            val stillInstalled = isGameInstalledInVirtualEnv()
            if (!stillInstalled) {
                Log.i(TAG, "Game uninstalled successfully")
            }
            !stillInstalled
        } catch (e: Exception) {
            Log.e(TAG, "Error uninstalling game", e)
            false
        }
    }
    
    fun launchGameInVirtualEnv(context: Context): Boolean {
        return try {
            if (!initialized) {
                Log.e(TAG, "BlackBox not initialized")
                return false
            }
            
            if (!isGameInstalledInVirtualEnv()) {
                Log.e(TAG, "Game not installed in virtual environment")
                return false
            }
            
            Log.i(TAG, "Launching game in virtual environment")
            BlackBoxCore.get().launchApk(GAME_PACKAGE, 0)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error launching game", e)
            false
        }
    }
    
    fun getInstalledApps(): List<AppInfo> {
        return try {
            if (!initialized) return emptyList()
            BlackBoxCore.get().installedApplications
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed apps", e)
            emptyList()
        }
    }
    
    fun getGameInfo(): String {
        return try {
            if (!initialized) {
                return "BlackBox not initialized"
            }
            
            val apps = getInstalledApps()
            val gameApp = apps.find { it.packageName == GAME_PACKAGE }
            
            if (gameApp != null) {
                buildString {
                    appendLine("Game: ${gameApp.name}")
                    appendLine("Package: ${gameApp.packageName}")
                    appendLine("Environment: Virtual (BlackBox)")
                    appendLine("User ID: 0")
                    appendLine("Data Path: ${getVirtualDataPath()}")
                }
            } else {
                "Game not installed in virtual environment"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    fun getVirtualDataPath(): String {
        return "/data/data/com.rdrgraphics.editor/virtual/data/user/0/$GAME_PACKAGE"
    }
    
    fun getVirtualGraphicsXmlPath(): String {
        return "${getVirtualDataPath()}/files/graphics.xml"
    }
    
    fun readGraphicsXmlFromVirtual(): String? {
        return try {
            val path = getVirtualGraphicsXmlPath()
            val file = File(path)
            if (file.exists()) {
                file.readText()
            } else {
                Log.w(TAG, "graphics.xml not found in virtual environment: $path")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading graphics.xml from virtual environment", e)
            null
        }
    }
    
    fun writeGraphicsXmlToVirtual(content: String): Boolean {
        return try {
            val path = getVirtualGraphicsXmlPath()
            val file = File(path)
            
            // Create parent directories if needed
            file.parentFile?.mkdirs()
            
            file.writeText(content)
            Log.i(TAG, "graphics.xml written to virtual environment: $path")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error writing graphics.xml to virtual environment", e)
            false
        }
    }
}
