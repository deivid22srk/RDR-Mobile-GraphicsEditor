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
import androidx.compose.ui.unit.dp
import com.rdrgraphics.editor.data.LanguageConfig
import com.rdrgraphics.editor.utils.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen() {
    var selectedLanguage by remember { mutableStateOf("en-US") }
    var showDropdown by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val currentLang = RootManager.getCurrentLanguage()
            if (currentLang != null) {
                selectedLanguage = currentLang
            }
            isLoading = false
        }
    }

    val languages = listOf(
        "en-US" to "English (US)",
        "pt-BR" to "Português (Brasil)",
        "es-ES" to "Español (España)",
        "es-MX" to "Español (México)",
        "fr-FR" to "Français",
        "de-DE" to "Deutsch",
        "it-IT" to "Italiano",
        "ja-JP" to "日本語",
        "ko-KR" to "한국어",
        "zh-CN" to "中文 (简体)",
        "zh-TW" to "中文 (繁體)",
        "ru-RU" to "Русский",
        "pl-PL" to "Polski",
        "tr-TR" to "Türkçe",
        "ar-SA" to "العربية"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        val isRoot = withContext(Dispatchers.IO) {
                            RootManager.isRootAvailable()
                        }
                        if (isRoot) {
                            val success = withContext(Dispatchers.IO) {
                                RootManager.updateLanguageOnly(selectedLanguage)
                            }
                            val message = if (success) {
                                "Language changed to $selectedLanguage successfully!"
                            } else {
                                "Error: Could not update language. Check if game is installed."
                            }
                            snackbarHostState.showSnackbar(message)
                        } else {
                            snackbarHostState.showSnackbar("Root access required")
                        }
                    }
                },
                icon = { Icon(Icons.Filled.Check, "Apply") },
                text = { Text("Apply Changes") },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
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
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Language Configuration",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Only the LANGUAGE line will be modified. All other settings in netflix.dat will be preserved.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Loading current language...")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            ExposedDropdownMenuBox(
                expanded = showDropdown,
                onExpandedChange = { showDropdown = !showDropdown }
            ) {
                OutlinedTextField(
                    value = languages.find { it.first == selectedLanguage }?.second ?: selectedLanguage,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Language") },
                    leadingIcon = { Icon(Icons.Filled.Language, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    languages.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedLanguage = code
                                showDropdown = false
                            },
                            leadingIcon = {
                                if (code == selectedLanguage) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Selected Language",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Code: $selectedLanguage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "Name: ${languages.find { it.first == selectedLanguage }?.second ?: selectedLanguage}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Important",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Restart the game after applying changes for them to take effect.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
