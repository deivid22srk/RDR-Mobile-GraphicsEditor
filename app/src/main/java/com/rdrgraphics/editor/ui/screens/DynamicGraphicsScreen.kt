package com.rdrgraphics.editor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.rdrgraphics.editor.data.XmlField
import com.rdrgraphics.editor.data.XmlParser
import com.rdrgraphics.editor.utils.DiagnosticHelper
import com.rdrgraphics.editor.utils.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicGraphicsScreen() {
    var fields by remember { mutableStateOf<List<XmlField>>(emptyList()) }
    var rootTag by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var hasRootAccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var errorDetails by remember { mutableStateOf<String?>(null) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var modifiedFields by remember { mutableStateOf<MutableMap<String, String>>(mutableMapOf()) }
    var useDefaultConfig by remember { mutableStateOf(false) }
    var showDiagnostics by remember { mutableStateOf(false) }
    var diagnosticReport by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    fun loadGraphicsConfig() {
        scope.launch {
            isLoading = true
            errorMessage = null
            withContext(Dispatchers.IO) {
                val rootAvailable = RootManager.isRootAvailable()
                hasRootAccess = rootAvailable
                
                if (rootAvailable) {
                    val xmlContent = if (useDefaultConfig) {
                        RootManager.getDefaultGraphicsXml()
                    } else {
                        RootManager.readGraphicsConfig()
                    }
                    
                    if (xmlContent != null) {
                        val (parsedRootTag, parsedFields) = XmlParser.parseGraphicsXml(xmlContent)
                        rootTag = parsedRootTag
                        fields = parsedFields
                        errorMessage = null
                        errorDetails = null
                    } else {
                        val path = "/data/user/0/com.netflix.NGP.Kamo/files/graphics.xml"
                        val fileExists = RootManager.fileExists(path)
                        val dirExists = RootManager.fileExists("/data/user/0/com.netflix.NGP.Kamo/files")
                        val appInstalled = RootManager.fileExists("/data/user/0/com.netflix.NGP.Kamo")
                        
                        errorMessage = if (!appInstalled) {
                            "Game not installed or not found"
                        } else if (!dirExists) {
                            "Game files directory not found"
                        } else if (!fileExists) {
                            "graphics.xml not found on device"
                        } else {
                            "Failed to read graphics.xml"
                        }
                        
                        errorDetails = "Path: $path\nGame dir exists: $appInstalled\nFiles dir exists: $dirExists\nXML exists: $fileExists"
                        showCreateFileDialog = appInstalled && !fileExists
                    }
                } else {
                    errorMessage = "Root access not available"
                    errorDetails = "This app requires root access to read/write game files. Please grant root access when prompted."
                }
                
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadGraphicsConfig()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (hasRootAccess && modifiedFields.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Applying changes...")
                            val success = withContext(Dispatchers.IO) {
                                RootManager.updateMultipleGraphicsFields(modifiedFields)
                            }
                            
                            if (success) {
                                snackbarHostState.showSnackbar(
                                    "Successfully updated ${modifiedFields.size} field(s)",
                                    duration = SnackbarDuration.Long
                                )
                                modifiedFields.clear()
                            } else {
                                snackbarHostState.showSnackbar(
                                    "Failed to apply changes",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    },
                    icon = { Icon(Icons.Filled.Check, "Apply") },
                    text = { Text("Apply ${modifiedFields.size} Change(s)") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Reading graphics configuration...")
                }
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        errorMessage!!,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (errorDetails != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                errorDetails!!,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { loadGraphicsConfig() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    diagnosticReport = withContext(Dispatchers.IO) {
                                        DiagnosticHelper.runDiagnostics(context)
                                    }
                                    isLoading = false
                                    showDiagnostics = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Diagnostics")
                        }
                        
                        if (showCreateFileDialog) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        val success = withContext(Dispatchers.IO) {
                                            val defaultXml = RootManager.getDefaultGraphicsXml()
                                            RootManager.writeGraphicsConfig(defaultXml)
                                        }
                                        
                                        if (success) {
                                            snackbarHostState.showSnackbar("Default file created successfully")
                                            useDefaultConfig = false
                                            loadGraphicsConfig()
                                        } else {
                                            isLoading = false
                                            snackbarHostState.showSnackbar("Failed to create file")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create File")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                useDefaultConfig = true
                                loadGraphicsConfig()
                            }
                        ) {
                            Text("Use Default Config")
                        }
                    }
                    }
                }
            }
        } else {
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
                        containerColor = if (useDefaultConfig) 
                            MaterialTheme.colorScheme.tertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (useDefaultConfig) "Using Default Configuration" else "Graphics Configuration Loaded",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (useDefaultConfig) 
                                "Loaded ${fields.size} settings from default template. Changes will create the file on device."
                            else
                                "Loaded ${fields.size} settings from device",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (modifiedFields.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${modifiedFields.size} pending change(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                fields.forEach { field ->
                    val isModified = modifiedFields.containsKey(field.name)
                    
                    when (field.type) {
                        XmlField.FieldType.BOOLEAN -> {
                            BooleanFieldEditor(
                                field = field,
                                isModified = isModified,
                                onValueChange = { newValue ->
                                    modifiedFields[field.name] = newValue
                                }
                            )
                        }
                        XmlField.FieldType.INTEGER -> {
                            IntegerFieldEditor(
                                field = field,
                                isModified = isModified,
                                currentValue = modifiedFields[field.name] ?: field.value,
                                onValueChange = { newValue ->
                                    modifiedFields[field.name] = newValue
                                }
                            )
                        }
                        XmlField.FieldType.FLOAT -> {
                            FloatFieldEditor(
                                field = field,
                                isModified = isModified,
                                currentValue = modifiedFields[field.name] ?: field.value,
                                onValueChange = { newValue ->
                                    modifiedFields[field.name] = newValue
                                }
                            )
                        }
                        XmlField.FieldType.STRING -> {
                            StringFieldEditor(
                                field = field,
                                isModified = isModified,
                                currentValue = modifiedFields[field.name] ?: field.value,
                                onValueChange = { newValue ->
                                    modifiedFields[field.name] = newValue
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    if (showDiagnostics) {
        AlertDialog(
            onDismissRequest = { showDiagnostics = false },
            title = { Text("Diagnostic Report") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        diagnosticReport,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Diagnostics", diagnosticReport)
                        clipboard.setPrimaryClip(clip)
                        scope.launch {
                            snackbarHostState.showSnackbar("Copied to clipboard")
                        }
                    }
                ) {
                    Text("Copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiagnostics = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun BooleanFieldEditor(
    field: XmlField,
    isModified: Boolean,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isModified) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatFieldName(field.name),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Boolean",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = field.value.equals("true", ignoreCase = true),
                onCheckedChange = { checked ->
                    onValueChange(if (checked) "true" else "false")
                }
            )
        }
    }
}

@Composable
fun IntegerFieldEditor(
    field: XmlField,
    isModified: Boolean,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    var textValue by remember(currentValue) { mutableStateOf(currentValue) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isModified) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                formatFieldName(field.name),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Integer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    textValue = newValue
                    if (newValue.toIntOrNull() != null || newValue.isEmpty()) {
                        onValueChange(newValue)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun FloatFieldEditor(
    field: XmlField,
    isModified: Boolean,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    var textValue by remember(currentValue) { mutableStateOf(currentValue) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isModified) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                formatFieldName(field.name),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Float",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    textValue = newValue
                    if (newValue.toFloatOrNull() != null || newValue.isEmpty()) {
                        onValueChange(newValue)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun StringFieldEditor(
    field: XmlField,
    isModified: Boolean,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    var textValue by remember(currentValue) { mutableStateOf(currentValue) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isModified) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                formatFieldName(field.name),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "String",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    textValue = newValue
                    onValueChange(newValue)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

fun formatFieldName(fieldName: String): String {
    return fieldName
        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1 $2")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
