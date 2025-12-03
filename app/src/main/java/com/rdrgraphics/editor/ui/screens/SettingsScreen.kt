package com.rdrgraphics.editor.ui.screens

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
import com.rdrgraphics.editor.utils.GofileUploader
import com.rdrgraphics.editor.utils.ProjectCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isCompressing by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var compressionProgress by remember { mutableStateOf(0f) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var downloadUrl by remember { mutableStateOf<String?>(null) }
    
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
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "About",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "RDR Mobile Graphics Editor\nVersion 2.0 - Dynamic Edition",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Export Project", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Upload to Gofile",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Compress and upload the entire project to Gofile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (isCompressing || isUploading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isCompressing) {
                            Text("Compressing...", style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(
                                progress = compressionProgress,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "${(compressionProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        if (isUploading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Uploading to Gofile...", style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(
                                progress = uploadProgress,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "${(uploadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    if (downloadUrl != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Upload Complete!",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    downloadUrl!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isCompressing = true
                                compressionProgress = 0f
                                downloadUrl = null
                                
                                val zipFile = withContext(Dispatchers.IO) {
                                    ProjectCompressor.compressProject(context) { progress ->
                                        compressionProgress = progress
                                    }
                                }
                                
                                isCompressing = false
                                
                                if (zipFile == null) {
                                    snackbarHostState.showSnackbar("Compression failed")
                                    return@launch
                                }
                                
                                isUploading = true
                                uploadProgress = 0f
                                
                                val result = withContext(Dispatchers.IO) {
                                    GofileUploader.uploadFile(zipFile) { progress ->
                                        uploadProgress = progress
                                    }
                                }
                                
                                isUploading = false
                                
                                if (result.success && result.downloadUrl != null) {
                                    downloadUrl = result.downloadUrl
                                    snackbarHostState.showSnackbar("Upload successful!")
                                } else {
                                    snackbarHostState.showSnackbar(
                                        "Upload failed: ${result.error ?: "Unknown error"}"
                                    )
                                }
                                
                                zipFile.delete()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCompressing && !isUploading
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload to Gofile")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Features", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureItem(
                        icon = Icons.Filled.DynamicForm,
                        title = "Dynamic XML Loading",
                        description = "Automatically reads and adapts to graphics.xml"
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    FeatureItem(
                        icon = Icons.Filled.Edit,
                        title = "Partial Line Modification",
                        description = "Only modifies the lines you change"
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    FeatureItem(
                        icon = Icons.Filled.Security,
                        title = "Root Access via libsu",
                        description = "Efficient and secure root operations"
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    FeatureItem(
                        icon = Icons.Filled.CloudUpload,
                        title = "Gofile Integration",
                        description = "Upload and share your project easily"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
