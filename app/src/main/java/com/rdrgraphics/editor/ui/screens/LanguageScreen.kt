package com.rdrgraphics.editor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var config by remember { mutableStateOf(LanguageConfig()) }
    var profileId by remember { mutableStateOf(config.profileId) }
    var selectedLanguage by remember { mutableStateOf(config.language) }
    var showDropdown by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                        val updatedConfig = config.copy(
                            profileId = profileId,
                            language = selectedLanguage
                        )
                        val isRoot = withContext(Dispatchers.IO) {
                            RootManager.isRootAvailable()
                        }
                        if (isRoot) {
                            val success = withContext(Dispatchers.IO) {
                                RootManager.writeLanguageConfig(updatedConfig.toProperties())
                            }
                            val message = if (success) {
                                "Language settings applied successfully!"
                            } else {
                                "Error applying settings"
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
                    Text(
                        "Configure game language and profile settings",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = profileId,
                onValueChange = { profileId = it },
                label = { Text("Profile ID") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        "Current Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Profile ID: $profileId",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "Language: ${languages.find { it.first == selectedLanguage }?.second ?: selectedLanguage}",
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
