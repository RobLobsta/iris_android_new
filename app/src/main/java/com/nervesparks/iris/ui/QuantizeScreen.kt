package com.nervesparks.iris.ui

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.llama.cpp.LLamaAndroid
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nervesparks.iris.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@SuppressLint("Range")
private fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        result?.let {
            val cut = it.lastIndexOf('/')
            if (cut != -1) {
                result = it.substring(cut + 1)
            }
        }
    }
    return result ?: "unknown"
}


private fun getPathFromUri(context: Context, uri: Uri): String? {
    val fileName = getFileName(context, uri)
    val file = File(context.cacheDir, fileName)
    file.createNewFile()
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
    return file.absolutePath
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantizeScreen(
    viewModel: MainViewModel,
    onGoBack: () -> Unit
) {
    val context = LocalContext.current
    val llama = viewModel.llamaAndroid
    var inputFile by remember { mutableStateOf("") }
    var outputFile by remember { mutableStateOf("") }
    var selectedQuantization by remember { mutableStateOf(llama.quantizationOptions.first()) }
    var status by remember { mutableStateOf("") }
    var isQuantizing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                inputFile = getPathFromUri(context, it) ?: ""
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quantize Model") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select a model to quantize. The quantized model will be saved in the app's data directory.")

            Button(onClick = { filePickerLauncher.launch(arrayOf("*/*")) }) {
                Text("Select Input Model")
            }
            OutlinedTextField(
                value = inputFile,
                onValueChange = { inputFile = it },
                label = { Text("Input Model Path") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = outputFile,
                onValueChange = { outputFile = it },
                label = { Text("Output Model Name") },
                modifier = Modifier.fillMaxWidth()
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedQuantization,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Quantization Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    llama.quantizationOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedQuantization = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (inputFile.isNotBlank() && outputFile.isNotBlank()) {
                        isQuantizing = true
                        status = "Quantizing..."
                        coroutineScope.launch {
                            val success = llama.quantize(inputFile, outputFile, selectedQuantization)
                            status = if (success) {
                                "Quantization successful!"
                            } else {
                                "Quantization failed."
                            }
                            isQuantizing = false
                        }
                    }
                },
                enabled = !isQuantizing && inputFile.isNotBlank() && outputFile.isNotBlank()
            ) {
                if (isQuantizing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Start Quantization")
                }
            }

            if (status.isNotBlank()) {
                Text(status)
            }
        }
    }
}
