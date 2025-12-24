# Gulf Coast Hazard Briefs – Kotlin Multiplatform App

A Kotlin Multiplatform (KMP) mobile + desktop + web app that automatically 
collects NOAA / National Weather Service hazard data and generates a clean, 
fast weekly hazard brief for the Texas Gulf Coast Region.

This project is intended for:
- 🌧️ Real-time multi-hazard data feeds
- ⚠️ AI-generated key messages & risk summaries
- 📅 One-tap Weekly Brief optimized for Red Cross workflows
- 🔥 Shared logic across Android, iOS, Desktop, and Web

Built using **Kotlin Multiplatform + Compose Multiplatform**.

---

## 📦 Project Structure

This is a Kotlin Multiplatform project targeting **Android, Web, Desktop (JVM)**.
composeApp/

├─ commonMain/   # Shared Kotlin code for all targets
├─ androidMain/  # Android-specific code
├─ jvmMain/      # Desktop-specific code
├─ wasmJsMain/   # Web (WASM) implementation
└─ jsMain/       # Web (JS fallback)

- `commonMain` contains logic shared by all platforms.
- Platform folders (`androidMain`, `jvmMain`, etc.) hold platform-specific code.
- Compose Multiplatform handles UI across all supported platforms.

---

## 🚀 Build & Run Instructions

### **Android Application**
To run the Android development build:

**From IntelliJ:**  
Use Run → select **composeApp (Android)** → launch on emulator or device.

**From terminal (macOS/Linux):**

./gradlew :composeApp:assembleDebug

**From terminal (Windows):**

.\gradlew.bat :composeApp:assembleDebug

---

### **Desktop (JVM) Application**

**From IntelliJ:**  
Choose the Desktop run configuration (`composeApp Desktop`).

**From terminal (macOS/Linux):**

./gradlew :composeApp:run

**From terminal (Windows):**

.\gradlew.bat :composeApp:run

---

### **Web Application (Compose Web)**

#### Faster Web Target (WASM)
**macOS/Linux:**

./gradlew :composeApp:wasmJsBrowserDevelopmentRun

**Windows:**

.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun

#### JS Legacy Target
**macOS/Linux:**

./gradlew :composeApp:jsBrowserDevelopmentRun

**Windows:**

.\gradlew.bat :composeApp:jsBrowserDevelopmentRun

---

## 🛠 Tech Stack

- **Kotlin Multiplatform**
- **Compose Multiplatform** (Android, Desktop, Web)
- **Coroutines + StateFlow**
- **Gradle Kotlin DSL**
- **Material 3 UI**

---

## 🧭 Roadmap

### Phase 1 — Foundation ✔️
- Initialize KMP project
- Android & Desktop builds working
- Emulator configuration
- GitHub setup

### Phase 2 — Data Pipeline (Next)
- Integrate NOAA/NWS hazard feeds  
- Parse CAP XML + API feeds  
- Build hazard models & risk filters  

### Phase 3 — Weekly Brief UI
- Home screen  
- Risk score cards  
- Auto-generated “Key Messages”  

### Phase 4 — Export & Sharing
- PDF / Image export  
- “One-tap weekly brief” view  

---

## 📢 Support & Community  
Interested in KMP or Compose Multiplatform?

- Join Slack: **#compose-web**
- Report issues on YouTrack

---

## 📝 License  
This project is licensed under the MIT License.  
See the **LICENSE** file for details.
