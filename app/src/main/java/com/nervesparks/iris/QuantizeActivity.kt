package com.nervesparks.iris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.nervesparks.iris.ui.QuantizeScreen

class QuantizeActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val llamaAndroid = android.llama.cpp.LLamaAndroid.instance()
        val userPrefsRepo = com.nervesparks.iris.data.UserPreferencesRepository.getInstance(applicationContext)
        val appDatabase = com.nervesparks.iris.data.database.AppDatabase.getDatabase(applicationContext)
        val viewModelFactory = MainViewModelFactory(applicationContext, llamaAndroid, userPrefsRepo, appDatabase)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        setContent {
            QuantizeScreen(
                viewModel = viewModel,
                onGoBack = { finish() }
            )
        }
    }
}
