# Iris Android App UI Flow and Button Functionality

This document outlines the UI flow of the Iris Android application and details the functionality of each button and interactive element.

## 1. Main Chat Screen (`MainChatScreen.kt`)

This is the main screen of the app. It's where the user interacts with the chatbot.

### UI Elements and Functionality

*   **Chat Messages:**
    *   Displays the conversation between the user and the assistant.
    *   If the chat is empty, it shows a welcome message and a list of prompts.
    *   Long-pressing a message opens a bottom sheet with options to **Copy Text**, **Select Text**, and **Text to Speech**.

*   **Input Field:**
    *   A `TextField` where the user can type their message.

*   **Buttons:**
    *   **Send Button:**
        *   An `IconButton` with a send icon.
        *   Sends the message from the input field to the chatbot.
        *   Only enabled when a model is loaded.
    *   **Stop Button:**
        *   An `IconButton` with a stop icon.
        *   Stops the chatbot from generating a response.
        *   Only visible when the model is generating a response.
    *   **Voice Input Button:**
        *   An `IconButton` with a microphone icon.
        *   Starts speech recognition to allow the user to speak their message.
    *   **Add Photo Button:**
        *   An `IconButton` with a photo icon.
        *   Opens the image gallery to select an image to send to the chatbot.
    *   **Add Mmproj Button:**
        *   An `IconButton` with a file icon.
        *   Opens the file picker to select a `mmproj.gguf` file.
    *   **RAG Toggle:**
        *   A `Switch` to enable or disable Retrieval-Augmented Generation.
    *   **Settings Button (Top App Bar):**
        *   An `IconButton` with a gear icon.
        *   Navigates to the **Settings Screen**.
    *   **New Chat Button (Top App Bar):**
        *   An `IconButton` with an edit icon.
        *   Clears the chat history and starts a new conversation.

## 2. Settings Screen (`SettingsScreen.kt`)

This screen provides access to various settings and other screens in the app.

### UI Elements and Functionality

*   **Models:**
    *   A `SettingsRow` that navigates to the **Models Screen**.
*   **Change Parameters:**
    *   A `SettingsRow` that navigates to the **Parameters Screen**.
*   **BenchMark:**
    *   A `SettingsRow` that navigates to the **BenchMark Screen**.
*   **About:**
    *   A `SettingsRow` that navigates to the **About Screen**.

## 3. Models Screen (`ModelsScreen.kt`)

This screen allows the user to manage the models.

### UI Elements and Functionality

*   **Search Hugging-Face Models:**
    *   A button that navigates to the **Search Results Screen**.
*   **Suggested Models:**
    *   A list of suggested models to download.
*   **My Models:**
    *   A list of models that the user has already downloaded.
*   **Model Card:**
    *   Each model is displayed in a `ModelCard`.
    *   **Download Button:** Downloads the model.
    *   **Delete Button:** Deletes the model from the device.
*   **Refresh Button (Top App Bar):**
    *   An `IconButton` that refreshes the list of models.

## 4. Parameters Screen (`ParametersScreen.kt`)

This screen allows the user to configure the model's parameters.

### UI Elements and Functionality

*   **Thread Selection:**
    *   A `Slider` to select the number of threads for processing.
*   **Perplexity:**
    *   A `Switch` to enable or disable perplexity calculation.
*   **Temperature:**
    *   A `Slider` to adjust the temperature value.
*   **Top P:**
    *   A `Slider` to adjust the Top P value.
*   **Top K:**
    *   A `Slider` to adjust the Top K value.
*   **Save Button:**
    *   A `Button` to save the changes.
*   **Reset Default Button:**
    *   A `Button` to reset the settings to their default values.

## 5. Search Results Screen (`SearchResultScreen.kt`)

This screen allows the user to search for models on Hugging Face.

### UI Elements and Functionality

*   **Search Input:**
    *   An `OutlinedTextField` where the user can enter the name of the model to search for (e.g., "bartowski/Llama-3.2-1B-Instruct-GGUF").
*   **Search Button:**
    *   A `Button` that initiates the search. It makes an API call to the Hugging Face API to get the model information.
*   **Search Results:**
    *   The search results are displayed in a `LazyColumn`.
    *   Each model is displayed in a `ModelCard`.

## 6. About Screen (`AboutScreen.kt`)

This screen displays information about the app. It's a simple informational screen with no interactive elements other than scrolling.

### Content

*   **Welcome to Iris:** A brief introduction to the app.
*   **Features:** A list of the app's key features.
*   **FAQs:** A list of frequently asked questions and their answers.

## 7. BenchMark Screen (`BenchMarkScreen.kt`)

This screen is for benchmarking the performance of the models.

### UI Elements and Functionality

*   **Device Info:**
    *   Displays information about the device, such as the model, Android version, processor, and available threads.
*   **Start Benchmark Button:**
    *   A `Button` that starts the benchmark test.
    *   It shows a confirmation dialog before starting.
*   **Progress Indicator:**
    *   A `CircularProgressIndicator` is displayed while the benchmark is running.
*   **Benchmark Results:**
    *   The results of the benchmark test are displayed in a `Card`.
*   **Tokens Per Second:**
    *   Displays the tokens per second speed.

## 8. Navigation Drawer

The app has a modal navigation drawer that can be opened from the main chat screen.

### UI Elements and Functionality

*   **Active Model:**
    *   Displays the name of the currently loaded model.
*   **Star us:**
    *   A button that opens the project's GitHub page.
*   **NerveSparks.com:**
    *   A button that opens the NerveSparks website.
*   **powered by llama.cpp:**
    *   A text that links to the `llama.cpp` website.
