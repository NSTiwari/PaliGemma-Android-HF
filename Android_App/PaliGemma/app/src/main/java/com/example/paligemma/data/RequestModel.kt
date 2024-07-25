package com.example.paligemma.data

import android.net.Uri
import java.io.File

data class RequestModel(
    val text: String,
    val uri: Uri,
    val file: File?
)
