package com.nervesparks.iris.ui

import android.app.DownloadManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nervesparks.iris.MainViewModel
import com.nervesparks.iris.R
import com.nervesparks.iris.ui.components.LoadingModal
import com.nervesparks.iris.ui.components.MyModels
import com.nervesparks.iris.ui.components.SuggestedModels
import java.io.File

@Composable
fun ModelsScreen(
    extFileDir: File?,
    viewModel: MainViewModel,
    onSearchResultButtonClick: () -> Unit,
    dm: DownloadManager,
) {
    val refresh = viewModel.refresh
    if (refresh) {
        viewModel.refresh = false
    }

    Box {
        if (viewModel.showAlert) {
            LoadingModal(viewModel)
        }

        LazyColumn(modifier = Modifier.padding(horizontal = 15.dp)) {
            item {
                Header(onSearchResultButtonClick)
            }

            item {
                SuggestedModels(extFileDir, viewModel, dm)
            }

            item {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.DarkGray,
                    thickness = 1.dp,
                )
            }

            item {
                MyModels(extFileDir, viewModel, dm)
            }
        }
    }
}

@Composable
private fun Header(onSearchResultButtonClick: () -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .clickable { onSearchResultButtonClick() },
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.search_svgrepo_com__3_),
                contentDescription = "Parameters",
                tint = Color.White,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Search Hugging-Face Models",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 7.dp),
            )
            Spacer(Modifier.weight(1f))
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.right_arrow_svgrepo_com),
                contentDescription = null,
                tint = Color.White,
            )
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color.DarkGray, // Set the color of the divider
            thickness = 1.dp,
        )
        Spacer(Modifier.height(25.dp))
        Text(
            text = "Suggested Models",
            color = Color.White.copy(alpha = .5f),
            modifier = Modifier.padding(5.dp),
            fontSize = 18.sp,
        )
    }
}
