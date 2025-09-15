package com.nervesparks.iris.ui.components

import android.app.DownloadManager
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.nervesparks.iris.MainViewModel
import java.io.File

@Composable
fun SuggestedModels(
    extFileDir: File?,
    viewModel: MainViewModel,
    dm: DownloadManager,
) {
    Column {
        viewModel.allModels.take(3).forEach { model ->
            extFileDir?.let {
                model["source"]?.let { source ->
                    ModelCard(
                        model["name"].toString(),
                        viewModel = viewModel,
                        dm = dm,
                        extFilesDir = extFileDir,
                        downloadLink = source,
                        showDeleteButton = true,
                    )
                }
            }
        }
    }
}
