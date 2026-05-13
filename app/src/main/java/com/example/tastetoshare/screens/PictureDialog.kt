package com.example.tastetoshare.screens

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastetoshare.viewmodels.PictureDialogViewModel

@Composable
fun PictureDialogDestination(
    onDismiss: () -> Unit,
    returnUri: (String) -> Unit,
    onNavigateToPictureScreen: (Boolean) -> Unit,
    viewModel: PictureDialogViewModel = viewModel(factory = PictureDialogViewModel.provideFactory())
    ) {

    val isFrontCamera by viewModel.isFrontCamera.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            if (uri != null) {
                returnUri(uri.toString())
            }
        }
    }

    PictureDialog(onDismiss = onDismiss,
        onNavigateToPictureScreen = onNavigateToPictureScreen,
        onOpenGallery = {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )

            launcher.launch(intent)
        },
        isFrontCamera = isFrontCamera
    )
}


@Composable
fun PictureDialog(
    onDismiss: () -> Unit,
    onNavigateToPictureScreen: (Boolean) -> Unit,
    onOpenGallery: () -> Unit,
    isFrontCamera: Boolean,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
        ) {

            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenGallery,
                ) {
                    Text("Load Photo from Gallery")
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onNavigateToPictureScreen(isFrontCamera)
                    },
                ) {
                    Text("Take Picture")
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss,
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}