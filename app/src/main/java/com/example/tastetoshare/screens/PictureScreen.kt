package com.example.tastetoshare.ui.screens

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastetoshare.viewmodels.PictureScreenViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun PictureScreenDestination(
    returnUri: (String) -> Unit,
    viewModel: PictureScreenViewModel = viewModel(factory = PictureScreenViewModel.provideFactory())
) {
    val isFrontCamera by viewModel.isFrontCamera.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE
            )
        }
    }

    controller.cameraSelector = if (isFrontCamera) {
        CameraSelector.DEFAULT_FRONT_CAMERA
    } else CameraSelector.DEFAULT_BACK_CAMERA

    PictureScreen(
        context = context,
        controller = controller,
        returnUri = returnUri,
        toggleCamera = viewModel::toggleCamera
    )
}


@Composable
fun PictureScreen(
    context: Context,
    controller: LifecycleCameraController,
    returnUri: (String) -> Unit,
    toggleCamera: () -> Unit,
) {

    Box(modifier = Modifier.fillMaxSize()) {

        val lifecycleOwner = LocalLifecycleOwner.current
        AndroidView(
            factory = {
                PreviewView(it).apply {
                    this.controller = controller
                    controller.bindToLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        ) {

            Button(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                onClick = {
                    takePhoto(
                        context = context,
                        controller = controller,
                        onReturnUri = { uri -> returnUri(uri) }
                    )
                }
            ) {}

            IconButton(
                modifier = Modifier.align(Alignment.Center)
                    .padding(start = 200.dp)
                    .size(80.dp),
                onClick = {
                    toggleCamera()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Cached,
                    contentDescription = "Switch camera",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

    }
}


private fun takePhoto(
    context: Context,
    controller: LifecycleCameraController,
    onReturnUri: (uri: String) -> Unit
) {
    val TAG = "CameraXApp"
    val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
        .format(System.currentTimeMillis())

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/CameraX-Images"
            )
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    controller.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {

            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val uri = output.savedUri?.toString() ?: return

                onReturnUri(uri)

                Toast.makeText(
                    context,
                    "Photo saved: $uri",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )
}