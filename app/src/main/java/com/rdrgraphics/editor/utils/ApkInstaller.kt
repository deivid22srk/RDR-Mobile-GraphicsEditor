package com.rdrgraphics.editor.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.topjohnwu.superuser.Shell
import java.io.File

object ApkInstaller {
    private const val TAG = "RDR_ApkInstaller"
    private const val GAME_PACKAGE = "com.netflix.NGP.Kamo"
    
    data class InstallResult(
        val success: Boolean,
        val message: String,
        val installedPath: String? = null
    )
    
    fun isGameInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(GAME_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    fun getGameInstallPath(): String? {
        return try {
            Log.i(TAG, "Detecting game installation path...")
            
            val possiblePaths = listOf(
                "/data/app/$GAME_PACKAGE-*/base.apk",
                "/data/app/~~*/$GAME_PACKAGE-*/base.apk",
                "/data/user/0/$GAME_PACKAGE/",
                "/data/data/$GAME_PACKAGE/"
            )
            
            for (pathPattern in possiblePaths) {
                val result = Shell.cmd("find ${pathPattern.substringBefore("*")} -name 'base.apk' 2>/dev/null | head -1").exec()
                if (result.isSuccess && result.out.isNotEmpty()) {
                    val path = result.out.firstOrNull()
                    if (!path.isNullOrEmpty()) {
                        Log.i(TAG, "Game found at: $path")
                        return path
                    }
                }
            }
            
            // Fallback: use pm path
            val pmResult = Shell.cmd("pm path $GAME_PACKAGE").exec()
            if (pmResult.isSuccess && pmResult.out.isNotEmpty()) {
                val path = pmResult.out.firstOrNull()?.removePrefix("package:")
                Log.i(TAG, "Game found via pm: $path")
                return path
            }
            
            Log.w(TAG, "Game path not found")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting game path", e)
            null
        }
    }
    
    fun getGameDataPaths(): List<String> {
        val paths = mutableListOf<String>()
        
        val basePaths = listOf(
            "/data/user/0/$GAME_PACKAGE",
            "/data/data/$GAME_PACKAGE",
            "/storage/emulated/0/Android/data/$GAME_PACKAGE",
            "/storage/emulated/0/Android/obb/$GAME_PACKAGE",
            "/data/media/0/Android/data/$GAME_PACKAGE"
        )
        
        for (path in basePaths) {
            val checkResult = Shell.cmd("test -d '$path' && echo 'EXISTS'").exec()
            if (checkResult.isSuccess && checkResult.out.firstOrNull() == "EXISTS") {
                paths.add(path)
                Log.d(TAG, "Found data path: $path")
            }
        }
        
        return paths
    }
    
    fun installApkWithRoot(apkPath: String): InstallResult {
        return try {
            Log.i(TAG, "Installing APK with root: $apkPath")
            
            if (!File(apkPath).exists()) {
                return InstallResult(false, "APK file not found: $apkPath")
            }
            
            // Method 1: pm install
            var result = Shell.cmd("pm install -r '$apkPath'").exec()
            
            if (result.isSuccess) {
                val output = result.out.joinToString("\n")
                Log.i(TAG, "Install output: $output")
                
                if (output.contains("Success")) {
                    val installedPath = getGameInstallPath()
                    return InstallResult(
                        true,
                        "Game installed successfully",
                        installedPath
                    )
                }
            }
            
            // Method 2: cmd package install
            result = Shell.cmd("cmd package install -r '$apkPath'").exec()
            
            if (result.isSuccess) {
                val output = result.out.joinToString("\n")
                if (output.contains("Success")) {
                    val installedPath = getGameInstallPath()
                    return InstallResult(
                        true,
                        "Game installed successfully",
                        installedPath
                    )
                }
            }
            
            val errorMsg = result.err.joinToString("\n").ifEmpty { "Unknown error" }
            Log.e(TAG, "Install failed: $errorMsg")
            InstallResult(false, "Installation failed: $errorMsg")
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during install", e)
            InstallResult(false, "Exception: ${e.message}")
        }
    }
    
    fun createInstallIntent(context: Context, apkFile: File): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
    
    fun copyApkToCache(context: Context, sourceUri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                Log.e(TAG, "Cannot open input stream for URI: $sourceUri")
                return null
            }
            
            val cacheFile = File(context.cacheDir, "temp_game.apk")
            cacheFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            
            Log.i(TAG, "APK copied to cache: ${cacheFile.absolutePath}, size: ${cacheFile.length()}")
            cacheFile
        } catch (e: Exception) {
            Log.e(TAG, "Error copying APK to cache", e)
            null
        }
    }
    
    fun getGameInfo(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(GAME_PACKAGE, 0)
            val sb = StringBuilder()
            
            sb.appendLine("Game: RDR Mobile")
            sb.appendLine("Package: $GAME_PACKAGE")
            sb.appendLine("Version: ${packageInfo.versionName}")
            sb.appendLine("Version Code: ${packageInfo.longVersionCode}")
            sb.appendLine("Install Path: ${getGameInstallPath()}")
            sb.appendLine("\nData Paths:")
            getGameDataPaths().forEach {
                sb.appendLine("  - $it")
            }
            
            sb.toString()
        } catch (e: Exception) {
            "Game not installed"
        }
    }
}
