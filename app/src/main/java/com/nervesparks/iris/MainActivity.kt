package com.nervesparks.iris

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.llama.cpp.LLamaAndroid
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.nervesparks.iris.data.UserPreferencesRepository
import com.nervesparks.iris.data.database.AppDatabase
import com.nervesparks.iris.ui.SettingsBottomSheet
import java.io.File

class MainViewModelFactory(
    private val context: Context,
    private val llamaAndroid: LLamaAndroid,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appDatabase: AppDatabase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context, llamaAndroid, userPreferencesRepository, appDatabase.chatMessageDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class MainActivity(
//    activityManager: ActivityManager? = null,
    downloadManager: DownloadManager? = null,
    clipboardManager: ClipboardManager? = null,
) : ComponentActivity() {
//    private val tag: String? = this::class.simpleName
//
//    private val activityManager by lazy { activityManager ?: getSystemService<ActivityManager>()!! }
    private val downloadManager by lazy { downloadManager ?: getSystemService<DownloadManager>()!! }
    private val clipboardManager by lazy { clipboardManager ?: getSystemService<ClipboardManager>()!! }

    private lateinit var viewModel: MainViewModel
    private lateinit var speechRecognizer: SpeechRecognizer

    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.let {
                if (it.isNotEmpty()) {
                    val spokenText = it[0]
                    viewModel.onSpokenText(spokenText)
                }
            }
        }
    }

    private val voiceServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // We don't need to do anything here, as the service runs in the background
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // This is called when the connection with the service has been unexpectedly disconnected
        }
    }

    // Get a MemoryInfo object for the device's current memory status.
//    private fun availableMemory(): ActivityManager.MemoryInfo {
//        return ActivityManager.MemoryInfo().also { memoryInfo ->
//            activityManager.getMemoryInfo(memoryInfo)
//        }
//    }

    val darkNavyBlue = Color(0xFF001F3D) // Dark navy blue color
    val lightNavyBlue = Color(0xFF3A4C7C)

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(darkNavyBlue, lightNavyBlue),
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = android.graphics.Color.parseColor("#FF070915"),
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = android.graphics.Color.parseColor("#FF070915"),
            )
        )
        super.onCreate(savedInstanceState)

        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build(),
        )
        val userPrefsRepo = UserPreferencesRepository.getInstance(applicationContext)
        val appDatabase = AppDatabase.getDatabase(applicationContext)

        val lLamaAndroid = LLamaAndroid.instance()
        val viewModelFactory = MainViewModelFactory(applicationContext, lLamaAndroid, userPrefsRepo, appDatabase)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

//        val free = Formatter.formatFileSize(this, availableMemory().availMem)
//        val total = Formatter.formatFileSize(this, availableMemory().totalMem)
        val transparentColor = Color.Transparent.toArgb()
        window.decorView.rootView.setBackgroundColor(transparentColor)
//        viewModel.log("Current memory: $free / $total")
//        viewModel.log("Downloads directory: ${getExternalFilesDir(null)}")

        val extFilesDir = getExternalFilesDir(null)

        val models = listOf(
//            Downloadable(
//                "SmolLM-135M.Q2_K.gguf",
//                Uri.parse("https://huggingface.co/QuantFactory/SmolLM-135M-GGUF/resolve/main/SmolLM-135M.Q2_K.gguf?download=true"),
//                File(extFilesDir, "SmolLM-135M.Q2_K.gguf")
//
//            ),
            Downloadable(
                "Llama-3.2-3B-Instruct-Q4_K_L.gguf",
                Uri.parse("https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_L.gguf?download=true"),
                File(extFilesDir, "Llama-3.2-3B-Instruct-Q4_K_L.gguf"),

            ),
            Downloadable(
                "Llama-3.2-1B-Instruct-Q6_K_L.gguf",
                Uri.parse("https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q6_K_L.gguf?download=true"),
                File(extFilesDir, "Llama-3.2-1B-Instruct-Q6_K_L.gguf"),
            ),
            Downloadable(
                "stablelm-2-1_6b-chat.Q4_K_M.imx.gguf",
                Uri.parse("https://huggingface.co/Crataco/stablelm-2-1_6b-chat-imatrix-GGUF/resolve/main/stablelm-2-1_6b-chat.Q4_K_M.imx.gguf?download=true"),
                File(extFilesDir, "stablelm-2-1_6b-chat.Q4_K_M.imx.gguf"),
            ),
        )

        if (extFilesDir != null) {
            viewModel.loadExistingModels(extFilesDir)
        }

        setContent {
            var showSettingSheet by remember { mutableStateOf(false) }
            var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
            var modelData by rememberSaveable { mutableStateOf<List<Map<String, String>>?>(null) }
            var selectedModel by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val sheetState = rememberModalBottomSheetState()

            var UserGivenModel by remember {
                mutableStateOf(
                    TextFieldValue(
                        text = viewModel.userGivenModel,
                        selection = TextRange(viewModel.userGivenModel.length), // Ensure cursor starts at the end
                    ),
                )
            }
            val isListening by VoiceServiceState.isListening.collectAsState()

            IrisApp(
                viewModel,
                clipboardManager,
                downloadManager,
                models,
                extFilesDir,
                onVoiceClicked = { startSpeechRecognition() },
                isListening = isListening,
            )
        }
    }

    private val wakeWordReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == VoiceService.WAKE_WORD_DETECTED_ACTION) {
                startSpeechRecognition()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, VoiceService::class.java)
        bindService(serviceIntent, voiceServiceConnection, Context.BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(this).registerReceiver(wakeWordReceiver, IntentFilter(VoiceService.WAKE_WORD_DETECTED_ACTION))
    }

    override fun onStop() {
        super.onStop()
        unbindService(voiceServiceConnection)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(wakeWordReceiver)
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        speechRecognitionLauncher.launch(intent)
    }
}

// [END android_compose_layout_material_modal_drawer]
