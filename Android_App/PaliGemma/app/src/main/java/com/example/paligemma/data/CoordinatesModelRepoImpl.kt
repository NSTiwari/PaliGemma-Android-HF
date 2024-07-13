package com.example.paligemma.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

class CoordinatesModelRepoImpl(
    val applicationContext: Context
) : CoordinatesModelRepo {

    override suspend fun getCoordinatesModel(requestModel: RequestModel): Response<CoordinatesModel> {
        return withContext(Dispatchers.IO) {
            val file = File(applicationContext.cacheDir, "myImage.jpg")
            file.createNewFile()
            file.outputStream().use {
                applicationContext.contentResolver.openInputStream(requestModel.uri)?.apply {
                    copyTo(it)
                    close()
                }
            }
            CoordinatesModelApi.instance.getCoordinatesModel(
                text = requestModel.text,
                image = MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    file.asRequestBody()
                )
            )
        }
    }

}