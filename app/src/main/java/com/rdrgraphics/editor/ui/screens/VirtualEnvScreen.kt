package com.rdrgraphics.editor.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rdrgraphics.editor.blackbox.BlackBoxManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualEnvScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isBlackBoxInitialized by remember { mutableStateOf(false) }
    var isGameInstalled by remember { mutableStateOf(false) }
    var gameInfo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // File picker for APK
    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isLoading = true
                snackbarHostState.showSnackbar("Installing APK in virtual environment...")
                
                val result = withContext(Dispatchers.IO) {
                    BlackBoxManager.installApkFromUri(context, uri)
                }
                
                isLoading = false
                
                if (result.success) {
                    snackbarHostState.showSnackbar("✅ ${result.message}")
                    isGameInstalled = BlackBoxManager.isGameInstalledInVirtualEnv()
                    gameInfo = BlackBoxManager.getGameInfo()
                } else {
                    snackbarHostState.showSnackbar("❌ ${result.message}")
                }
            }
        }
    }
    
    LaunchedEffect(Unit) {
        isLoading = true
        val initialized = withContext(Dispatchers.IO) {
            BlackBoxManager.initialize(context.applicationContext)
        }
        isBlackBoxInitialized = initialized
        
        if (initialized) {
            isGameInstalled = withContext(Dispatchers.IO) {
                BlackBoxManager.isGameInstalledInVirtualEnv()
            }
            gameInfo = withContext(Dispatchers.IO) {
                BlackBoxManager.getGameInfo()
            }
        }
        isLoading = false
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CellTower,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Virtual Environment",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "Run game without root",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBlackBoxInitialized) 
                        MaterialTheme.colorScheme.tertiaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isBlackBoxInitialized) Icons.Filled.CheckCircle else Icons.Filled.Error,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isBlackBoxInitialized) "Virtual Environment Active" else "Virtual Environment Inactive",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    if (isBlackBoxInitialized) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Game Status: ${if (isGameInstalled) "Installed ✅" else "Not Installed ❌"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Install APK Section
            Text("Install Game", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Install RDR Mobile APK in virtual environment",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            apkPickerLauncher.launch(arrayOf("application/vnd.android.package-archive"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isBlackBoxInitialized && !isLoading
                    ) {
                        Icon(Icons.Filled.FileOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select APK from Storage")
                    }
                    
                    if (isGameInstalled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    val success = withContext(Dispatchers.IO) {
                                        BlackBoxManager.uninstallGameFromVirtualEnv()
                                    }
                                    isLoading = false
                                    
                                    if (success) {
                                        snackbarHostState.showSnackbar("Game uninstalled")
                                        isGameInstalled = false
                                        gameInfo = BlackBoxManager.getGameInfo()
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to uninstall")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uninstall Game")
                        }
                    }
                }
            }
            
            if (isGameInstalled) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Launch Game Section
                Text("Launch Game", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Start game in virtual environment",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    val success = withContext(Dispatchers.IO) {
                                        BlackBoxManager.launchGameInVirtualEnv(context)
                                    }
                                    if (success) {
                                        snackbarHostState.showSnackbar("Game launched")
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to launch game")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch RDR Mobile")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Game Info
                Text("Game Information", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            gameInfo,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "About Virtual Environment",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        """
                        • No root required
                        • Game runs in isolated environment
                        • Modify graphics.xml without system root
                        • Data stored in app's private directory
                        • Compatible with most Android versions
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
            
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
