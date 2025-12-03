package com.rdrgraphics.editor.utils

import android.content.Context
import android.widget.Toast
import com.topjohnwu.superuser.Shell
import java.io.File

object DiagnosticHelper {
    fun runDiagnostics(context: Context): String {
        val report = StringBuilder()
        
        try {
            report.appendLine("=== Root Access Diagnostics ===\n")
            
            val isRoot = RootManager.isRootAvailable()
            report.appendLine("Root Available: $isRoot")
            
            if (!isRoot) {
                report.appendLine("\n❌ Root access not granted!")
                return report.toString()
            }
            
            report.appendLine("\n=== File System Check ===\n")
            
            val paths = listOf(
                "/data/user/0/com.netflix.NGP.Kamo",
                "/data/user/0/com.netflix.NGP.Kamo/files",
                "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
            )
            
            for (path in paths) {
                val exists = RootManager.fileExists(path)
                report.appendLine("$path")
                report.appendLine("  Exists: $exists")
                
                if (exists) {
                    val lsResult = Shell.cmd("ls -la '$path' 2>&1").exec()
                    if (lsResult.isSuccess && lsResult.out.isNotEmpty()) {
                        report.appendLine("  Permissions: ${lsResult.out.joinToString(" ")}")
                    }
                    
                    val contextResult = Shell.cmd("ls -Z '$path' 2>&1").exec()
                    if (contextResult.isSuccess && contextResult.out.isNotEmpty()) {
                        report.appendLine("  SELinux: ${contextResult.out.joinToString(" ")}")
                    }
                }
                report.appendLine()
            }
            
            report.appendLine("=== SELinux Status ===\n")
            val selinuxResult = Shell.cmd("getenforce 2>&1").exec()
            if (selinuxResult.isSuccess) {
                report.appendLine("SELinux Mode: ${selinuxResult.out.joinToString(" ")}")
            }
            
            report.appendLine("\n=== Read Test ===\n")
            val xmlPath = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
            
            report.appendLine("Method 1: Direct cat")
            val catResult = Shell.cmd("cat '$xmlPath' 2>&1 | head -5").exec()
            report.appendLine("  Success: ${catResult.isSuccess}")
            report.appendLine("  Output lines: ${catResult.out.size}")
            if (catResult.err.isNotEmpty()) {
                report.appendLine("  Errors: ${catResult.err.take(3).joinToString("; ")}")
            }
            
            report.appendLine("\nMethod 2: su 0 cat")
            val suResult = Shell.cmd("su 0 cat '$xmlPath' 2>&1 | head -5").exec()
            report.appendLine("  Success: ${suResult.isSuccess}")
            report.appendLine("  Output lines: ${suResult.out.size}")
            
            report.appendLine("\nMethod 3: After chmod")
            Shell.cmd("chmod 666 '$xmlPath' 2>/dev/null").exec()
            val chmodResult = Shell.cmd("cat '$xmlPath' 2>&1 | head -5").exec()
            report.appendLine("  Success: ${chmodResult.isSuccess}")
            report.appendLine("  Output lines: ${chmodResult.out.size}")
            
            report.appendLine("\n=== Shell Info ===\n")
            val shellResult = Shell.cmd("echo \$0 && id").exec()
            if (shellResult.isSuccess) {
                report.appendLine(shellResult.out.joinToString("\n"))
            }
            
        } catch (e: Exception) {
            report.appendLine("\n❌ Error: ${e.message}")
            e.printStackTrace()
        }
        
        return report.toString()
    }
}
