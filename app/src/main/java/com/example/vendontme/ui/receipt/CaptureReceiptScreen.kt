package com.example.vendontme.ui.receipt

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vendontme.R
import com.example.vendontme.core.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CaptureReceiptScreen(
    groupId: String,
    navController: NavController
) {
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Camera permission
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            capturedImageUri = tempImageUri
            // Save to gallery
            saveImageToGallery(context, tempImageUri!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capture Receipt") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, "Cancel")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (capturedImageUri == null) {
                // Show camera button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.photo_cam),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (cameraPermission.status.isGranted) {
                                // Launch camera
                                tempImageUri = createImageUri(context)
                                cameraLauncher.launch(tempImageUri!!)
                            } else {
                                cameraPermission.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.photo_cam),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Take Photo")
                    }
                }
            } else {
                // Show preview with approve/retake buttons
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Image preview
                    Image(
                        painter = rememberAsyncImagePainter(capturedImageUri),
                        contentDescription = "Receipt photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentScale = ContentScale.Fit
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Retake button
                        OutlinedButton(
                            onClick = {
                                capturedImageUri = null
                                tempImageUri = createImageUri(context)
                                cameraLauncher.launch(tempImageUri!!)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.photo_cam),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Retake")
                        }

                        Spacer(Modifier.width(16.dp))

                        // Approve button
                        Button(
                            onClick = {
                                // Navigate to add items screen
                                navController.navigate(
                                    Screen.AddReceiptItems.pass(
                                        groupId = groupId,
                                        imageUri = Uri.encode(capturedImageUri.toString())
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.photo_cam),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Approve")
                        }
                    }
                }
            }
        }
    }
}

// Helper function to create image URI
private fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "RECEIPT_$timeStamp.jpg"

    // FIXED: Use cache directory instead
    val image = File(context.cacheDir, imageFileName)

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        image
    )
}

// Helper function to save to gallery
private fun saveImageToGallery(context: Context, uri: Uri) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Receipt_$timeStamp.jpg"

        // Save to app's Pictures directory
        val picturesDir = File(context.getExternalFilesDir(null), "Pictures")
        if (!picturesDir.exists()) {
            picturesDir.mkdirs()
        }

        val outputFile = File(picturesDir, fileName)
        inputStream?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}