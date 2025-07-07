<h1 align="center">🧘‍♀️ SyncWell – Health & Wellness Tracking App</h1>

<p align="center">
  <img src="app/src/main/res/drawable/app_icon.jpg" width="600" alt="SyncWell Banner" />
</p>

<p align="center">
  <img src="https://img.shields.io/github/stars/your-username/SyncWell?style=social" />
  <img src="https://img.shields.io/github/forks/your-username/SyncWell?style=social" />
  <img src="https://img.shields.io/github/license/your-username/SyncWell" />
  <img src="https://img.shields.io/badge/Kotlin-%E2%9C%85-blue" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material3-blueviolet" />
  <img src="https://img.shields.io/badge/Firebase-CloudSync-yellow" />
  <img src="https://img.shields.io/badge/Room-Local%20DB-green" />
</p>

---

## 📑 Table of Contents

- [✨ Features](#-features)
- [📱 UI Preview](#-ui-preview)
- [📦 Tech Stack](#-tech-stack)
- [🚀 Getting Started](#-getting-started)
- [🤝 Contributing](#-contributing)
- [🛡️ License](#-license)

---

## ✨ Features

### ✅ Health Task Management
- 📅 Task creation with deadlines and importance levels  
- 🔔 Smart reminders with cloud/local sync  
- 🗃️ Room persistence + Firebase sync

```kotlin
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey @DocumentId val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val completed: Boolean = false,
    val deadlineMillis: Long = 0,
    val importance: Int = 0,
    val lastModified: Long = System.currentTimeMillis()
)
