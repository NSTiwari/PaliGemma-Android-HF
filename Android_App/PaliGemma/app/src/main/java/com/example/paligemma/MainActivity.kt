package com.example.paligemma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyRow
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import coil.compose.AsyncImage
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import okhttp3.*


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

@Composable
fun ImageUploadScreen() {


    var drawRectangle by rememberSaveable { mutableStateOf(false) }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var textPrompt by rememberSaveable { mutableStateOf("") }
    var requestBodyText by rememberSaveable { mutableStateOf("") }
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(all = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Button(
            onClick = {
                pickMedia.launch("image/*")
            },
            modifier = Modifier
                .padding(all = 4.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A73E8),
                contentColor = Color(0xFFFFFFFF))
        ) {

            Text("Upload Image")
        }

        /*imageUri?.let { uri ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .requiredSize(300.dp)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )

            }*/

        imageUri?.let { uri ->
            ImageWithBoundingBox(
                uri = uri,
                drawRectangle = drawRectangle,
                onDrawButtonClick = { drawRectangle = !drawRectangle }
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

            Text(
                text = requestBodyText,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = {
                    drawRectangle = !drawRectangle
                    // TODO
                },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color(0xFFFAFAFA)
                )
            ) {
                Text("Draw")
            }
        }
    }
}


@Composable
fun ImageWithBoundingBox(uri: Uri, drawRectangle: Boolean, onDrawButtonClick: () -> Unit) {
    val painter = rememberAsyncImagePainter(uri)

    Column(
        modifier = Modifier
            .padding(8.dp)
            .requiredSize(300.dp)
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        if (drawRectangle) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Example: Draw a red rectangle at specific coordinates
                val rect = Rect(left = 50f, top = 50f, right = 250f, bottom = 250f)
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    style = Stroke(
                        width = 10f)
                )
            }
        }
    }
}
