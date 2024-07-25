package com.example.paligemma.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response


class CoordinatesModelRepoImpl(
    val applicationContext: Context
) : CoordinatesModelRepo {

    override suspend fun getCoordinatesModel(requestModel: RequestModel): Response<CoordinatesModel> {
        return withContext(Dispatchers.IO) {
//            val file = File(applicationContext.externalCacheDir,"myImage.jpg").apply {
//                createNewFile()
//                outputStream().use { op->
//                    applicationContext.contentResolver.openInputStream(requestModel.uri).use { ip->
//                        ip?.copyTo(op)
//                    }
//                }
//            }
            val file = requestModel.file!!
            Log.d("TAG", "getCoordinatesModel: File = $file")
            CoordinatesModelApi.instance.getCoordinatesModel(
                text = requestModel.text.toRequestBody(MultipartBody.FORM),
                image = MultipartBody.Part.createFormData(
                    name = "image",
                    filename = null,
                    body = file.asRequestBody(MultipartBody.FORM)
                )
            )
        }
    }

}