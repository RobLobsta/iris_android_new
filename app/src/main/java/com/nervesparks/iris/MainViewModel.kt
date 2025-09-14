package com.nervesparks.iris

import android.content.Context
import android.llama.cpp.LLamaAndroid
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.nervesparks.iris.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

import com.nervesparks.iris.data.database.ChatMessageDao

import com.nervesparks.iris.data.database.ChatMessage
import kotlinx.coroutines.flow.*

class MainViewModel(
    private val context: Context,
    val llamaAndroid: LLamaAndroid = LLamaAndroid.instance(),
    private val userPreferencesRepository: UserPreferencesRepository,
    private val chatMessageDao: ChatMessageDao
): ViewModel() {

    private val _streamingResponse = MutableStateFlow<ChatMessage?>(null)

    val chatMessages: StateFlow<List<ChatMessage>> = combine(
        chatMessageDao.getAllMessages(),
        _streamingResponse
    ) { messages, streaming ->
        if (streaming != null) {
            messages + streaming
        } else {
            messages
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val ragManager: RAGManager = RAGManager(context, llamaAndroid) {
        _uiState.update { it.copy(isRagReady = true) }
    }

    companion object {
//        @JvmStatic
//        private val NanosPerSecond = 1_000_000_000.0
    }


    private val _defaultModelName = mutableStateOf("")
    val defaultModelName: State<String> = _defaultModelName
    private val tag: String = "MainViewModel"

    init {
        loadDefaultModelName()
        Log.i(tag, llamaAndroid.system_info())
    }
    private fun loadDefaultModelName(){
        _defaultModelName.value = userPreferencesRepository.getDefaultModelName()
    }

    fun setDefaultModelName(modelName: String){
        userPreferencesRepository.setDefaultModelName(modelName)
        _defaultModelName.value = modelName
    }

    lateinit var selectedModel: String


    var newShowModal by mutableStateOf(false)
    var showDownloadInfoModal by mutableStateOf(false)
    var user_thread by mutableStateOf(0f)
    var topP by mutableStateOf(0f)
    var topK by mutableStateOf(0)
    var temp by mutableStateOf(0f)
    var perplexity by mutableStateOf(false)

    var allModels by mutableStateOf(
        listOf(
            mapOf(
                "name" to "Llama-3.2-1B-Instruct-Q6_K_L.gguf",
                "source" to "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q6_K_L.gguf?download=true",
                "destination" to "Llama-3.2-1B-Instruct-Q6_K_L.gguf"
            ),
            mapOf(
                "name" to "Llama-3.2-3B-Instruct-Q4_K_L.gguf",
                "source" to "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_L.gguf?download=true",
                "destination" to "Llama-3.2-3B-Instruct-Q4_K_L.gguf"
            ),
            mapOf(
                "name" to "stablelm-2-1_6b-chat.Q4_K_M.imx.gguf",
                "source" to "https://huggingface.co/Crataco/stablelm-2-1_6b-chat-imatrix-GGUF/resolve/main/stablelm-2-1_6b-chat.Q4_K_M.imx.gguf?download=true",
                "destination" to "stablelm-2-1_6b-chat.Q4_K_M.imx.gguf"
            ),

        )
    )

    private var first by mutableStateOf(
        true
    )
    var userSpecifiedThreads by mutableIntStateOf(2)
    var message by mutableStateOf("")
        private set

    var userGivenModel by mutableStateOf("")
    var SearchedName by mutableStateOf("")

    private var textToSpeech:TextToSpeech? = null

    var textForTextToSpeech = ""
    var stateForTextToSpeech by mutableStateOf(true)
        private set

    var eot_str = ""


    var refresh by mutableStateOf(false)

    fun loadExistingModels(directory: File) {
        // List models in the directory that end with .gguf
        directory.listFiles { file -> file.extension == "gguf" }?.forEach { file ->
            val modelName = file.name
            Log.i("This is the modelname", modelName)
            if (!allModels.any { it["name"] == modelName }) {
                allModels += mapOf(
                    "name" to modelName,
                    "source" to "local",
                    "destination" to file.name
                )
            }
        }

        if (defaultModelName.value.isNotEmpty()) {
            val loadedDefaultModel = allModels.find { model -> model["name"] == defaultModelName.value }

            if (loadedDefaultModel != null) {
                val destinationPath = File(directory, loadedDefaultModel["destination"].toString())
                if(loadedModelName.value == "") {
                    load(destinationPath.path, userThreads = user_thread.toInt())
                }
                currentDownloadable = Downloadable(
                    loadedDefaultModel["name"].toString(),
                    Uri.parse(loadedDefaultModel["source"].toString()),
                    destinationPath
                )
            } else {
                // Handle case where the model is not found
                allModels.find { model ->
                    val destinationPath = File(directory, model["destination"].toString())
                    destinationPath.exists()
                }?.let { model ->
                    val destinationPath = File(directory, model["destination"].toString())
                    if(loadedModelName.value == "") {
                        load(destinationPath.path, userThreads = user_thread.toInt())
                    }
                    currentDownloadable = Downloadable(
                        model["name"].toString(),
                        Uri.parse(model["source"].toString()),
                        destinationPath
                    )
                }
            }
        } else{
            allModels.find { model ->
                val destinationPath = File(directory, model["destination"].toString())
                destinationPath.exists()
            }?.let { model ->
                val destinationPath = File(directory, model["destination"].toString())
                if(loadedModelName.value == "") {
                    load(destinationPath.path, userThreads = user_thread.toInt())
                }
                currentDownloadable = Downloadable(
                    model["name"].toString(),
                    Uri.parse(model["source"].toString()),
                    destinationPath
                )
            }
        // Attempt to find and load the first model that exists in the combined logic

         }
    }



    fun textToSpeech(context: Context) {
        if (!getIsSending()) {
            // If TTS is already initialized, stop it first
            textToSpeech?.stop()

            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.let { txtToSpeech ->
                        txtToSpeech.language = Locale.US
                        txtToSpeech.setSpeechRate(1.0f)

                        // Add a unique utterance ID for tracking
                        val utteranceId = UUID.randomUUID().toString()

                        txtToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onDone(utteranceId: String?) {
                                // Reset state when speech is complete
                                CoroutineScope(Dispatchers.Main).launch {
                                    stateForTextToSpeech = true
                                }
                            }

                            override fun onError(utteranceId: String?) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    stateForTextToSpeech = true
                                }
                            }

                            override fun onStart(utteranceId: String?) {
                                // Update state to indicate speech is playing
                                CoroutineScope(Dispatchers.Main).launch {
                                    stateForTextToSpeech = false
                                }
                            }
                        })

                        txtToSpeech.speak(
                            textForTextToSpeech,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            utteranceId
                        )
                    }
                }
            }
        }
    }



    fun stopTextToSpeech() {
        textToSpeech?.apply {
            stop()  // Stops current speech
            shutdown()  // Releases the resources
        }
        textToSpeech = null

        // Reset state to allow restarting
        stateForTextToSpeech = true
    }



    var toggler by mutableStateOf(false)
    var showModal by  mutableStateOf(true)
    var showAlert by mutableStateOf(false)
    var switchModal by mutableStateOf(false)
    var currentDownloadable: Downloadable? by mutableStateOf(null)

    override fun onCleared() {
        textToSpeech?.shutdown()
        super.onCleared()

        viewModelScope.launch {
            try {

                llamaAndroid.unload()

            } catch (exc: IllegalStateException) {
                Log.e(tag, "onCleared() failed", exc)
            }
        }
    }

    fun send() {
        val userMessage = removeExtraWhiteSpaces(message)
        message = ""

        if (userMessage.isNotBlank() || selectedImageUri != null) {
            viewModelScope.launch {
                if (selectedImageUri != null && preprocessedImage != null) {
                    withContext(Dispatchers.IO) {
                        llamaAndroid.llava_eval_image(
                            LLamaAndroid.getContext(),
                            LLamaAndroid.getClipContext(),
                            preprocessedImage!!,
                            224,
                            224
                        )
                    }
                    selectedImageUri = null
                    preprocessedImage = null
                }

                val userEmbedding = withContext(Dispatchers.IO) {
                    llamaAndroid.embedding(userMessage)
                }
                val userChatMessage = ChatMessage(
                    text = userMessage,
                    timestamp = System.currentTimeMillis(),
                    isUser = true,
                    embedding = floatArrayToByteArray(userEmbedding)
                )
                chatMessageDao.insert(userChatMessage)

                val prompt = if (_uiState.value.isRagEnabled) {
                    val contextChunks = withContext(Dispatchers.IO) {
                        ragManager.search(userMessage)
                    }
                    val context = contextChunks.joinToString("\n\n")
                    "Context: $context\n\nQuestion: $userMessage"
                } else {
                    userMessage
                }

                val assistantResponse = StringBuilder()
                try {
                    if (first) {
                        val systemMessage = "This is a conversation between User and Iris, a friendly chatbot. Iris is helpful, kind, honest, good at writing, and never fails to answer any requests immediately and with precision."
                        val systemEmbedding = withContext(Dispatchers.IO) {
                            llamaAndroid.embedding(systemMessage)
                        }
                        val systemChatMessage = ChatMessage(
                            text = systemMessage,
                            timestamp = System.currentTimeMillis(),
                            isUser = false,
                            embedding = floatArrayToByteArray(systemEmbedding)
                        )
                        chatMessageDao.insert(systemChatMessage)

                        val hiMessage = "Hi"
                        val hiEmbedding = withContext(Dispatchers.IO) {
                            llamaAndroid.embedding(hiMessage)
                        }
                        val hiChatMessage = ChatMessage(
                            text = hiMessage,
                            timestamp = System.currentTimeMillis(),
                            isUser = true,
                            embedding = floatArrayToByteArray(hiEmbedding)
                        )
                        chatMessageDao.insert(hiChatMessage)

                        val howMayIHelpYouMessage = "How may I help You?"
                        val howMayIHelpYouEmbedding = withContext(Dispatchers.IO) {
                            llamaAndroid.embedding(howMayIHelpYouMessage)
                        }
                        val howMayIHelpYouChatMessage = ChatMessage(
                            text = howMayIHelpYouMessage,
                            timestamp = System.currentTimeMillis(),
                            isUser = false,
                            embedding = floatArrayToByteArray(howMayIHelpYouEmbedding)
                        )
                        chatMessageDao.insert(howMayIHelpYouChatMessage)
                        first = false
                    }

                    val messagesForTemplate = chatMessages.value.map {
                        mapOf("role" to if (it.isUser) "user" else "assistant", "content" to it.text)
                    } + mapOf("role" to "user", "content" to prompt)

                    llamaAndroid.send(llamaAndroid.getTemplate(messagesForTemplate))
                        .catch {
                            Log.e(tag, "send() failed", it)
                        }
                        .collect { response ->
                            assistantResponse.append(response)
                            _streamingResponse.value = ChatMessage(
                                text = assistantResponse.toString(),
                                timestamp = System.currentTimeMillis(),
                                isUser = false,
                                embedding = byteArrayOf()
                            )
                        }
                } finally {
                    val finalResponse = assistantResponse.toString()
                    if (finalResponse.isNotBlank()) {
                        val assistantEmbedding = withContext(Dispatchers.IO) {
                            llamaAndroid.embedding(finalResponse)
                        }
                        val assistantChatMessage = ChatMessage(
                            text = finalResponse,
                            timestamp = System.currentTimeMillis(),
                            isUser = false,
                            embedding = floatArrayToByteArray(assistantEmbedding)
                        )
                        chatMessageDao.insert(assistantChatMessage)
                    }
                    _streamingResponse.value = null
                }
            }
        }
    }

    fun toggleRag() {
        _uiState.update { it.copy(isRagEnabled = !it.isRagEnabled) }
    }

    private fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
        val byteBuffer = java.nio.ByteBuffer.allocate(floatArray.size * 4)
        for (value in floatArray) {
            byteBuffer.putFloat(value)
        }
        return byteBuffer.array()
    }

    suspend fun unload(){
        llamaAndroid.unload()
    }

    var tokensList = mutableListOf<String>()
    var benchmarkStartTime: Long = 0L
    var tokensPerSecondsFinal: Double by mutableStateOf(0.0)
    var isBenchmarkingComplete by mutableStateOf(false)

    fun myCustomBenchmark() {
        viewModelScope.launch {
            try {
                tokensList.clear()
                benchmarkStartTime = System.currentTimeMillis()
                isBenchmarkingComplete = false

                launch {
                    while (!isBenchmarkingComplete) {
                        delay(1000L)
                        val elapsedTime = System.currentTimeMillis() - benchmarkStartTime
                        if (elapsedTime > 0) {
                            tokensPerSecondsFinal = tokensList.size.toDouble() / (elapsedTime / 1000.0)
                        }
                    }
                }

                llamaAndroid.myCustomBenchmark()
                    .collect { emittedString ->
                        if (emittedString != null) {
                            tokensList.add(emittedString)
                            Log.d(tag, "Token collected: $emittedString")
                        }
                    }
            } catch (exc: IllegalStateException) {
                Log.e(tag, "myCustomBenchmark() failed", exc)
            } catch (exc: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(tag, "myCustomBenchmark() timed out", exc)
            } catch (exc: Exception) {
                Log.e(tag, "Unexpected error during myCustomBenchmark()", exc)
            } finally {
                val elapsedTime = System.currentTimeMillis() - benchmarkStartTime
                val finalTokensPerSecond = if (elapsedTime > 0) {
                    tokensList.size.toDouble() / (elapsedTime / 1000.0)
                } else {
                    0.0
                }
                Log.d(tag, "Benchmark complete. Tokens/sec: $finalTokensPerSecond")

                tokensPerSecondsFinal = finalTokensPerSecond
                isBenchmarkingComplete = true
            }
        }
    }

    var loadedModelName = mutableStateOf("");

    fun load(pathToModel: String, userThreads: Int)  {
        viewModelScope.launch {
            try{
                llamaAndroid.unload()
            } catch (exc: IllegalStateException){
                Log.e(tag, "load() failed", exc)
            }
            try {
                val modelName = pathToModel.split("/").last()
                loadedModelName.value = modelName
                newShowModal = false
                showModal= false
                showAlert = true
                llamaAndroid.load(pathToModel, userThreads = userThreads, topK = topK, topP = topP, temp = temp)
                showAlert = false

            } catch (exc: IllegalStateException) {
                Log.e(tag, "load() failed", exc)
            }
            showModal = false
            showAlert = false
            eot_str = llamaAndroid.send_eot_str()
        }
    }

    private fun removeExtraWhiteSpaces(input: String): String {
        return input.replace("\\s+".toRegex(), " ")
    }

    private fun parseTemplateJson(chatData: List<Map<String, String>> ):String{
        var chatStr = ""
        for (data in chatData){
            val role = data["role"]
            val content = data["content"]
            if (role != "log"){
                chatStr += "$role \n$content \n"
            }

        }
        return chatStr
    }
    fun updateMessage(newMessage: String) {
        message = newMessage
    }

    fun clear() {
        viewModelScope.launch {
            chatMessageDao.deleteAll()
        }
        first = true
    }

    fun log(message: String) {
    }

    fun getIsSending(): Boolean {
        return llamaAndroid.getIsSending()
    }

    private fun getIsMarked(): Boolean {
        return llamaAndroid.getIsMarked()
    }

    fun getIsCompleteEOT(): Boolean{
        return llamaAndroid.getIsCompleteEOT()
    }

    fun stop() {
        llamaAndroid.stopTextGeneration()
    }

    suspend fun perplexity(text: String): Double {
        return llamaAndroid.perplexity(text)
    }

    suspend fun scanModel(filePath: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext "Model file not found."
                }

                // Calculate checksum
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                file.inputStream().use { fis ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                }
                val checksum = digest.digest().joinToString("") { "%02x".format(it) }

                // Check compatibility
                var isCompatible = false
                try {
                    llamaAndroid.load(filePath, userThreads = 1, topK = 0, topP = 0f, temp = 0f)
                    isCompatible = true
                    llamaAndroid.unload()
                } catch (e: Exception) {
                    // Model is not compatible
                }

                "Checksum: $checksum\nCompatible: $isCompatible"
            } catch (e: Exception) {
                "Error scanning model: ${e.message}"
            }
        }
    }

    var selectedImageUri by mutableStateOf<Uri?>(null)
    var preprocessedImage by mutableStateOf<ByteArray?>(null)
    var mmprojPath by mutableStateOf<String?>(null)

    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
        viewModelScope.launch {
            preprocessedImage = preprocessImage(uri)
        }
    }

fun onSpokenText(spokenText: String) {
    updateMessage(spokenText)
}

    fun loadMmproj(path: String) {
        mmprojPath = path
        viewModelScope.launch {
            llamaAndroid.loadClipModel(path)
        }
    }

    private suspend fun preprocessImage(uri: Uri?): ByteArray? {
        if (uri == null) {
            return null
        }
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

                // Resize
                val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 224, 224, true)

                // Center Crop
                val cropSize = 224
                val x = (resizedBitmap.width - cropSize) / 2
                val y = (resizedBitmap.height - cropSize) / 2
                val croppedBitmap = android.graphics.Bitmap.createBitmap(resizedBitmap, x, y, cropSize, cropSize)

                val byteStream = java.io.ByteArrayOutputStream()
                croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, byteStream)
                byteStream.toByteArray()
            } catch (e: Exception) {
                Log.e(tag, "Error preprocessing image: ${e.message}")
                null
            }
        }
    }
}

fun sentThreadsValue(){

}