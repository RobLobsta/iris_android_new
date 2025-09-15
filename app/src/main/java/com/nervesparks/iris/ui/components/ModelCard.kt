package com.nervesparks.iris.ui.components

import android.app.DownloadManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nervesparks.iris.Downloadable
import com.nervesparks.iris.MainViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ModelCard(
    modelName: String,
    viewModel: MainViewModel,
    dm: DownloadManager,
    extFilesDir: File,
    downloadLink: String,
    showDeleteButton: Boolean,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isDeleted by remember { mutableStateOf(false) }
    var showDeletedMessage by remember { mutableStateOf(false) }
    val downloadProgress by remember { mutableStateOf(0f) }
    val isDownloading by remember { mutableStateOf(false) }

    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            showDeletedMessage = true
            kotlinx.coroutines.delay(1000)
            showDeletedMessage = false
            isDeleted = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xff1e293b), // slate-800
            contentColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = modelName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (modelName == viewModel.loadedModelName.value) {
                    Text(color = Color.Green, text = "Active", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            File(extFilesDir, modelName).let {
                Text(
                    text = if (formatFileSize(it.length()) != "0 Bytes") {
                        "Size: ${formatFileSize(it.length())}"
                    } else {
                        "Not Downloaded"
                    },
                    color = Color.Gray,
                    fontSize = 12.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isDownloading) {
                LinearProgressIndicator(
                    progress = downloadProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val coroutineScope = rememberCoroutineScope()
                val fullUrl = if (downloadLink.isNotEmpty()) {
                    downloadLink
                } else {
                    "https://huggingface.co/${viewModel.userGivenModel}/resolve/main/$modelName?download=true"
                }

                if (!showDeletedMessage) {
                    Downloadable.Button(
                        viewModel,
                        dm,
                        Downloadable(
                            modelName,
                            source = Uri.parse(fullUrl),
                            destination = File(extFilesDir, modelName),
                        ),
                    )
                }

                Spacer(modifier = Modifier.padding(5.dp))

                if (showDeleteButton && File(extFilesDir, modelName).exists()) {
                    Button(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.buttonColors(Color(0xFFb91c1c)),
                    ) {
                        Text(text = "Delete", color = Color.White)
                    }

                    if (showDeleteConfirmation) {
                        AlertDialog(
                            textContentColor = Color.LightGray,
                            containerColor = Color(0xFF233340),
                            modifier = Modifier.background(shape = RoundedCornerShape(8.dp), color = Color(0xFF233340)),
                            onDismissRequest = { showDeleteConfirmation = false },
                            title = { Text("Confirm Deletion", color = Color.White) },
                            text = { Text("Are you sure you want to delete this model? The app will restart after deletion.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (modelName == viewModel.loadedModelName.value) {
                                            viewModel.setDefaultModelName("")
                                        }
                                        coroutineScope.launch { viewModel.unload() }
                                        File(extFilesDir, modelName).delete()
                                        viewModel.showModal = false
                                        if (modelName == viewModel.loadedModelName.value) {
                                            viewModel.newShowModal = true
                                            showDeleteConfirmation = false
                                        }
                                        if (modelName == viewModel.loadedModelName.value) {
                                            viewModel.loadedModelName.value = ""
                                        }
                                        isDeleted = true
                                        viewModel.refresh = true
                                    },
                                    colors = ButtonDefaults.buttonColors(Color(0xFFb91c1c)),
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                Button(
                                    colors = ButtonDefaults.buttonColors(Color.Black),
                                    onClick = { showDeleteConfirmation = false },
                                ) {
                                    Text("Cancel")
                                }
                            },
                        )
                    }
                }
            }

            if (showDeletedMessage) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Model Deleted",
                    color = Color.Red,
                    fontSize = 15.sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (modelName == viewModel.loadedModelName.value) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val context = LocalContext.current
                    RadioButton(
                        selected = (modelName == viewModel.defaultModelName.value),
                        onClick = {
                            viewModel.setDefaultModelName(modelName)
                            Toast.makeText(
                                context,
                                "$modelName set as default model",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.Green,
                            unselectedColor = Color.Gray,
                        ),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set as Default Model",
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
        gb >= 1 -> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        else -> String.format("%d Bytes", size)
    }
}
