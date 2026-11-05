# NIJO PVT — 4-in-1 Multi-Platform Ecosystem & PWA

Production-ready, multi-platform personal workspace designed for **one user across three devices**:
- **Laptop (Windows 11)**: Master Admin Host Server & central state manager
- **Mobile Phone (Android)**: Touch-optimized client with 4 bottom tabs (Today · Calendar · Files · Study)
- **Smart TV (Android TV 14 / 1080p)**: 10-foot UI with high contrast cards and D-pad remote navigation (Arrows & OK)

---

## 1. Project Directory Structure

```
.
├── host_server/                      # Node.js + Express + Socket.io Server (Laptop Host)
│   ├── package.json                  # Dependencies: express, socket.io, multer, cors
│   ├── server.js                     # Centralized Hub, WebSocket router, File Streaming
│   ├── uploads/                      # Local storage for high-speed Wi-Fi transfers
│   ├── public/                       # Responsive PWA Frontend
│   │   ├── index.html                # Universal HTML5 Clipboard, 3 Device Views, Tailwind CSS
│   │   └── manifest.json             # PWA Install Manifest
│   └── README.md                     # Host Run & Deployment Guide
│
└── app/                              # Native Android (Kotlin + Jetpack Compose) Application
    ├── src/main/java/com/example/
    │   ├── MainActivity.kt           # Edge-to-edge Compose entry point
    │   ├── ui/
    │   │   ├── AdaptiveWorkspaceApp.kt # Responsive Multi-Surface Layout (Phone, Laptop, TV)
    │   │   └── components/
    │   │       ├── TasksScreen.kt    # Aim 4: Notion-lite Tasks & Kanban
    │   │       ├── MalayalamCalendarScreen.kt # Aim 1: Mathrubhumi Malayalam Panchangam
    │   │       ├── FilesScreen.kt    # Aim 2: High-speed Wi-Fi file hub & clipboard
    │   │       ├── TvFilesScreen.kt  # TV 10-foot D-pad remote layout (Files only)
    │   │       ├── StudyScreen.kt    # Aim 3: Syllabus Harvester & Tuition Teacher
    │   │       └── CustomNamedAiWidget.kt # Module 4: Custom-Named AI Assistant
    │   └── util/
    │       └── MalayalamCalendarHelper.kt # Kollam Era 1202 & Nakshatra Calculations
    └── src/test/java/com/example/    # Local JVM Robolectric tests
```

---

## 2. Step-by-Step Instructions: Running on Laptop Host

### Step 1: Install Dependencies
Open PowerShell or Command Prompt on your Windows 11 laptop:
```bash
cd host_server
npm install
```

### Step 2: Start the Host Server
```bash
npm start
```
The console will display:
```
=======================================================
   NIJO PVT - Cross-Platform Host Server & PWA Hub    
=======================================================
[*] Laptop Host Server active on Port 4000
[*] Localhost access:   http://localhost:4000
[*] LAN / Wi-Fi access: http://192.168.1.X:4000
[*] Shared Files folder: C:\Users\nijoj\PVT_Shared_Files
[*] Open URL on Phone or Smart TV to pair instantly!
=======================================================
```

### Step 3: Windows Firewall Setup (One-time)
If your phone cannot reach your laptop on Wi-Fi, run PowerShell as Administrator:
```powershell
netsh advfirewall firewall add rule name="NIJO PVT Hub 4000" dir=in action=allow protocol=TCP localport=4000
```

---

## 3. Connecting Mobile Phone & Smart TV

### Mobile Phone (Android 7-17 & OriginOS)
1. Ensure your phone is connected to the same home Wi-Fi network as the laptop.
2. Open Chrome or your default browser and navigate to:
   `http://<laptop-ip>:3000` (e.g. `http://192.168.1.15:3000`).
3. Tap the browser menu and select **"Add to Home Screen"** or **"Install app"** to install as a full-screen PWA.
4. The interface auto-activates **Mobile View** with touch-friendly 44px+ targets and bottom navigation tabs.

### Smart TV (42" Android TV 14)
1. Open the TV web browser (or sideloaded PWA client).
2. Navigate to `http://<laptop-ip>:3000?ui=tv` or click the **TV** mode button in the header.
3. The interface switches to the **10-foot UI**:
   - High contrast cards with 4px neon focus rings.
   - Fully controllable using the TV remote D-Pad (`Arrow Up`, `Down`, `Left`, `Right`) and `OK` button.
   - Pinned TV file transfers and live stream items are prominently displayed.

---

## 4. Key Feature Walkthrough

### Module 1: Universal Copy-Paste & Xender-Style File Sharing
- **Universal Clipboard**: Press `Ctrl+C` and `Ctrl+V` on your laptop, or tap "Paste & Receive" on your phone or TV. Text, links, or screenshots are instantly synchronized over WebSockets.
- **Targeted Routing**: Use the dropdown to choose `Send to All`, `Send to TV`, `Send to Mobile`, or `Send to Laptop`.
- **Drag & Drop**: Drop files into the dashed zone; files are hashed (SHA-256) and available immediately for LAN download without leaving the local network.

### Module 2: Notion-Style Tasks & Kanban
- Move tasks across `To-Do`, `In Progress`, and `Completed`.
- Select `Target Device: Pin to Smart TV` to project daily study goals or alerts directly onto the TV home screen.

### Module 3: Mathrubhumi Malayalam Calendar
- Computes **Kollam Era (Kollavarsham 1202)** accurately (transitioning at Chingam 1).
- Authentic Malayalam script for months (ചിങ്ങം, കന്നി, തുലാം, etc.) and 27 Nakshatrams (അശ്വതി, ഭരണി, തിരുവോണം, etc.).
- Includes daily Rahu Kalam times and Kerala festivals (വിഷു, ഓണം, കേരളപ്പിറവി, etc.).

### Module 4: Custom-Named AI Assistant
- Floating widget accessible from every screen.
- Click the name (default: "Nijo Copilot") to change it dynamically (e.g., to "Jarvis" or "Athena"). The name syncs across all connected devices in real time.
- Context-aware voice/text commands:
  - *"Paste last copied link to the TV"* &rarr; Routes the latest clipboard URL directly to the Smart TV.
  - *"What is today in Malayalam calendar?"* &rarr; Returns current Malayalam date, star, and festival.
  - *"Add task: Study AI Unit 1 tonight"* &rarr; Adds and syncs a new task across all devices.
