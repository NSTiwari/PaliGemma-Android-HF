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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.paligemma.data.CoordinatesModel
import com.example.paligemma.data.CoordinatesModelRepoImpl
import com.example.paligemma.data.CoordinatesModelViewModel
import com.example.paligemma.data.CoordinatesModelViewModelFactory
import com.example.paligemma.data.RequestModel
import com.example.paligemma.data.Result
import com.example.paligemma.data.UiState
import kotlinx.coroutines.launch
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
                    color = Color.Black
                ) {
                    ImageUploadScreen()
                }
            }
        }
    }
}

fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = System.currentTimeMillis().toString()
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

    var imageHeight by remember { mutableIntStateOf(0) }
    var imageWidth by remember { mutableIntStateOf(0) }

    val viewModel = viewModel<CoordinatesModelViewModel>(
        factory = CoordinatesModelViewModelFactory(
            coordinatesModelRepo = CoordinatesModelRepoImpl(
                applicationContext = context.applicationContext
            )
        )
    )
    val coordinates by viewModel.coordinates

    var shouldRefreshImage by remember {
        mutableStateOf(false)
    }
    val cameraImageFile = remember(shouldRefreshImage) {
        context.createImageFile()
    }
    val cameraImageUri = remember(cameraImageFile) {
        Log.d("TAG", "ImageUploadScreen: ImageUri updated with ${cameraImageFile.name}")
        FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            context.packageName + ".provider", cameraImageFile
        )
    }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                imageUri = cameraImageUri
                viewModel.resetData()
            }
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraImageUri?.let { it1 -> cameraLauncher.launch(it1) }
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    var textPrompt by rememberSaveable { mutableStateOf("") }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.resetData()
            imageUri = it
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = coordinates) {
        if(coordinates is UiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((coordinates as UiState.Error).e)
            }
        }
    }
    Scaffold(
        containerColor = Color.Black,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            }
        }
    ) { it->
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            imageUri?.let {
                ImageWithBoundingBox(
                    uri = it,
                    results = (coordinates as? UiState.Success)?.result
                ) { h, w ->
                    imageHeight = h
                    imageWidth = w
                }
            }

            if (coordinates is UiState.Loading) {
                CircularProgressIndicator(color = Color(0xFF1A73E8))
            } else {

                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            shouldRefreshImage = !shouldRefreshImage
                            val permissionCheckResult =
                                ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                )
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(cameraImageUri!!)
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

                OutlinedTextField(
                    value = textPrompt,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A73E8),
                        unfocusedBorderColor = Color(0xFF1A73E8),
                        focusedLabelColor = Color(0xFF1A73E8),
                        unfocusedLabelColor = Color(0xFF1A73E8),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedPlaceholderColor = Color.White,
                        unfocusedPlaceholderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
                        viewModel.getCoordinatesModel(
                            requestModel = RequestModel(
                                text = textPrompt,
                                uri = imageUri ?: Uri.EMPTY,
                                height = imageHeight.toString(),
                                width = imageWidth.toString()
                            )
                        )
                        textPrompt = ""
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

}


@Composable
fun ImageWithBoundingBox(
    uri: Uri,
    results : List<Result>?,
    onSizeChange: (Int, Int) -> Unit
) {
    //initial height set at 0.dp
    var leftDistance by remember { mutableFloatStateOf(0.0f) }
    val textMeasurer = rememberTextMeasurer()

    Box {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .build(),
                modifier = Modifier
                    .heightIn(max = 550.dp)
                    .onGloballyPositioned {
                        leftDistance = it.positionInRoot().x
                        onSizeChange(it.size.height, it.size.width)
                    },
                contentDescription = null
            )
        }
        val map = remember {
            HashMap<String, Color>()
        }
        results?.forEach { result ->
            val (y1, x1, y2, x2) = result.coordinates
            LaunchedEffect(key1 = Unit) {
                map.putIfAbsent(result.label.removeTicks(), getRandomColor())
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = map.getOrDefault(result.label.removeTicks(), Color.Red),
                    style = Stroke(width = 5f),
                    topLeft = Offset(x1.toFloat() + leftDistance, y1.toFloat()),
                    size = Size(
                        width = (x2 - x1).toFloat(),
                        height = (y2 - y1).toFloat()
                    )
                )
                drawText(
                    textMeasurer = textMeasurer,
                    topLeft = Offset(x1.toFloat() + leftDistance, y1.toFloat() - 35),
                    text = result.label.removeTicks()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() },
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color.White,
                        background = map.getOrDefault(result.label.removeTicks(), Color.Red)
                    ),
                    size = Size(
                        width = (x2 - x1).toFloat(),
                        height = (y2 - y1).toFloat()
                    )
                )
            }
        }
    }
}

fun String.removeTicks(): String {
    return this.replace("'", "").trim()
}

fun getRandomColor(): Color {
    val r = (1..254).random()
    val g = (1..254).random()
    val b = (1..254).random()
    return Color(
        red = r,
        green = g,
        blue = b
    )
}
