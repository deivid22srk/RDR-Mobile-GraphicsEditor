package com.rdrgraphics.editor.utils

import android.util.Log
import com.topjohnwu.superuser.Shell
import java.io.File

object DiagnosticManager {
    private const val TAG = "RDR_DIAGNOSTIC"
    private const val GAME_PACKAGE = "com.netflix.NGP.Kamo"
    
    data class DiagnosticResult(
        val success: Boolean,
        val message: String,
        val details: Map<String, String>
    )
    
    fun performFullDiagnostic(): DiagnosticResult {
        val details = mutableMapOf<String, String>()
        val messages = mutableListOf<String>()
        
        Log.i(TAG, "=== STARTING FULL DIAGNOSTIC ===")
        
        // 1. Check root
        val hasRoot = Shell.getShell().isRoot
        details["hasRoot"] = hasRoot.toString()
        Log.i(TAG, "Root available: $hasRoot")
        if (!hasRoot) {
            return DiagnosticResult(false, "Root not available", details)
        }
        
        // 2. Check if game is installed
        val gameCheck = Shell.cmd("pm list packages | grep $GAME_PACKAGE").exec()
        val gameInstalled = gameCheck.isSuccess && gameCheck.out.isNotEmpty()
        details["gameInstalled"] = gameInstalled.toString()
        Log.i(TAG, "Game installed: $gameInstalled")
        
        // 3. Find ALL possible graphics.xml locations
        Log.i(TAG, "Searching for graphics.xml in all possible locations...")
        val findResult = Shell.cmd(
            "find /data -name 'graphics.xml' 2>/dev/null",
            "find /storage/emulated/0 -name 'graphics.xml' 2>/dev/null"
        ).exec()
        
        if (findResult.isSuccess && findResult.out.isNotEmpty()) {
            details["foundGraphicsFiles"] = findResult.out.joinToString(", ")
            Log.i(TAG, "Found graphics.xml files at: ${findResult.out.joinToString(", ")}")
            messages.add("Found graphics.xml at: ${findResult.out.joinToString(", ")}")
        } else {
            details["foundGraphicsFiles"] = "none"
            Log.w(TAG, "No graphics.xml files found anywhere!")
            messages.add("⚠️ No graphics.xml found anywhere on device")
        }
        
        // 4. Check all game data directories
        val possiblePaths = listOf(
            "/data/data/$GAME_PACKAGE/files/graphics.xml",
            "/data/user/0/$GAME_PACKAGE/files/graphics.xml",
            "/storage/emulated/0/Android/data/$GAME_PACKAGE/files/graphics.xml",
            "/storage/emulated/0/Android/obb/$GAME_PACKAGE/graphics.xml",
            "/data/media/0/Android/data/$GAME_PACKAGE/files/graphics.xml"
        )
        
        Log.i(TAG, "Checking specific paths...")
        for (path in possiblePaths) {
            val checkCmd = Shell.cmd(
                "if [ -f '$path' ]; then echo 'EXISTS'; else echo 'NOT_FOUND'; fi",
                "ls -la '$path' 2>/dev/null"
            ).exec()
            
            if (checkCmd.out.firstOrNull() == "EXISTS") {
                details["exists_$path"] = "YES"
                val lsOutput = checkCmd.out.drop(1).joinToString("\n")
                details["ls_$path"] = lsOutput
                Log.i(TAG, "✓ Found: $path - $lsOutput")
                messages.add("✓ Found: $path")
            } else {
                details["exists_$path"] = "NO"
                Log.i(TAG, "✗ Not found: $path")
            }
        }
        
        // 5. Test write and verify
        val testPath = "/data/user/0/$GAME_PACKAGE/files/graphics.xml"
        Log.i(TAG, "Testing write to: $testPath")
        
        val testContent = """<?xml version="1.0" encoding="UTF-8"?>
<!-- TEST WRITE AT ${System.currentTimeMillis()} -->
<rage__GraphicsOptions v="83.0">
    <ResolutionX value="9999"/>
</rage__GraphicsOptions>"""
        
        val tempFile = File.createTempFile("diagnostic", ".xml")
        tempFile.writeText(testContent)
        
        val writeCmd = Shell.cmd(
            "mkdir -p /data/user/0/$GAME_PACKAGE/files",
            "cp '${tempFile.absolutePath}' '$testPath'",
            "chmod 644 '$testPath'",
            "sync",  // Force write to disk
            "sleep 1"  // Wait for write to complete
        ).exec()
        
        tempFile.delete()
        
        details["writeExitCode"] = writeCmd.code.toString()
        Log.i(TAG, "Write exit code: ${writeCmd.code}")
        
        // 6. Verify content immediately after write
        val verifyCmd = Shell.cmd(
            "cat '$testPath'",
            "stat -c '%s %Y %a %U:%G' '$testPath'"
        ).exec()
        
        if (verifyCmd.isSuccess && verifyCmd.out.isNotEmpty()) {
            val content = verifyCmd.out.dropLast(1).joinToString("\n")
            val stats = verifyCmd.out.lastOrNull() ?: "unknown"
            
            details["verifyContent"] = content.take(200)
            details["fileStats"] = stats
            
            Log.i(TAG, "Verification - File stats: $stats")
            Log.i(TAG, "Verification - Content preview: ${content.take(100)}")
            
            if (content.contains("9999")) {
                messages.add("✓ Write successful - content verified")
                Log.i(TAG, "✓ TEST WRITE SUCCESSFUL - Content verified!")
            } else {
                messages.add("✗ Write failed - content doesn't match")
                Log.e(TAG, "✗ TEST WRITE FAILED - Content doesn't match!")
            }
        } else {
            messages.add("✗ Cannot read file after write")
            Log.e(TAG, "✗ Cannot read file after write")
        }
        
        // 7. Check if game process is running
        val psResult = Shell.cmd("ps -A | grep $GAME_PACKAGE").exec()
        val gameRunning = psResult.isSuccess && psResult.out.isNotEmpty()
        details["gameRunning"] = gameRunning.toString()
        if (gameRunning) {
            messages.add("⚠️ Game is currently running - may overwrite file")
            Log.w(TAG, "Game process is running: ${psResult.out.joinToString()}")
        }
        
        // 8. Check SELinux
        val seResult = Shell.cmd("getenforce").exec()
        if (seResult.isSuccess) {
            val seMode = seResult.out.firstOrNull() ?: "unknown"
            details["selinux"] = seMode
            Log.i(TAG, "SELinux mode: $seMode")
            if (seMode == "Enforcing") {
                messages.add("⚠️ SELinux is Enforcing - may block access")
            }
        }
        
        // 9. Check directory permissions
        val dirCheck = Shell.cmd("ls -lad /data/user/0/$GAME_PACKAGE/files/").exec()
        if (dirCheck.isSuccess && dirCheck.out.isNotEmpty()) {
            details["dirPermissions"] = dirCheck.out.joinToString()
            Log.i(TAG, "Directory permissions: ${dirCheck.out.joinToString()}")
        }
        
        Log.i(TAG, "=== DIAGNOSTIC COMPLETE ===")
        
        val finalMessage = if (messages.isEmpty()) {
            "Diagnostic completed - check details"
        } else {
            messages.joinToString("\n")
        }
        
        return DiagnosticResult(true, finalMessage, details)
    }
    
