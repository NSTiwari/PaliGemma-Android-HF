package com.example.paligemma

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.paligemma.data.CoordinatesModel
import com.example.paligemma.data.CoordinatesModelRepoImpl
import com.example.paligemma.data.CoordinatesModelViewModel
import com.example.paligemma.data.CoordinatesModelViewModelFactory
import com.example.paligemma.data.UiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    ImageUploadScreen()
                }
            }
        }
    }
}

fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}

@Composable
fun ImageUploadScreen() {
    val context = LocalContext.current
    val viewModel = viewModel<CoordinatesModelViewModel>(
        factory = CoordinatesModelViewModelFactory(
            coordinatesModelRepo = CoordinatesModelRepoImpl(
                applicationContext = context.applicationContext
            )
        )
    )
    val coordinates by viewModel.coordinates

    val cameraImageFile = remember {
        context.createImageFile()
    }
    val cameraImageUri = remember {
        FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            "com.example.paligemma" + ".provider", cameraImageFile
        )
    }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            imageUri = cameraImageUri
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(cameraImageUri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    var textPrompt by rememberSaveable { mutableStateOf("") }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    Column(
        modifier = Modifier
            .padding(all = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (
            coordinates
        ) {
            UiState.Loading -> {
                CircularProgressIndicator()
            }

            is UiState.Error -> {
                (coordinates as UiState.Error).e.let {
                    Log.d("ERROR", "ImageUploadScreen: $it")
                    Text(text = it.message?:"Something went wrong!")
                }
            }

            else -> {}
        }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val permissionCheckResult =
                        ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        )
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(cameraImageUri)
                    } else {
                        // Request a permission
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier
                    .padding(all = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color(0xFFFFFFFF)
                )
            ) {
                Text("Open Camera")
            }
            Button(
                onClick = {
                    pickMedia.launch("image/*")
                },
                modifier = Modifier
                    .padding(all = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color(0xFFFFFFFF)
                )
            ) {
                Text("Upload Image")
            }
        }

        imageUri?.let { uri ->
            ImageWithBoundingBox(
                uri = uri,
                coordinatesModel = (coordinates as? UiState.Success)?.coordinatesModel
            )

            OutlinedTextField(
                value = textPrompt,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1A73E8),
                    unfocusedBorderColor = Color(0xFF1A73E8),
                    focusedLabelColor = Color(0xFF1A73E8),
                    unfocusedLabelColor = Color(0xFF1A73E8)
                ),
                label = { Text("Prompt") },
                onValueChange = { textPrompt = it },
                placeholder = { Text("Enter text prompt") },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .align(Alignment.CenterHorizontally),
            )

            Button(
                onClick = {
                    viewModel.getCoordinatesModel(text = textPrompt, uri = imageUri ?: Uri.EMPTY)
                },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color(0xFFFAFAFA)
                )
            ) {
                Text("Submit")
            }
        }
    }
}


@Composable
fun ImageWithBoundingBox(uri: Uri, coordinatesModel: CoordinatesModel?) {
    val painter = rememberAsyncImagePainter(uri)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .requiredSize(300.dp)
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        coordinatesModel?.result?.forEach {
            Column {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Example: Draw a red rectangle at specific coordinates
                    val rect = Rect(
                        left = it.coordinates[0].toFloat(),
                        top = it.coordinates[1].toFloat(),
                        right = it.coordinates[2].toFloat(),
                        bottom = it.coordinates[3].toFloat()
                    )
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(rect.left, rect.top),
                        size = Size(rect.width, rect.height),
                        style = Stroke(
                            width = 10f
                        )
                    )
                }
                Text(text = it.label)
            }
        }
    }
}
