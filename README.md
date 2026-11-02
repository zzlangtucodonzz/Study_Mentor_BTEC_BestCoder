<div align="center">

<img src="art/study_mentor_logo.png" alt="Study Mentor Logo" width="140" height="140" style="border-radius: 28px;" />

# 🎓 Study Mentor — Your AI Study Companion
### *Next-Generation Multimodal AI Educational Assistant with Real-Time Vision & RPG Gamification*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![CameraX](https://img.shields.io/badge/CameraX-1.4.1-34A853?style=for-the-badge&logo=googlecamera&logoColor=white)](https://developer.android.com/training/camerax)
[![Firebase](https://img.shields.io/badge/Firebase-Auth%20%26%20Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)
[![OpenAI SSE](https://img.shields.io/badge/AI%20Streaming-SSE%20Token%20Stream-412991?style=for-the-badge&logo=openai&logoColor=white)](https://platform.openai.com)

<p align="center">
  <b>Empowering students to master complex STEM concepts through instant multimodal problem solving, interactive viewfinder cropping, and motivating RPG progression.</b>
</p>

---

</div>

## 📖 Table of Contents
- [🌟 Overview & Motivation](#-overview--motivation)
- [✨ Core Features](#-core-features)
  - [1. Multimodal Ask AI & Live Camera Viewfinder](#1-multimodal-ask-ai--live-camera-viewfinder)
  - [2. Interactive Resizable Crop Viewfinder](#2-interactive-resizable-crop-viewfinder)
  - [3. Precision Aspect-Ratio Bitmap Cropping](#3-precision-aspect-ratio-bitmap-cropping)
  - [4. Smart Gamification & RPG Leveling System](#4-smart-gamification--rpg-leveling-system)
  - [5. Daily Practice & Assessment Engine](#5-daily-practice--assessment-engine)
  - [6. Cloud Session History & Profile Customization](#6-cloud-session-history--profile-customization)
- [📸 Application Preview](#-application-preview)
- [🛠️ Tech Stack & Architecture](#️-tech-stack--architecture)
- [📂 Project Structure](#-project-structure)
- [🚀 Installation & Setup](#-installation--setup)
- [🔐 Environment & Secrets Configuration](#-environment--secrets-configuration)
- [🏆 Best Coder 2026 Highlights](#-best-coder-2026-highlights)
- [📄 License](#-license)

---

## 🌟 Overview & Motivation

In modern education, students often encounter conceptual bottlenecks while doing self-study or homework. Traditional study tools present two major pain points:
1. **Friction in Querying**: Manually typing mathematical equations, geometric notations, or chemistry formulas on a mobile keyboard is tedious and prone to syntax errors.
2. **Lack of Engagement & Motivation**: Passive reading and answer lookups lead to rapid student burnout without continuous positive feedback.

**Study Mentor** bridges this gap by merging **high-performance mobile vision** with **generative AI streaming** and **RPG gamification mechanics**:
- Students point their camera at any textbook equation or worksheet.
- A dynamic, interactive viewfinder lets them tightly frame the exact problem of interest.
- With a single tap, the app crops the Region of Interest (ROI), streams tokens via Server-Sent Events (SSE) in real-time, and generates step-by-step Socratic explanations.
- Every question asked and quiz solved awards Experience Points (XP), leveling up the student's gaming rank from **Novice** all the way to **Mythic Grandmaster**.

---

## ✨ Core Features

### 1. Multimodal Ask AI & Live Camera Viewfinder
- **Real-Time Token Streaming (SSE)**: Powered by a resilient, low-latency streaming pipeline using `HttpURLConnection` and coroutine `Flow<String>`. Tokens render incrementally character-by-character as the model generates them, eliminating user waiting fatigue.
- **One-Tap Capture & "Lazy User" Logic**: Tapping the glowing contextual center Floating Action Button (FAB) triggers CameraX `ImageCapture`, extracts the cropped math problem, detects whether the input query is blank, and automatically falls back to:
  > *"Solve this math problem step-by-step and explain the concepts."*
  The student gets an instant solution with **zero extra clicks**.
- **Contextual Animated FAB**: The center navigation button features a continuous, hardware-accelerated 360° rotating RGB LED ring glow. When on the "Ask AI" tab, it dynamically morphs from sparkles (`AutoAwesome`) to a paper airplane (`Send`), providing instant feedback with an inline `CircularProgressIndicator` while the AI thinks.

### 2. Interactive Resizable Crop Viewfinder
- **Fluid Gesture Manipulation**: Built on Jetpack Compose `pointerInput` and `detectDragGestures`, allowing students to grab and drag any of the 4 visual corner handles to expand, contract, or pan the bounding box.
- **Dynamic Punch-Out Shader**: Utilizes an offscreen graphics layer (`CompositingStrategy.Offscreen`) with `BlendMode.Clear` to produce a 100% transparent cutout window over the live CameraX preview while dimming the surrounding background with `Color.Black.copy(alpha = 0.6f)`.
- **Photomath-Style Guide Brackets**: High-visibility L-bracket corner marks and concentric primary-blue handle dots visually anchor the capture region.

### 3. Precision Aspect-Ratio Bitmap Cropping
- **EXIF-Aware Auto-Rotation**: Inspects JPEG metadata via `android.media.ExifInterface` and transforms the raw camera buffer by $90^\circ, 180^\circ,$ or $270^\circ$ to guarantee exact alignment with the portrait device orientation.
- **Scale-Type Geometric Mapping**: Implements the mathematical transformation formula for `PreviewView.ScaleType.FILL_CENTER`:
  $$\text{scale} = \max\left(\frac{W_{\text{preview}}}{W_{\text{bitmap}}}, \frac{H_{\text{preview}}}{H_{\text{bitmap}}}\right)$$
  $$\text{offsetX} = \frac{(W_{\text{bitmap}} \cdot \text{scale}) - W_{\text{preview}}}{2}, \quad \text{offsetY} = \frac{(H_{\text{bitmap}} \cdot \text{scale}) - H_{\text{preview}}}{2}$$
  Accurately translates screen pixel coordinates into sub-sampled bitmap bounds before compressing to JPEG 80% (capped at 1024×1024), minimizing network payload and preventing Out-Of-Memory (OOM) exceptions.

### 4. Smart Gamification & RPG Leveling System
- **Leveling Formula**: Experience points earned from asking AI questions (+10 XP) and completing practice quizzes (up to +150 XP) contribute to a persistent RPG progression curve:
  $$\text{Level} = \left\lfloor \frac{\text{Total XP}}{100} \right\rfloor + 1$$
- **10 Distinct Mastery Ranks**:
  `Novice (Lvl 1)` ➔ `Scholar (Lvl 2)` ➔ `Adept (Lvl 3)` ➔ `Specialist (Lvl 4)` ➔ `Expert (Lvl 5)` ➔ `Master (Lvl 6)` ➔ `Grandmaster (Lvl 7)` ➔ `Champion (Lvl 8)` ➔ `Legend (Lvl 9)` ➔ `Mythic (Lvl 10+)`.
- **Cloud Synchronization**: User statistics are synced in real-time with Google Cloud Firestore using reactive Kotlin `StateFlow`, updating user progress cards instantaneously across Home, Practice, and Profile screens.

### 5. Daily Practice & Assessment Engine
- **Scalable Quiz Architecture**: Modular repository supporting both `MULTIPLE_CHOICE` and `SHORT_ANSWER` question types.
- **Proportional XP Scoring**:
  $$\text{Earned XP} = \text{round}\left(\frac{\text{Correct Answers}}{\text{Total Questions}} \times \text{Max XP}\right)$$
- **Interactive Quiz Interface**: Clean card layout, real-time feedback, and automated score dialogue with XP rewards on completion.

### 6. Cloud Session History & Profile Customization
- **Chat Session Persistence**: All discussions, attached image references, and AI responses are preserved in Firestore under user-specific sub-collections.
- **Library History Viewer**: Browse and reload past study sessions to review step-by-step solutions before exams.
- **Custom Profile**: Manage authentication, update student credentials, and choose custom profile avatars.

---

## 🛠️ Tech Stack & Architecture

Study Mentor is architected following Google's recommended **Modern Android Development (MAD)** standards and Clean Architecture principles.

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│   Jetpack Compose (Material 3) + StateFlow Observers   │
└──────────────────────────┬─────────────────────────────┘
                           │ UI Events / State Collection
┌──────────────────────────▼─────────────────────────────┐
│                   ViewModel Layer                      │
│   AuthViewModel | ChatViewModel | PracticeViewModel    │
└──────────────────────────┬─────────────────────────────┘
                           │ Coroutines (Dispatchers.IO)
┌──────────────────────────▼─────────────────────────────┐
│                   Repository Layer                     │
│ FirestoreChatRepo | GamificationRepo | QuizRepository  │
└─────────────┬────────────────────────────┬─────────────┘
              │                            │
┌─────────────▼───────────────┐ ┌──────────▼───────────────┐
│     Remote Network / AI     │ │   Firebase Cloud Suite   │
│ Custom SSE Stream / OpenAI  │ │ Firestore & Auth SDK     │
└─────────────────────────────┘ └──────────────────────────┘
```

| Technology | Purpose |
| :--- | :--- |
| **Kotlin 2.2.10** | Modern, concise, null-safe language for Android |
| **Jetpack Compose (BOM 2026.02.01)** | Declarative UI toolkit with Material Design 3 guidelines |
| **AndroidX CameraX (1.4.1)** | Lifecycle-aware camera hardware preview and high-speed image capture |
| **Navigation Compose (2.8.5)** | Single-activity backstack and animated tab navigation |
| **Firebase Auth & Cloud Firestore** | Secure user authentication and distributed document database |
| **Kotlin Coroutines & StateFlow** | Asynchronous background processing and unidirectional reactive state |
| **Custom SSE HttpURLConnection** | Resilient Server-Sent Events parser delivering real-time streaming tokens |
| **Coil Compose (2.6.0)** | Asynchronous image decoding, disk caching, and memory cache management |
| **AndroidX ExifInterface** | High-precision sensor rotation mapping for camera captures |

---

## 📂 Project Structure

```text
Study_Mentor_BTEC_BestCode/
├── app/
│   ├── build.gradle.kts                # Application build config & dependencies
│   ├── google-services.json            # Firebase configuration file
│   └── src/main/
│       ├── AndroidManifest.xml         # Permissions (Camera, Internet) & Activity
│       └── java/com/example/study_mentor/
│           ├── MainActivity.kt         # Edge-to-Edge root Activity
│           ├── OpenAIApiClient.kt      # SSE Streaming HTTP Network Client
│           ├── GeminiApiClient.kt      # Multimodal fallback interface
│           ├── data/                   # Data Models & Repositories
│           │   ├── ChatMessage.kt      # Chat history model
│           │   ├── GamificationModels  # RPG level math & UserStats
│           │   ├── GamificationRepo    # Firestore XP increment logic
│           │   ├── FirestoreChatRepo   # Cloud session persistence
│           │   ├── QuizModels.kt       # Multi-choice & short-answer schemas
│           │   └── QuizRepository.kt   # Practice curriculum modules
│           └── ui/                     # Presentation Layer (Compose)
│               ├── MainScreen.kt       # Bottom navigation & RGB glowing FAB
│               ├── CameraScannerScreen # Live CameraX & Resizable Crop Viewfinder
│               ├── HomeScreen.kt       # Level progression dashboard & banners
│               ├── PracticeScreen.kt   # Available quizzes catalog
│               ├── QuizTakingScreen.kt # Interactive test engine
│               ├── LibraryScreen.kt    # Past session replay list
│               ├── ProfileScreen.kt    # User stats & avatar selector
│               ├── AuthViewModel.kt    # Firebase auth & stats observer
│               ├── ChatViewModel.kt    # Camera capture & AI chat streaming
│               └── theme/              # Color palette, Shapes, & Typography
├── art/                                # Project branding & screenshot assets
├── build.gradle.kts                    # Root build configuration
├── gradle.properties                   # JVM & memory configuration
└── settings.gradle.kts                 # Repositories & plugin declarations
```

---

## 🚀 Installation & Setup

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1+) or newer.
- **JDK**: Version 17 or Version 21.
- **Android Device / Emulator**: Running Android 10.0 (API level 29) or higher with camera support.

### Step-by-Step Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/Study_Mentor_BTEC_BestCode.git
   cd Study_Mentor_BTEC_BestCode
   ```

2. **Configure API Secrets**:
   Create or open the `local.properties` file in the project's root folder and append your AI API Key:
   ```properties
   ## Local Environment Configurations
   sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   
   # AI Service API Key for OpenAI / Proxy endpoint
   AI_API_KEY=sk-your-actual-api-key-here
   ```

3. **Add Firebase Credentials**:
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android App with package name: `com.example.study_mentor`.
   - Download the generated `google-services.json` and place it in the `app/` folder:
     ```text
     Study_Mentor_BTEC_BestCode/app/google-services.json
     ```
   - In Firebase Console, enable **Email/Password Authentication** and **Cloud Firestore Database**.

4. **Build & Run**:
   Compile and deploy directly from Android Studio or via Gradle CLI:
   ```bash
   # Build debug APK
   ./gradlew assembleDebug

   # Install onto connected Android device
   ./gradlew installDebug
   ```

---

## 🔐 Environment & Secrets Configuration

To protect proprietary keys from leaking into version control, Study Mentor employs a multi-tiered security pattern:
- **Git Ignored Keys**: `local.properties` and private credentials are excluded via `.gitignore`.
- **Gradle Secrets Injection**: During the build step, Gradle parses `local.properties` and injects `AI_API_KEY` into `BuildConfig` at compile-time:
  ```kotlin
  buildConfigField("String", "AI_API_KEY", "\"$aiApiKey\"")
  ```
- **Runtime Authorization**: The custom client reads `BuildConfig.AI_API_KEY` directly when constructing the `Authorization: Bearer <TOKEN>` header.

---
</div>

