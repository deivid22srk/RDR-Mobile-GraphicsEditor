package com.rdrgraphics.editor.ui.screens

import android.net.Uri
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
import com.rdrgraphics.editor.data.XmlField
import com.rdrgraphics.editor.data.XmlParser
import com.rdrgraphics.editor.utils.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicGraphicsScreen(
    xmlFileUri: Uri? = null
) {
    val context = LocalContext.current
    var parsedXml by remember { mutableStateOf<XmlParser.ParsedXml?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentFileUri by remember { mutableStateOf(xmlFileUri) }
    var fileName by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(currentFileUri) {
        if (currentFileUri == null) {
            error = "No file selected"
            isLoading = false
            return@LaunchedEffect
        }
        
        isLoading = true
        error = null
        
        fileName = withContext(Dispatchers.IO) {
            FileManager.getFileName(context, currentFileUri!!)
        }
        
        val parsed = withContext(Dispatchers.IO) {
            FileManager.parseXmlFile(context, currentFileUri!!)
        }
        
        if (parsed == null) {
            error = "Could not read or parse XML file"
            isLoading = false
            return@LaunchedEffect
        }
        
        parsedXml = parsed
        isLoading = false
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (parsedXml != null && currentFileUri != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        scope.launch {
                            parsedXml?.let { xml ->
                                snackbarHostState.showSnackbar("Applying changes...")
                                val success = withContext(Dispatchers.IO) {
                                    FileManager.writePartialXmlFile(context, currentFileUri!!, xml)
                                }
                                
                                snackbarHostState.showSnackbar(
                                    message = if (success) {
                                        "Settings applied successfully!"
                                    } else {
                                        "Failed to apply settings"
                                    },
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    },
                    icon = { Icon(Icons.Filled.Check, "Apply") },
                    text = { Text("Apply Changes") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reading XML file...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            parsedXml != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fileName ?: "Unknown file",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    DynamicFieldsList(
                        parsedXml = parsedXml!!,
                        onFieldChanged = { field ->
                            field.isModified = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicFieldsList(
    parsedXml: XmlParser.ParsedXml,
    onFieldChanged: (XmlField) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
                    "Dynamic Editor",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Loaded ${parsedXml.fields.size} settings from XML file",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val groupedFields = parsedXml.fields.groupBy { field ->
            when {
                field.name.contains("Resolution", ignoreCase = true) ||
                field.name.contains("Fullscreen", ignoreCase = true) ||
                field.name.contains("Monitor", ignoreCase = true) ||
                field.name.contains("Refresh", ignoreCase = true) ||
                field.name.contains("Frame", ignoreCase = true) ||
                field.name.contains("Vsync", ignoreCase = true) -> "Display"
                
                field.name.contains("Shadow", ignoreCase = true) ||
                field.name.contains("AntiAliasing", ignoreCase = true) ||
                field.name.contains("Filtering", ignoreCase = true) -> "Graphics Quality"
                
                field.name.contains("Blur", ignoreCase = true) -> "Motion Blur"
                
                field.name.contains("FSR", ignoreCase = true) ||
                field.name.contains("DLSS", ignoreCase = true) ||
                field.name.contains("Percentage", ignoreCase = true) -> "Upscaling"
                
                field.name.contains("HDR", ignoreCase = true) ||
                field.name.contains("Brightness", ignoreCase = true) ||
                field.name.contains("White", ignoreCase = true) -> "HDR"
                
                field.name.contains("Dynamic", ignoreCase = true) ||
                field.name.contains("Triple", ignoreCase = true) ||
                field.name.contains("World", ignoreCase = true) ||
                field.name.contains("Terrain", ignoreCase = true) ||
                field.name.contains("Tree", ignoreCase = true) ||
                field.name.contains("Grass", ignoreCase = true) ||
                field.name.contains("Streaming", ignoreCase = true) -> "Advanced"
                
                else -> "Other"
            }
        }
        
        groupedFields.forEach { (category, fields) ->
            Text(
                category,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            fields.forEach { field ->
                when (field) {
                    is XmlField.BooleanField -> {
                        DynamicBooleanField(field, onFieldChanged)
                    }
                    is XmlField.IntField -> {
                        DynamicIntField(field, onFieldChanged)
                    }
                    is XmlField.FloatField -> {
                        DynamicFloatField(field, onFieldChanged)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DynamicBooleanField(field: XmlField.BooleanField, onFieldChanged: (XmlField) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                formatFieldName(field.name),
                style = MaterialTheme.typography.bodyLarge
            )
            if (field.isModified) {
                Text(
                    "Modified",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Switch(
            checked = field.value,
            onCheckedChange = { 
                field.value = it
                onFieldChanged(field)
            }
        )
    }
}

@Composable
fun DynamicIntField(field: XmlField.IntField, onFieldChanged: (XmlField) -> Unit) {
    val minValue = field.min.coerceAtMost(field.value)
    val maxValue = field.max.coerceAtLeast(field.value)
    val validMin = minValue.coerceAtMost(maxValue)
    val validMax = maxValue.coerceAtLeast(validMin)
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatFieldName(field.name),
                    style = MaterialTheme.typography.bodyLarge
                )
                if (field.isModified) {
                    Text(
                        "Modified",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                field.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        if (validMin < validMax) {
            Slider(
                value = field.value.toFloat().coerceIn(validMin.toFloat(), validMax.toFloat()),
                onValueChange = { 
                    field.value = it.toInt().coerceIn(validMin, validMax)
                    onFieldChanged(field)
                },
                valueRange = validMin.toFloat()..validMax.toFloat(),
                steps = ((validMax - validMin).coerceAtMost(100) - 1).coerceAtLeast(0)
            )
        } else {
            Text(
                "Value: ${field.value} (fixed)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DynamicFloatField(field: XmlField.FloatField, onFieldChanged: (XmlField) -> Unit) {
    val minValue = field.min.coerceAtMost(field.value)
    val maxValue = field.max.coerceAtLeast(field.value)
    val validMin = minValue.coerceAtMost(maxValue)
    val validMax = maxValue.coerceAtLeast(validMin)
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatFieldName(field.name),
                    style = MaterialTheme.typography.bodyLarge
                )
                if (field.isModified) {
                    Text(
                        "Modified",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                String.format("%.2f", field.value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        if (validMin < validMax) {
            Slider(
                value = field.value.coerceIn(validMin, validMax),
                onValueChange = { 
                    field.value = it.coerceIn(validMin, validMax)
                    onFieldChanged(field)
                },
                valueRange = validMin..validMax,
                steps = ((validMax - validMin) * 10).toInt().coerceIn(0, 100)
            )
        } else {
            Text(
                "Value: ${String.format("%.2f", field.value)} (fixed)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatFieldName(name: String): String {
    return name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1 $2")
        .replace("_", " ")
}