    fun getDetailedReport(): String {
        val result = performFullDiagnostic()
        val sb = StringBuilder()
        
        sb.appendLine("=== RDR GRAPHICS EDITOR DIAGNOSTIC REPORT ===")
        sb.appendLine()
        sb.appendLine("Timestamp: ${System.currentTimeMillis()}")
        sb.appendLine("Result: ${if (result.success) "SUCCESS" else "FAILED"}")
        sb.appendLine()
        sb.appendLine("MESSAGE:")
        sb.appendLine(result.message)
        sb.appendLine()
        sb.appendLine("DETAILS:")
        result.details.forEach { (key, value) ->
            sb.appendLine("$key: $value")
        }
        sb.appendLine()
        sb.appendLine("=== END OF REPORT ===")
        
        return sb.toString()
    }
    
    fun exportDiagnosticToFile(): String? {
        return try {
            val report = getDetailedReport()
            val timestamp = System.currentTimeMillis()
            val outputPath = "/sdcard/Download/rdr_diagnostic_$timestamp.txt"
            
            val tempFile = File.createTempFile("diagnostic", ".txt")
            tempFile.writeText(report)
            
            val result = Shell.cmd("cp '${tempFile.absolutePath}' '$outputPath'").exec()
            tempFile.delete()
            
            if (result.isSuccess) {
                Log.i(TAG, "Diagnostic exported to: $outputPath")
                outputPath
            } else {
                Log.e(TAG, "Failed to export diagnostic")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Export exception", e)
            null
        }
    }
}
