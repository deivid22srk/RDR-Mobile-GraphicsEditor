package com.rdrgraphics.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rdrgraphics.editor.ui.screens.DynamicGraphicsScreen
import com.rdrgraphics.editor.ui.screens.LanguageScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (selectedItem) {
                            0 -> "Graphics Settings"
                            1 -> "Language Settings"
                            else -> "RDR Graphics Editor"
                        }
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Tune, contentDescription = "Graphics") },
                    label = { Text("Graphics") },
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                        navController.navigate("graphics") {
                            popUpTo("graphics") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Language, contentDescription = "Language") },
                    label = { Text("Language") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        navController.navigate("language") {
                            popUpTo("language") { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "graphics",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("graphics") {
                DynamicGraphicsScreen()
            }
            composable("language") {
                LanguageScreen()
            }
        }
    }
}
