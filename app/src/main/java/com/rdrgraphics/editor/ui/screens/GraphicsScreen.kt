package com.rdrgraphics.editor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rdrgraphics.editor.data.GraphicsConfig
import com.rdrgraphics.editor.utils.RootManager
import com.rdrgraphics.editor.utils.DiagnosticManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphicsScreen() {
    var config by remember { mutableStateOf(GraphicsConfig()) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var diagnosticReport by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        val existingConfig = withContext(Dispatchers.IO) {
            RootManager.readGraphicsConfig()
        }
        if (existingConfig != null) {
            config = existingConfig
            snackbarHostState.showSnackbar("Loaded existing settings from graphics.xml")
        } else {
            snackbarHostState.showSnackbar("No existing graphics.xml found, using defaults")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Checking root access...")
                        val isRoot = withContext(Dispatchers.IO) {
                            RootManager.isRootAvailable()
                        }
                        if (isRoot) {
                            snackbarHostState.showSnackbar("Saving graphics configuration...")
                            val success = withContext(Dispatchers.IO) {
                                RootManager.writeGraphicsConfig(config)
                            }
                            snackbarMessage = if (success) {
                                "Graphics settings applied successfully!"
                            } else {
                                "Error: Could not write graphics.xml. Check if game is installed."
                            }
                        } else {
                            snackbarMessage = "Error: Root access denied or not available"
                        }
                        snackbarHostState.showSnackbar(
                            message = snackbarMessage,
                            duration = SnackbarDuration.Long
                        )
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
                        "Root Access Required",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "This app requires root access to modify game files",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Running diagnostic...")
                        diagnosticReport = withContext(Dispatchers.IO) {
                            DiagnosticManager.getDetailedReport()
                        }
                        showDiagnosticDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Filled.BugReport, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🔍 Run Diagnostic Test")
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Display Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            SliderSetting(
                label = "Resolution Width",
                value = config.resolutionX.toFloat(),
                range = 640f..3840f,
                steps = 31,
                onValueChange = { config = config.copy(resolutionX = it.toInt()) }
            )

            SliderSetting(
                label = "Resolution Height",
                value = config.resolutionY.toFloat(),
                range = 360f..2160f,
                steps = 17,
                onValueChange = { config = config.copy(resolutionY = it.toInt()) }
            )

            SwitchSetting(
                label = "Fullscreen",
                checked = config.fullscreen,
                onCheckedChange = { config = config.copy(fullscreen = it) }
            )

            SwitchSetting(
                label = "VSync",
                checked = config.vsync == 1,
                onCheckedChange = { config = config.copy(vsync = if (it) 1 else 0) }
            )

            SliderSetting(
                label = "Frame Rate Limit",
                value = config.frameRateLimit.toFloat(),
                range = 0f..240f,
                steps = 23,
                onValueChange = { config = config.copy(frameRateLimit = it.toInt()) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Graphics Quality", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            SliderSetting(
                label = "Shadow Quality (0-4)",
                value = config.shadowQuality.toFloat(),
                range = 0f..4f,
                steps = 4,
                onValueChange = { config = config.copy(shadowQuality = it.toInt()) }
            )

            SliderSetting(
                label = "Shadow Softness (0-4)",
                value = config.shadowSoftness.toFloat(),
                range = 0f..4f,
                steps = 4,
                onValueChange = { config = config.copy(shadowSoftness = it.toInt()) }
            )

            SliderSetting(
                label = "Anti-Aliasing (0-4)",
                value = config.aaAntiAliasing.toFloat(),
                range = 0f..4f,
                steps = 4,
                onValueChange = { config = config.copy(aaAntiAliasing = it.toInt()) }
            )

            SliderSetting(
                label = "Anisotropic Filtering (0-16)",
                value = config.minAnisotropicFiltering.toFloat(),
                range = 0f..16f,
                steps = 15,
                onValueChange = { config = config.copy(minAnisotropicFiltering = it.toInt()) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Motion Blur", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            SliderSetting(
                label = "Motion Blur Style (0-2)",
                value = config.motionBlurStyle.toFloat(),
                range = 0f..2f,
                steps = 2,
                onValueChange = { config = config.copy(motionBlurStyle = it.toInt()) }
            )

            SliderSetting(
                label = "Motion Blur Strength",
                value = config.motionBlurStrength,
                range = 0f..1f,
                steps = 19,
                onValueChange = { config = config.copy(motionBlurStrength = it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Advanced Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            SwitchSetting(
                label = "Dynamic Resolution",
                checked = config.dynamicResolution,
                onCheckedChange = { config = config.copy(dynamicResolution = it) }
            )

            SwitchSetting(
                label = "Triple Buffering",
                checked = config.tripleBuffer,
                onCheckedChange = { config = config.copy(tripleBuffer = it) }
            )

            SliderSetting(
                label = "World Streaming Radius",
                value = config.worldStreamingRadius,
                range = 50f..200f,
                steps = 29,
                onValueChange = { config = config.copy(worldStreamingRadius = it) }
            )

            SliderSetting(
                label = "Terrain Streaming Factor",
                value = config.terrainStreamingFactor,
                range = 0.5f..2f,
                steps = 14,
                onValueChange = { config = config.copy(terrainStreamingFactor = it) }
            )

            SliderSetting(
                label = "Tree Level of Detail",
                value = config.treeLevelOfDetail,
                range = 0.5f..2f,
                steps = 14,
                onValueChange = { config = config.copy(treeLevelOfDetail = it) }
            )

            SliderSetting(
                label = "Grass Streaming Distance",
                value = config.grassStreamingDistance,
                range = 0f..100f,
                steps = 19,
                onValueChange = { config = config.copy(grassStreamingDistance = it) }
            )

            SwitchSetting(
                label = "HDR",
                checked = config.hdr,
                onCheckedChange = { config = config.copy(hdr = it) }
            )

            if (config.hdr) {
                SliderSetting(
                    label = "Peak Brightness",
                    value = config.peakBrightness,
                    range = 100f..1000f,
                    steps = 17,
                    onValueChange = { config = config.copy(peakBrightness = it) }
                )

                SliderSetting(
                    label = "Paper White",
                    value = config.paperWhite,
                    range = 80f..300f,
                    steps = 21,
                    onValueChange = { config = config.copy(paperWhite = it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Upscaling", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            SliderSetting(
                label = "FSR3 Quality (0=Off, 1-4)",
                value = config.fsr3UpscalingQuality.toFloat(),
                range = 0f..4f,
                steps = 4,
                onValueChange = { config = config.copy(fsr3UpscalingQuality = it.toInt()) }
            )

            SliderSetting(
                label = "FSR3 Sharpness",
                value = config.fsr3AdditionalSharpness,
                range = 0f..1f,
                steps = 19,
                onValueChange = { config = config.copy(fsr3AdditionalSharpness = it) }
            )

            SliderSetting(
                label = "DLSS Quality (0=Off, 1-4)",
                value = config.dlssUpscalingQuality.toFloat(),
                range = 0f..4f,
                steps = 4,
                onValueChange = { config = config.copy(dlssUpscalingQuality = it.toInt()) }
            )

            SliderSetting(
                label = "Screen Percentage",
                value = config.screenPercentage,
                range = 0.5f..2f,
                steps = 29,
                onValueChange = { config = config.copy(screenPercentage = it) }
            )

            SliderSetting(
                label = "Mobile Preset (0-4)",
                value = config.mobilePreset.toFloat(),
                range = 0f..4f,
                steps = 4,
                onValueChange = { config = config.copy(mobilePreset = it.toInt()) }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
        
        if (showDiagnosticDialog) {
            AlertDialog(
                onDismissRequest = { showDiagnosticDialog = false },
                title = { Text("\ud83d\udd0d Diagnostic Report") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            diagnosticReport,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val exported = withContext(Dispatchers.IO) {
                                    DiagnosticManager.exportDiagnosticToFile()
                                }
                                if (exported != null) {
                                    snackbarHostState.showSnackbar("Exported to: $exported")
                                } else {
                                    snackbarHostState.showSnackbar("Failed to export")
                                }
                            }
                        }
                    ) {
                        Text("Export to File")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiagnosticDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun SliderSetting(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                String.format("%.2f", value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps
        )
    }
}

@Composable
fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
