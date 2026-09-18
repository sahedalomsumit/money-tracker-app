# Money Tracker App 💰

> Track money, log income, and see your breakdown and savings grow over time without ads.

A modern, ad-free Android app built with **Kotlin**, **Jetpack Compose**, and **Firebase** (Authentication + Cloud Firestore with offline persistence) for personal salary and income tracking.

---

## Features

- **Google Sign-In**: Seamless authentication with session persistence. Scoped single-user Firestore data (`users/{uid}`).
- **First-Time Onboarding**:
  - Automatically loads your Google name, email, and avatar (read-only).
  - Prompts for mandatory preferences: **Sex**, **Country** (searchable picker), and **Currency** (auto-suggested from country with override support).
  - Automatically seeds default allocation settings and income categories.
- **Dynamic Allocation Engine**:
  - Global percentages: **Saving** (30%), **Investing** (3%), **Donate** (1%), and **Rest** (auto-calculated: `100 - (saving + invest + donate)`).
  - Customizable labels and percentages in Settings with real-time validation (blocks saving if total exceeds 100%).
- **Interactive Monthly Bar Chart**:
  - Full 12-month calendar overview (Jan–Dec).
  - Automatically sums multiple income entries per month into a single bar.
  - Automatically highlights peak earning (**Best Month**) and lowest earning (**Low Month**) with visual badges.
  - Tap any bar to inspect month totals in your selected currency.
- **Income Sources & Inline Creation**:
  - Default main sources: *Job*, *Business*, *Freelancing*, *Tax Return*, *Gift*.
  - Default freelancing sub-sources: *Fiverr*, *Upwork*, *Stripe*, *Direct*, *PayPal*.
  - Full management in Settings (add, rename, delete) plus **inline "+ Add new sub-source"** directly from the Add Entry sheet!
- **Global Currency Formatting**:
  - Every amount displayed (dashboard totals, bar chart, category cards, drilldown breakdowns, entry list) respects your currency symbol and code.
- **Visual Design**:
  - Typography: **Montserrat** applied app-wide.
  - Palette: Dark mode default with sleek near-black background, charcoal card surfaces, and emerald teal accents (`#10B981`); full light mode equivalent.
  - Theme override: System Default / Light / Dark.
  - Bottom navigation with center raised circular "+" Floating Action Button.
- **Support Section**:
  - Direct links to Stripe and bKash for optional donations to support ad-free maintenance.

---

## Remaining Setup Steps (Firebase & Google Sign-In)

The project is pre-configured with `google-services.json` in `app/` connected to Firebase project **`money-tracker-99`**.

To allow Google Sign-In to succeed on your Android device or emulator:

### 1. Generate your debug SHA-1 Fingerprint
Run the following command in PowerShell / Terminal:
```powershell
# In the project root:
.\gradlew.bat signingReport
```
Or using `keytool` on Windows:
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

### 2. Add the SHA-1 to Firebase Console
1. Go to the [Firebase Console](https://console.firebase.google.com/project/money-tracker-99/settings/general).
2. Under **Your apps**, click on **Money Tracker App** (`com.sahed.money_tracker`).
3. Under **SHA certificate fingerprints**, click **Add fingerprint**.
4. Paste your **SHA-1** fingerprint and click **Save**.
5. *(Optional)* Download the updated `google-services.json` and place it in `app/google-services.json` if you'd like the web client ID to automatically refresh.

---

## Project Structure

```
money-tracker-app/
├── app/
│   ├── google-services.json          # Firebase credentials
│   ├── build.gradle.kts              # App-level dependencies & plugins
│   └── src/main/
│       ├── AndroidManifest.xml       # Permissions, Application, Activity
│       ├── res/
│       │   ├── font/                 # Montserrat font files
│       │   ├── drawable/             # App icon foreground & background
│       │   ├── mipmap-anydpi-v26/    # Adaptive icon definitions
│       │   └── values/               # Strings, colors, theme
│       └── java/com/sahed/money_tracker/
│           ├── MoneyTrackerApp.kt    # Offline Firestore persistence initialization
│           ├── MainActivity.kt       # Compose entry point & Edge-to-Edge setup
│           ├── data/
│           │   ├── model/            # UserProfile, AllocationSettings, IncomeSource, IncomeEntry, CountryCurrency
│           │   ├── preferences/      # AppPreferences (DataStore for Theme & Notifications)
│           │   └── repository/       # AuthRepository, ProfileRepository, AllocationRepository, SourceRepository, EntryRepository
│           ├── viewmodel/            # AuthViewModel, OnboardingViewModel, DashboardViewModel, AddEntryViewModel, SettingsViewModel, ManageSourcesViewModel
│           ├── ui/
│           │   ├── components/       # MonthlyBarChart, MoneyBottomBar, AllocationCards, SourceBreakdownCard, EntryItem, Dialogs
│           │   ├── screens/
│           │   │   ├── auth/         # LoginScreen
│           │   │   ├── onboarding/   # OnboardingScreen
│           │   │   ├── dashboard/    # DashboardScreen
│           │   │   ├── entry/        # AddEntryScreen
│           │   │   └── settings/     # SettingsScreen, ManageSourcesScreen
│           │   ├── navigation/       # NavGraph & bottom nav routes
│           │   └── theme/            # Color, Type (Montserrat), Theme
├── build.gradle.kts                  # Root Gradle build script
├── settings.gradle.kts               # Module and repository definitions
└── README.md
```

---

## Building & Running

### Android Studio
1. Open Android Studio.
2. Select **File > Open...** and choose the `money-tracker-app` directory.
3. Wait for Gradle Sync to complete.
4. Select a connected device or emulator and click **Run (Shift + F10)**.

### Command Line
```powershell
# Build Debug APK
.\gradlew.bat assembleDebug

# Install on connected device
.\gradlew.bat installDebug
```

---

## Author & License

Built with ❤️ by [Sahed Alom Sumit](https://sahedalomsumit.com).
© 2026 Sahed Alom Sumit. All rights reserved.