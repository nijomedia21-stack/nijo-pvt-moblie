# Implementation Plan - NIJO PVT Multi-Surface Workspace

A private, secure, multi-surface personal workspace engineered for **one owner across three screen formats** (Phone, TV, and Large Screen/Laptop view), now supercharged with **Internet-wide Cloud Sync** via Firebase Firestore and Google Authentication, while strictly preserving the privacy badge, `</>` branding, and zero-emoji design rules.

---

## User Review & Critical Decisions

The user confirmed the following architectural and UX choices:
1. **Cloud & Sync Model**: Internet-wide cloud sync with accounts and database (backed by the already-provisioned and verified Firebase Firestore and Auth).
2. **Android Client Interface**: Native Jetpack Compose application featuring responsive Phone (4 tabs: Today, Calendar, Files, Study) and specialized TV mode (single tab: Files with D-pad navigation and Send/Receive).
3. **Module Priority**: Full implementation of all four core modules in this cycle:
   - **Tasks** (Notion-lite: All, Today, Upcoming, Done, inline edit, due date highlights, priority)
   - **Calendar** (Mathrubhumi-style Malayalam calendar: Kollam era calculation, Malayalam script for months and nakshatras, English UI chrome)
   - **Files** (Cloud and Wi-Fi transfer hub with TV D-pad remote-optimized Send/Receive layout)
   - **Study** (Syllabus reader, course module harvester, and local revision assistant)

---

## Visual Design & Non-Negotiable Product Rules

- **Brand Logo**: The literal text symbol `</>`. **No emojis anywhere in the UI**.
- **Privacy Assurance**: Every screen displays the explicit **Privacy locked** status indicator badge.
- **Top Bar Controls**: Every view features a **Notifications** dialog trigger and an **Update** action trigger.
- **Color System**:
  - Primary: Deep Tech Blue (`#0054A6`)
  - Accent / Focus: Vivid Sky (`#0284C7`)
  - Surface Dark: Obsidian Slate (`#0B0F19`) / Card Background (`#161E2E`)
  - Surface Light: Crisp Frost (`#F8FAFC`) / Card Background (`#FFFFFF`)
  - Border & Dividers: Subdued Steel (`#334155` dark / `#E2E8F0` light)
- **TV Leanback Experience**:
  - Focus-aware D-pad navigation using `Modifier.focusable()`, large high-contrast focus rings, and 1080p-scaled card tiles.
  - Dedicated TV mode toggle / auto-detection: when `ui=tv` or leanback mode is triggered, the interface locks strictly to the **Files** module (Send / Receive / Shared).

---

## Proposed Changes

### 1. Project Identity & Configuration
- Update `metadata.json` and `res/values/strings.xml` to set `app_name` to **NIJO PVT**.
- Set up TV intent category support and leanback banner resource in `AndroidManifest.xml` while preserving mobile default launcher.

### 2. Domain Models & Firestore Data Layer
- **`TaskItem`**: Title, description, due date timestamp, completed status, tags, priority, syllabus tag.
- **`MalayalamDate`**: Kollam era year calculation (Gregorian − 824 for Chingam 1 ≈ Aug 17 – Dec 31; Gregorian − 825 for Jan 1 – Chingam 1), Malayalam month name (Chingam, Kanni, Thulam, etc.), Nakshatra (Aswathi, Bharani, etc.), and festival names.
- **`FileTransferItem`**: Name, size, MIME type, upload timestamp, device sender tag (Laptop / Phone / TV), cloud storage URL or local share reference.
- **`StudyTopic`**: Subject title, syllabus unit, extracted notes, flashcards, key definitions.
- Update `WorkspaceRepository` to persist and synchronize all four collections (`tasks`, `calendar_events`, `files`, `study_notes`) under the authenticated user's private tenant path (`users/{userId}/...`).

### 3. UI Layer & Adaptive Navigation
- **Navigation Shell (`MainScreen.kt`)**:
  - Detects device mode (`Phone` vs `TV Leanback` vs `Laptop/Expanded`).
  - Top Bar with `</>` Logo, "Privacy locked" badge, Notifications button, and Update status trigger.
  - For **Phone & Tablet**: Bottom navigation bar with 4 tabs: **Today** (Tasks), **Calendar**, **Files**, **Study**.
  - For **TV**: Pure single-view screen displaying the **Files** module with prominent D-pad focus indicators for "Send", "Receive", and "Shared Files".
- **Module 1: Today / Tasks Screen (`TasksScreen.kt`)**:
  - Filter pills: All, Today, Upcoming, Done.
  - Notion-style list and board cards with inline editing, checkbox toggles, due date warning badges.
- **Module 2: Calendar Screen (`MalayalamCalendarScreen.kt`)**:
  - Monthly grid with English numerals and Gregorian dates accompanied by authentic Malayalam Kollam month headers (ചിങ്ങം, കന്നി, etc.) and Nakshatras (അശ്വതി, ഭരണി, etc.).
  - Quick switcher between English Gregorian view and Mathrubhumi Malayalam calendar data.
- **Module 3: Files Screen (`FilesScreen.kt` & `TvFilesScreen.kt`)**:
  - TV Leanback mode: D-pad focusable cards for "Receive File", "Send File", and "Recent Transfers".
  - Phone mode: Quick upload action, recent shares list, device source indicators (Laptop / Phone / TV), and download triggers.
- **Module 4: Study Screen (`StudyScreen.kt`)**:
  - Local syllabus browser, topic review cards, flashcard revision deck, and syllabus passage viewer.

---

## Technical Architecture

```
+-----------------------------------------------------------------------------------+
|                                NIJO PVT Application                               |
|                                                                                   |
|  [</> Logo]  [Privacy locked Badge]          [Notifications]  [Check for Updates]  |
+-----------------------------------------------------------------------------------+
                                         |
                       +-----------------+-----------------+
                       |                                   |
              [Phone / Laptop Mode]                   [TV Mode]
            (Today, Calendar, Files, Study)          (Files Only - D-Pad)
                       |                                   |
                       +-----------------+-----------------+
                                         |
                            [WorkspaceViewModel]
                                         |
                           [WorkspaceRepository]
                                         |
             +---------------------------+---------------------------+
             |                                                       |
   [Firebase Firestore]                                     [Firebase Auth]
 (Tasks, Calendar, Files, Study)                        (Secure Google Sign-In)
```

---

## Verification Plan

### Automated Tests
1. **Robolectric JVM Tests (`app:testDebugUnitTest`)**:
   - `WorkspaceRepositoryRuleTest`: Verify CRUD actions across all four collections with zero security rule violations.
   - `MalayalamCalendarTest`: Verify Kollam year calculation and month conversions across critical dates (e.g., Aug 17 transition).
2. **Applet Compilation**:
   - Run `compile_applet` to ensure error-free Kotlin and Compose compilation.

### Manual & Visual Verification
1. Inspect the top bar on all screens to verify the `</>` logo, the "Privacy locked" badge, and absence of any emoji.
2. Toggle between Phone and TV layouts to verify that TV mode presents only the Files interface with clear focus rings.
3. Test task creation, Malayalam calendar date switching, file transfer item addition, and study deck browsing.
