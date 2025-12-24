# Gulf Coast Hazard Briefs — Kotlin Multiplatform App

A Kotlin Multiplatform (KMP) Android + Desktop application that automatically
collects NOAA / National Weather Service (NWS) weather data and generates a clear,
weekly multi-hazard briefing for the Texas Gulf Coast Region.

This project focuses on turning technical weather products into
**decision-ready summaries** for planners, volunteers, and community responders.

Built using **Kotlin Multiplatform + Compose Multiplatform**.

---

## 🌦️ What This Project Does

The Texas Gulf Coast regularly experiences storms, heavy rain, flooding, and
temperature extremes. While NOAA and the National Weather Service provide
high-quality data, that information is spread across multiple technical products
and can be difficult to interpret quickly.

This app:
- Fetches real NWS forecast and alert data
- Applies rule-based hazard detection and risk scoring
- Generates human-readable **Key Messages**
- Organizes content into a structured **weekly hazard brief**
- Runs on **Android and Desktop** using shared Kotlin logic

The goal is not raw data visualization, but **clear, actionable summaries**.

---

## 🧠 Key Features

- **Weekly Overview (Page 1)**  
  Key messages, temperature trends, precipitation chances, and a 7-day hazard
  timeline comparing North vs South Gulf Coast areas.

- **Hazard Spotlight Pages (Pages 2–3)**  
  Dynamic hazard pages that activate when signals are detected, including:
    - Headline summary
    - What / Where / When / Impacts
    - Risk level (Low / Medium / High)
    - Recommended actions

- **Shared Hazard Engine**  
  All data parsing, hazard rules, and briefing logic live in shared Kotlin code
  and are reused across platforms.

---

## 🧩 Project Architecture
NWS APIs
→ DTOs
→ Domain Models
→ Hazard Rules & Risk Scoring
→ BriefBuilder
→ BriefPage (Page 1–3+)
→ Android & Desktop UI
- Shared logic lives in `commonMain`
- UI layers contain no data-fetching or hazard logic
- Pages are represented as sealed `BriefPage` classes that generate plain text,
  making the logic reusable beyond the UI

---

## 🛠 Tech Stack & Platforms

### Primary Platforms (Judged Demos)

- **Android (primary demo)**
    - Jetpack Compose UI
    - Uses the shared hazard engine and data layer

- **Desktop (JVM) (secondary demo)**
    - Compose Multiplatform Desktop
    - Reuses the same shared logic as Android

### Core Technologies

- **Language:** Kotlin (Kotlin Multiplatform)
- **UI:** Jetpack Compose + Compose Multiplatform
- **Networking:** Ktor Client (multiplatform)
- **Async:** kotlinx.coroutines
- **Serialization:** kotlinx.serialization (JSON)
- **Date / Time:** kotlinx-datetime
- **Build:** Gradle Kotlin DSL

### Data Sources

- NOAA / National Weather Service APIs
    - Gridpoint Forecast
    - Forecast periods
    - NWS Alerts

---

## 🚀 Build & Run Instructions

### Prerequisites
- IntelliJ IDEA
- JDK 17
- Android SDK (for Android build)

### Android Application

**From IntelliJ:**  
Run → select **composeApp (Android)** → launch on emulator or device.

**From terminal (macOS/Linux):**
```bash
./gradlew :composeApp:assembleDebug
Desktop (JVM) Application

From IntelliJ:
Run the Desktop configuration.

From terminal (macOS/Linux):
./gradlew :composeApp:run

⸻

🧪 How to Try It
	1.	Launch the app
	2.	Tap Refresh
	3.	Review:
	•	Weekly Overview (Page 1)
	•	Active Hazard Page (e.g., Convective Weather)
	•	Weekly Brief navigation

⸻

🧭 Roadmap (High-Level)
	•	Phase 1 — Foundation ✔️
KMP setup, Android + Desktop builds, shared architecture
	•	Phase 2 — Hazard Logic & Weekly Brief ✔️
NWS data ingestion, hazard rules, multi-page briefing
	•	Phase 3 — Polish & Extensions (Future)
	•	Additional hazard types (heat, flooding, etc.)
	•	Exportable brief formats (PDF / text)
	•	Expanded regional support

⸻

📝 License

This project is licensed under the MIT License.
See the LICENSE file for details.

⸻

Coding Style & Conventions

This project follows the official
Kotlin Coding Conventions￼,
including:
	•	Clear package structure (data, domain, logic, brief)
	•	data classes for models and sealed classes for page types
	•	Explicit null-safety and coroutine-based async design
	•	Shared logic written to be reusable across platforms

The goal is to keep the hazard engine readable, testable, and idiomatic for
Kotlin Multiplatform.
