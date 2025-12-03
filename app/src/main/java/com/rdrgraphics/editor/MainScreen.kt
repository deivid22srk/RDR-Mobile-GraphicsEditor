package com.rdrgraphics.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rdrgraphics.editor.ui.screens.DynamicGraphicsScreen
import com.rdrgraphics.editor.ui.screens.FileExplorerScreen
import com.rdrgraphics.editor.ui.screens.LanguageScreen
import com.rdrgraphics.editor.ui.screens.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedItem by remember { mutableStateOf(0) }
    var selectedXmlPath by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (selectedItem) {
                            0 -> "File Explorer"
                            1 -> if (selectedXmlPath != null) "XML Editor" else "Graphics Settings"
                            2 -> "Language Settings"
                            3 -> "Settings"
                            else -> "RDR Graphics Editor"
                        }
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (selectedItem == 1 && selectedXmlPath != null) {
                        IconButton(
                            onClick = { 
                                selectedXmlPath = null
                                selectedItem = 0
                            }
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close editor")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.FolderOpen, contentDescription = "Files") },
                    label = { Text("Files") },
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                        selectedXmlPath = null
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Edit, contentDescription = "Editor") },
                    label = { Text("Editor") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                    },
                    enabled = selectedXmlPath != null
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Language, contentDescription = "Language") },
                    label = { Text("Language") },
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedItem == 3,
                    onClick = {
                        selectedItem = 3
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> {
                    FileExplorerScreen(
                        onXmlSelected = { path ->
                            selectedXmlPath = path
                            selectedItem = 1
                        }
                    )
                }
                1 -> {
                    if (selectedXmlPath != null) {
                        DynamicGraphicsScreen(xmlFilePath = selectedXmlPath)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Filled.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "No XML Selected",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Go to Files tab and select an XML file to edit",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    LanguageScreen()
                }
                3 -> {
                    SettingsScreen()
                }
            }
        }
    }
}
