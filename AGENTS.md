# AGENTS.md: A Guide for AI Agents

Welcome, agent! This document provides comprehensive instructions for working on the Iris Android repository. Please follow these guidelines carefully to ensure a smooth and efficient workflow.

## 1. Project Overview

Iris is a privacy-focused, offline-first large language model (LLM) application for Android. It allows users to run powerful AI models directly on their device, ensuring that their data remains private and secure.

*   **Project Vision**: For a detailed understanding of the project's goals, please read [**VISION.md**](VISION.md).
*   **UI Flow**: For a walkthrough of the app's user interface, please see the [**UI Flow Guide**](UI_FLOW.md).

## 2. Environment Setup

The development environment in the sandbox is pre-configured with the necessary tools to build and work on this project. The following tools are available:

*   Android SDK (version 33)
*   Android NDK (version 25.2.9519653)
*   CMake (version 3.22.1)
*   OpenJDK (version 11)

You do not need to perform any additional setup steps. The environment is ready for you to start building the project.

## 3. Building the Project

To build the project and generate a debug APK, run the following command from the root directory:

```bash
./gradlew assembleDebug
```

The generated APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

**Note:** In the current sandboxed environment, you cannot install or run the application on an emulator or physical device. However, you can still perform builds to verify that your changes compile successfully.

## 4. Running Tests

This project currently lacks a comprehensive suite of unit and integration tests. This is an area where your contributions would be highly valuable!

If you are implementing a new feature, please consider adding tests for it. You can run any existing or new tests using the following command:

```bash
./gradlew test
```

## 5. Workflow for Implementing New Features

When you are assigned a task to implement a new feature, please follow this workflow:

1.  **Understand the Request**: Thoroughly read the user's request and all relevant documentation (`VISION.md`, `UI_FLOW.md`, `AGENTS.md`).
2.  **Explore the Code**: Use `ls`, `read_file`, and `grep` to understand the existing codebase and identify the files you will need to modify.
3.  **Formulate a Plan**: Create a detailed, step-by-step plan using the `set_plan` tool. Your plan should include implementation, testing, and verification steps.
4.  **Implement the Changes**:
    *   Make your changes to the appropriate modules.
    *   Follow the code style guidelines (see section 6).
    *   Verify each change by reading the file or using other verification methods.
5.  **Test Your Changes**:
    *   Build the project to ensure there are no compilation errors.
    *   If possible, add unit or integration tests for your new features and run them.
6.  **Request a Review**: Once you are confident in your changes, use the `request_code_review` tool to get feedback on your work.
7.  **Submit Your Work**: After addressing any feedback from the code review, use the `submit` tool to submit your changes for approval.

## 6. Code Style and Conventions

*   **Kotlin**: Follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
*   **Android**: Adhere to the [Android Code Style Guidelines](https://source.android.com/setup/contribute/code-style).
*   **C++ (for `llama.cpp`)**: Follow the `llama.cpp` project's coding style.

## 7. Key Modules

*   `app`: The main Android application module. This is where the UI and application logic reside.
*   `llama`: An Android library module that wraps the `llama.cpp` functionality. It provides a higher-level API for the `app` module to interact with the LLM.
*   `llama.cpp`: A submodule containing the `llama.cpp` source code. Do not modify this directly unless it's for a specific bug fix or feature integration that can't be done in the `llama` module.

## 8. Common Tasks

### Adding a New Screen

1.  Create a new Kotlin file in the `app/src/main/java/com/nervesparks/iris/ui/` directory.
2.  Define a new Composable function for your screen.
3.  Add the new screen to the navigation graph in `app/src/main/java/com/nervesparks/iris/ui/NavGraph.kt`.

### Implementing a New Feature in the `llama` Module

1.  Identify the C++ functions in `llama.cpp` that you need to call.
2.  Add a new function to the `LlamaRepository.kt` file in the `llama` module that calls the C++ function using JNI.
3.  Expose the new function to the `app` module through the `LlamaRepository` interface.

### Updating the UI

*   All UI components are built using Jetpack Compose.
*   The UI files are located in the `app/src/main/java/com/nervesparks/iris/ui/` directory.
*   The theme and styling are defined in the `app/src/main/java/com/nervesparks/iris/ui/theme/` directory.

---

Thank you for your contribution to Iris! By following these guidelines, you will help us maintain a high-quality codebase and create a better product for our users.
