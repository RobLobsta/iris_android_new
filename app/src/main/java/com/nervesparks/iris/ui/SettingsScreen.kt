package com.nervesparks.iris.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nervesparks.iris.MainViewModel
import com.nervesparks.iris.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onParamsScreenButtonClicked: () -> Unit,
    onModelsScreenButtonClicked: () -> Unit,
    onAboutScreenButtonClicked: () -> Unit,
    onBenchMarkScreenButtonClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { onBackButtonClicked() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171E2C)),
                ) {
                    SettingsRow(
                        text = "Models",
                        iconRes = R.drawable.data_exploration_models_svgrepo_com,
                        onClick = onModelsScreenButtonClicked,
                    )

                    SettingsDivider()

                    SettingsRow(
                        text = "Change Parameters",
                        iconRes = R.drawable.setting_4_svgrepo_com,
                        onClick = onParamsScreenButtonClicked,
                    )

                    SettingsDivider()

                    SettingsRow(
                        text = "BenchMark",
                        iconRes = R.drawable.bench_mark_icon,
                        onClick = onBenchMarkScreenButtonClicked,
                    )

                    SettingsDivider()

                    SettingsRow(
                        text = "About",
                        iconRes = R.drawable.information_outline_svgrepo_com,
                        onClick = onAboutScreenButtonClicked,
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171E2C)),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "RAG",
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = uiState.isRagEnabled,
                            onCheckedChange = { viewModel.toggleRag() },
                            enabled = uiState.isRagReady,
                        )
                    }

                    SettingsDivider()

                    SettingsRow(
                        text = "Quantize",
                        iconRes = R.drawable.settings_gear_svgrepo_com,
                        onClick = {
                            context.startActivity(Intent(context, com.nervesparks.iris.QuantizeActivity::class.java))
                        },
                    )

                    SettingsDivider()

                    SettingsRow(
                        text = "Embedding",
                        iconRes = R.drawable.data_exploration_models_svgrepo_com,
                        onClick = {
                            context.startActivity(Intent(context, com.nervesparks.iris.EmbeddingActivity::class.java))
                        },
                    )

                    SettingsDivider()

                    SettingsRow(
                        text = "Search",
                        iconRes = R.drawable.search_svgrepo_com__3_,
                        onClick = {
                            context.startActivity(Intent(context, com.nervesparks.iris.SearchActivity::class.java))
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsRow(text: String, iconRes: Int, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .clickable { onClick() },
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color(0xFFa2a3a8),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        Spacer(Modifier.weight(1f))
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.right_arrow_svgrepo_com),
            contentDescription = null,
            tint = Color(0xFFa2a3a8),
        )
    }
}

@Composable
fun SettingsDivider() {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        color = Color(0xFF333948),
        thickness = 1.dp,
    )
}
