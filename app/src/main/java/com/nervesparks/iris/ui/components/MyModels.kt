package com.nervesparks.iris.ui.components

import android.app.DownloadManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nervesparks.iris.MainViewModel
import java.io.File

@Composable
fun MyModels(
    extFileDir: File?,
    viewModel: MainViewModel,
    dm: DownloadManager,
) {
    Column {
        val myModels = viewModel.allModels.drop(3)
        if (myModels.isEmpty()) {
            Text(
                text = "No models to show",
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, start = 2.dp),
            )
        } else {
            myModels.forEach { model ->
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
}
