package com.rdrgraphics.editor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.rdrgraphics.editor.ui.theme.RDRGraphicsEditorTheme
import com.rdrgraphics.editor.utils.PermissionManager
import com.rdrgraphics.editor.utils.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val hasStoragePermission = mutableStateOf(false)
    
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasStoragePermission.value = permissions.values.all { it }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch(Dispatchers.IO) {
            RootManager.isRootAvailable()
        }
        
        checkStoragePermissions()
        
        setContent {
            RDRGraphicsEditorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        hasStoragePermission = hasStoragePermission.value,
                        onRequestPermission = { requestStoragePermissions() }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        checkStoragePermissions()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionManager.MANAGE_STORAGE_REQUEST_CODE) {
            checkStoragePermissions()
        }
    }
    
    private fun checkStoragePermissions() {
        hasStoragePermission.value = PermissionManager.hasStoragePermissions(this)
    }
    
    private fun requestStoragePermissions() {
        PermissionManager.requestStoragePermissions(this)
    }
}
