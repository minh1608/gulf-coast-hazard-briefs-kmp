🌧️ Gulf Coast Hazard Briefs

A Kotlin Multiplatform Weekly Weather Briefing App for Humanitarian Use

Gulf Coast Hazard Briefs is a Kotlin Multiplatform (KMP) application that automatically collects National Weather Service (NWS) data and generates a clear, multi-page weekly hazard brief for the Texas Gulf Coast Region.

The app is designed for humanitarian workflows, such as emergency planning, volunteer coordination, and situational awareness — where fast, readable summaries matter more than raw meteorological charts.

Built once, shared everywhere: Android + Desktop (JVM), with an experimental Web preview.

⸻

✨ What This Project Does
•	Fetches real NWS forecast and alert data
•	Applies rule-based hazard logic in shared Kotlin code
•	Automatically generates a multi-page weekly brief
•	Presents the same brief consistently on Android and Desktop
•	Optimized for quick review, not expert interpretation

Current Pages
•	Page 1 — Weekly Overview
Forecast timeline, temperature ranges, precipitation chances, and confidence
•	Page 2 — Active Hazard Spotlight
What / Where / When / Impacts + recommended actions
•	Page 3 — Flooding & Rainfall Context
External map previews (WPC, radar, graphical forecast) with safe fallbacks

⸻

🧠 Why It Matters

During severe or complex weather weeks, responders often need to answer:
•	What’s the main risk this week?
•	Where should we pay attention?
•	What actions should we take now?

This project turns raw weather data into human-readable briefings, reducing cognitive load and making it easier for non-meteorologists to act quickly and confidently.

⸻

## 📸 Screenshots

### Android – Weekly Overview
![Weekly Overview](screenshots/page1_overview_android.png)

### Android – Active Hazard Page (Convective Weather)
![Convective Weather](screenshots/page2_convective_android.png)

### Desktop – Same Shared Logic
![Desktop Convective](screenshots/page2_convective_desktop.png)

⸻

🧩 Project Architecture

This is a Kotlin Multiplatform project with shared logic and platform-specific UIs.
composeApp/
├─ commonMain/     # Shared hazard logic, models, brief builder
├─ androidMain/    # Android-specific UI & integrations
├─ jvmMain/        # Desktop (JVM) UI
├─ wasmJsMain/     # Experimental Web (WASM)
└─ jsMain/         # JS fallback

•	commonMain contains the hazard engine, rules, and page models
•	All platforms use the same BriefBuilder
•	Compose Multiplatform renders UI consistently across targets

⸻

🛠 Tech Stack
•	Kotlin Multiplatform
•	Compose Multiplatform (Android + Desktop)
•	Ktor Client (multiplatform networking)
•	kotlinx.serialization (JSON parsing)
•	kotlinx.coroutines (async logic)
•	Gradle Kotlin DSL
•	Material 3 UI

⸻

🚀 Build & Run Instructions

Android Application

From IntelliJ:
Run → select composeApp (Android) → launch on emulator or device.

From terminal (macOS / Linux):
./gradlew :composeApp:assembleDebug

From terminal (Windows):
.\gradlew.bat :composeApp:assembleDebug


⸻

Desktop (JVM) Application

From IntelliJ:
Run the Desktop configuration.

From terminal (macOS / Linux):
./gradlew :composeApp:run

From terminal (Windows):
.\gradlew.bat :composeApp:run


⸻

🧪 How to Try It
1.	Launch the app
2.	Tap Refresh
3.	Review:
•	Weekly Overview (Page 1)
•	Active Hazard Page (e.g., Convective Weather)
•	Weekly Brief navigation

⸻

🗺 Roadmap (High-Level)

Phase 1 — Foundation ✔️
•	Kotlin Multiplatform setup
•	Android + Desktop builds
•	Shared architecture

Phase 2 — Hazard Logic & Weekly Brief ✔️
•	NWS data ingestion
•	Hazard rules & scoring
•	Multi-page briefing UI

Phase 3 — Polish & Extensions (Future)
•	Additional hazard types (heat, flooding, etc.)
•	Exportable brief formats (PDF / text)
•	Expanded regional support

⸻

📚 Data Sources
•	National Weather Service (NWS)
•	Forecast API
•	Alerts API
•	Optional external references:
•	WPC rainfall outlooks
•	NWS radar and graphical forecasts

(See docs/DATA_SOURCES.md for details.)

⸻

📝 License

This project is licensed under the MIT License.
See the LICENSE file for details.

⸻

🧩 Coding Style & Conventions

This project follows the official
Kotlin Coding Conventions￼, including:
•	Clear package structure (data, domain, logic, brief)
•	Data classes for models and sealed classes for page types
•	Explicit null-safety and coroutine-based async design
•	Shared logic written once and reused across platforms

The goal is to keep the hazard engine readable, testable, and idiomatic for Kotlin Multiplatform.

⸻

📄 Essay

A short project essay is available at:
essay/ESSAY.md

It covers:
•	Motivation and humanitarian context
•	Technical decisions
•	Impact and future directions
