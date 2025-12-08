package com.rdrgraphics.editor

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.rdrgraphics.editor.ui.theme.RDRGraphicsEditorTheme

class MainActivity : ComponentActivity() {
    private val selectedXmlUri = mutableStateOf<Uri?>(null)
    
    private val xmlPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            selectedXmlUri.value = it
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            RDRGraphicsEditorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        selectedXmlUri = selectedXmlUri.value,
                        onSelectFile = {
                            xmlPickerLauncher.launch(arrayOf("text/xml", "application/xml", "*/*"))
                        },
                        onClearSelection = {
                            selectedXmlUri.value = null
                        }
                    )
                }
            }
        }
    }
}
