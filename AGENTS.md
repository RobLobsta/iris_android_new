# AGENTS.md: Instructions for AI Agents

This document provides instructions for AI agents working on the Iris Android repository.

## 1. Project Overview

Iris Android is a privacy-focused, offline-first LLM platform for Android. Refer to `VISION.md` for the detailed project vision.

## 2. Development Environment

*   The project is configured to be built in the provided sandbox environment.
*   The Android SDK, NDK, and other dependencies are pre-installed.
*   Do not attempt to modify the global development environment.

## 3. Building the Project

To build the project, run the following command from the root directory:

```bash
./gradlew build
```

To install the application on a connected device (not available in the current sandbox), you would run:

```bash
./gradlew installDebug
```

## 4. Code Style and Conventions

*   **Kotlin:** Follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
*   **Android:** Adhere to the [Android Code Style Guidelines](https://source.android.com/setup/contribute/code-style).
*   **C++ (for `llama.cpp`):** Follow the `llama.cpp` project's coding style.

## 5. Key Modules

*   `app`: The main Android application module. This is where the UI and application logic reside.
*   `llama`: An Android library module that wraps the `llama.cpp` functionality. It provides a higher-level API for the `app` module to interact with the LLM.
*   `llama.cpp`: A submodule containing the `llama.cpp` source code. Do not modify this directly unless it's for a specific bug fix or feature integration that can't be done in the `llama` module.

## 6. Workflow for Implementing New Features

1.  **Understand the request:** Thoroughly read the user's request and the project documentation (`VISION.md`, `AGENTS.md`).
2.  **Explore the code:** Use `ls` and `read_file` to understand the existing codebase.
3.  **Formulate a plan:** Create a detailed, step-by-step plan using `set_plan`.
4.  **Implement the changes:**
    *   Make changes to the appropriate modules.
    *   Verify each change by reading the file or using other verification methods.
5.  **Test your changes:**
    *   Build the project to ensure there are no compilation errors.
    *   If possible, add unit or integration tests for your new features.
6.  **Submit for review:**
    *   Once you are confident in your changes, ask the user for approval before submitting.

## 7. Semantic Search Implementation

*   **Database:** Use Room for storing chat history. The database schema should be designed to accommodate embeddings.
*   **Embeddings:** Use a suitable on-device embedding model. The `llama.cpp` library supports embedding generation.
*   **Vector Store:** Implement a simple in-memory or file-based vector store for semantic search. For a large number of vectors, consider a more efficient solution.

## 8. Llava Integration

*   **Image Preprocessing:** The `llama.cpp` library has support for Llava. You will need to handle image preprocessing on the Android side before passing the data to the native layer.
*   **UI:** The chat UI needs to be updated to allow users to select an image from their gallery.
*   **Inference:** The inference logic in the `llama` module needs to be updated to handle multimodal inputs.
