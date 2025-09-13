# Project Vision: Iris Android

## 1. Overview

Iris Android is a privacy-focused, offline-first, and highly customizable large language model (LLM) platform for Android devices. It empowers users to bring their own models, ensuring data sovereignty and enabling powerful AI capabilities without relying on cloud services. The application will provide enterprise-level features for model management, inference, and interaction, including advanced capabilities like semantic search and multimodal understanding with Llava.

## 2. Core Principles

*   **Offline-First:** All core functionalities, including model inference and semantic search, must work entirely offline.
*   **User-Owned Models:** Users can import, manage, and use their own LLM models. The app will provide tools for model discovery, health checks, and quantization.
*   **Privacy and Security:** By keeping all data and models on the user's device, we ensure maximum privacy and security. No user data will be sent to external servers.
*   **High Performance:** The application will be optimized for performance on a wide range of Android devices, leveraging the NDK for CPU-based inference.
*   **Extensibility:** The architecture will be modular to allow for the integration of new models and features in the future.

## 3. Key Features

### Phase 1: Core LLM and Semantic Search

*   **Model Management:**
    *   Import models from device storage.
    *   Scan models for health and compatibility.
    *   Quantize models to optimize for mobile performance.
    *   Delete models.
*   **Chat Interface:**
    *   A simple, intuitive interface for interacting with the loaded LLM.
    *   Chat history is stored locally.
*   **Semantic Search:**
    *   Search through chat history based on meaning, not just keywords.
    *   Uses on-device embeddings and a local vector store.
*   **RAG System:**
    *   A Retrieval-Augmented Generation system to provide high-confidence answers from a local knowledge base.

### Phase 2: Multimodal (Llava) Integration

*   **Image-based Prompts:**
    *   Users can include an image in their prompt.
    *   The app will support asking questions about the image.
*   **Llava Model Support:**
    *   Integrate Llava models for multimodal inference.

## 4. Target Audience

*   **Privacy-conscious individuals:** Users who want to leverage the power of LLMs without compromising their privacy.
*   **AI enthusiasts and developers:** Users who want to experiment with different models and configurations on their mobile devices.
*   **Enterprise users:** Businesses that require a secure, offline-first LLM solution for their employees.
