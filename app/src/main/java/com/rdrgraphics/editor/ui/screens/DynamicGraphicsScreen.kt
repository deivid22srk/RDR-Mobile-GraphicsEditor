package com.rdrgraphics.editor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rdrgraphics.editor.data.XmlField
import com.rdrgraphics.editor.data.XmlParser
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
    var modifiedFields by remember { mutableStateOf<MutableMap<String, String>>(mutableMapOf()) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val rootAvailable = RootManager.isRootAvailable()
                hasRootAccess = rootAvailable
                
                if (rootAvailable) {
                    val xmlContent = RootManager.readGraphicsConfig()
                    if (xmlContent != null) {
                        val (parsedRootTag, parsedFields) = XmlParser.parseGraphicsXml(xmlContent)
                        rootTag = parsedRootTag
                        fields = parsedFields
                        errorMessage = null
                    } else {
                        errorMessage = "Failed to read graphics.xml from device"
                    }
                } else {
                    errorMessage = "Root access not available"
                }
                
                isLoading = false
            }
        }
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
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                withContext(Dispatchers.IO) {
                                    val rootAvailable = RootManager.isRootAvailable()
                                    hasRootAccess = rootAvailable
                                    
                                    if (rootAvailable) {
                                        val xmlContent = RootManager.readGraphicsConfig()
                                        if (xmlContent != null) {
                                            val (parsedRootTag, parsedFields) = XmlParser.parseGraphicsXml(xmlContent)
                                            rootTag = parsedRootTag
                                            fields = parsedFields
                                            errorMessage = null
                                        } else {
                                            errorMessage = "Failed to read graphics.xml from device"
                                        }
                                    } else {
                                        errorMessage = "Root access not available"
                                    }
                                    
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Graphics Configuration Loaded",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
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
