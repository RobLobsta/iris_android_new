package com.nervesparks.iris.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nervesparks.iris.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmbeddingScreen(
    viewModel: MainViewModel,
    onGoBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var embedding by remember { mutableStateOf<FloatArray?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Get Embedding") },
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
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter text") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        embedding = viewModel.llamaAndroid.embedding(text)
                        isLoading = false
                    }
                },
                enabled = !isLoading && text.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Get Embedding")
                }
            }

            embedding?.let {
                LazyColumn {
                    item {
                        Text("Embedding:")
                    }
                    items(it.size) { index ->
                        Text(it[index].toString())
                    }
                }
            }
        }
    }
}
