# Architecture

This project follows a layered Kotlin Multiplatform architecture:

Data → Logic → Brief → UI

Shared logic lives in `commonMain` and feeds both Android and Desktop Compose UIs.