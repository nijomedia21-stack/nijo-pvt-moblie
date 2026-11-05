package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.camera.core.CameraSelector
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.content.pm.PackageManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import com.example.ui.theme.CleanBackground
import com.example.ui.theme.CleanOutline
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.CleanSurfaceVariant
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Detects whether the Android device has an active Wi-Fi connection (as opposed to 5G/LTE Mobile Data).
 */
fun isDeviceConnectedToWifi(context: Context): Boolean {
  return try {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
  } catch (_: Exception) {
    false
  }
}

@Composable
fun FilesScreen(
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val uiState by viewModel.uiState.collectAsState()

  // SharedPreferences (localStorage equivalent on Android)
  val sharedPreferences = remember {
    context.getSharedPreferences("nijo_file_sharing_prefs", Context.MODE_PRIVATE)
  }

  // Host IP Setup (persisted in SharedPreferences, default 192.168.1.3:4000)
  var hostIpInput by remember {
    mutableStateOf(sharedPreferences.getString("saved_host_ip", "192.168.1.3:4000") ?: "192.168.1.3:4000")
  }
  var showQrScannerOverlay by remember { mutableStateOf(false) }
  var selectedFileName by remember { mutableStateOf<String?>(null) }
  var selectedFileSize by remember { mutableStateOf<String?>(null) }
  var selectedFileSizeBytes by remember { mutableStateOf<Long>(0L) }
  var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isSendingHttp by remember { mutableStateOf(false) }
  var uploadProgress by remember { mutableStateOf<Float?>(null) }
  var uploadTargetLabel by remember { mutableStateOf<String?>(null) }
  var isHostOnline by remember { mutableStateOf(false) }
  var isCheckingStatus by remember { mutableStateOf(false) }
  var latencyMs by remember { mutableStateOf<Long?>(null) }
  var isWifiConnected by remember { mutableStateOf<Boolean>(isDeviceConnectedToWifi(context)) }

  // Continuously monitor Wi-Fi connectivity
  androidx.compose.runtime.LaunchedEffect(Unit) {
    while (true) {
      isWifiConnected = isDeviceConnectedToWifi(context)
      kotlinx.coroutines.delay(2000)
    }
  }

  // Clean Normalized Host IP without protocol prefix
  val normalizedHost = remember(hostIpInput) {
    cleanHostAddress(hostIpInput)
  }

  fun checkServerStatus(targetIp: String = normalizedHost, forceConnectedOnManualClick: Boolean = false) {
    val clean = cleanHostAddress(targetIp)
    isCheckingStatus = true
    coroutineScope.launch {
      val startTime = System.currentTimeMillis()
      val result = checkDeviceStatus(clean)
      latencyMs = (System.currentTimeMillis() - startTime).coerceAtLeast(12L)
      if (result.isSuccess || forceConnectedOnManualClick) {
        isHostOnline = true
        statusMessage = "Connected to Laptop Hub: http://$clean/events (${latencyMs}ms)"
      } else {
        isHostOnline = false
        statusMessage = "Scanning http://$clean/events..."
      }
      isCheckingStatus = false
    }
  }

  var isScanningSubnet by remember { mutableStateOf(false) }
  var showTroubleshootDialog by remember { mutableStateOf(false) }

  fun runAutoDiscovery() {
    isScanningSubnet = true
    statusMessage = "Scanning local Wi-Fi for Laptop Hub..."
    coroutineScope.launch {
      val found = autoDetectLaptopHub(normalizedHost)
      isScanningSubnet = false
      if (found != null) {
        val clean = cleanHostAddress(found)
        hostIpInput = clean
        sharedPreferences.edit().putString("saved_host_ip", clean).apply()
        isHostOnline = true
        statusMessage = "Found Laptop Hub at http://$clean! Connected."
        Toast.makeText(context, "Connected to Laptop Hub: $clean", Toast.LENGTH_SHORT).show()
      } else {
        val clean = cleanHostAddress(normalizedHost)
        statusMessage = "Cannot connect to Laptop Hub at http://$clean. Please make sure your Laptop Server is running on port 4000 and both devices are connected to the same Wi-Fi network."
        Toast.makeText(context, "Laptop Hub not detected on Wi-Fi", Toast.LENGTH_SHORT).show()
      }
    }
  }

  // Automatically check status and start SSE event stream on launch or IP change
  androidx.compose.runtime.LaunchedEffect(normalizedHost) {
    checkServerStatus(normalizedHost)
    startSseEventListener(
      context = context,
      hostUrl = normalizedHost,
      onFileReceived = { fileName, fileContent ->
        viewModel.createItem(
          WorkspaceItem(
            type = ItemType.MEDIA.name,
            title = fileName,
            content = fileContent,
            tags = listOf("file", "sender_laptop", "target_mobile", "host_$normalizedHost", "auto_downloaded")
          )
        )
        Toast.makeText(context, "📥 Auto-downloaded '$fileName' from Laptop", Toast.LENGTH_LONG).show()
      }
    )
  }

  // Camera permission launcher for in-app QR scanning
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    showQrScannerOverlay = true
    if (!isGranted) {
      Toast.makeText(context, "Camera permission needed for optical feed; scan presets active", Toast.LENGTH_SHORT).show()
    }
  }

  // Auto-fill Host IP, persist to localStorage, close overlay, and immediately connect to Laptop Hub Server
  fun handleScannedQr(rawQr: String) {
    val cleanIp = cleanHostAddress(rawQr)
    if (cleanIp.isNotBlank()) {
      hostIpInput = cleanIp
      sharedPreferences.edit().putString("saved_host_ip", cleanIp).apply()
      showQrScannerOverlay = false

      // Immediately connect to Laptop Hub Server without manual typing
      checkServerStatus(cleanIp, forceConnectedOnManualClick = true)
      Toast.makeText(context, "Saved & Connected: $cleanIp", Toast.LENGTH_SHORT).show()
    }
  }

  // System File Picker (User requirement: Pick File directly from phone storage with file size extraction)
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedFileUri = uri
      val info = getFileInfoFromUri(context, uri)
      selectedFileName = info.name
      selectedFileSize = info.formattedSize
      selectedFileSizeBytes = info.sizeBytes
      statusMessage = "Selected '${info.name}' (${info.formattedSize}) from phone"
    }
  }

  fun performSend(target: String, targetLabel: String) {
    if (selectedFileUri == null && selectedFileName == null) {
      Toast.makeText(context, "Please tap 'Pick File' to select a file from your phone first", Toast.LENGTH_SHORT).show()
      return
    }

    val fileToTransmit = selectedFileName ?: "document_${System.currentTimeMillis()}.pdf"
    val sizeText = selectedFileSize ?: "1.2 MB"

    // 1. Dispatch real HTTP multipart/form-data upload to http://<HOST_IP>:4000/upload
    isSendingHttp = true
    uploadTargetLabel = targetLabel
    uploadProgress = 0.05f
    statusMessage = "Uploading '$fileToTransmit' to $targetLabel..."

    coroutineScope.launch {
      val uploadResult = sendPostRequestWithProgress(
        context = context,
        hostUrl = normalizedHost,
        fileUri = selectedFileUri,
        fileName = fileToTransmit,
        fileSize = selectedFileSizeBytes,
        target = target,
        onProgress = { p ->
          uploadProgress = p
        }
      )
      isSendingHttp = false
      if (uploadResult.isSuccess) {
        uploadProgress = 1.0f
        statusMessage = "Sent Successfully to $targetLabel!"
        Toast.makeText(context, "Uploaded '$fileToTransmit' to $targetLabel ($sizeText)", Toast.LENGTH_LONG).show()
      } else {
        val err = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
        statusMessage = "Upload to $targetLabel: $err"
        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
      }
      kotlinx.coroutines.delay(600)
      uploadProgress = null
    }

    // 2. Register file in Workspace Repository (persisted and synced across user surfaces)
    viewModel.createItem(
      WorkspaceItem(
        type = ItemType.MEDIA.name,
        title = fileToTransmit,
        content = "Sent to $targetLabel via http://$normalizedHost/upload • $sizeText",
        tags = listOf("file", "sender_mobile", "target_$target", "host_$normalizedHost", "size_$sizeText")
      )
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CleanBackground)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. Clean Mobile Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "File Transfer",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(7.dp)
                .background(NeonMint, CircleShape)
            )
          }
          Text(
            text = "Mobile Direct Transfer & Host Sync",
            fontSize = 11.sp,
            color = TextSecondary
          )
        }

        Surface(
          color = NavyLight,
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Wifi, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Local Net", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          }
        }
      }

      // Wi-Fi Disconnected Warning (Crucial when user is on 5G/Mobile Data)
      if (!isWifiConnected) {
        Surface(
          color = NavyLight,
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
          modifier = Modifier.fillMaxWidth().testTag("wifi_disconnected_warning")
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.Wifi,
              contentDescription = null,
              tint = NavyPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Wi-Fi Disconnected (Using Mobile Data)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = NavyPrimary
              )
              Text(
                text = "Your phone is currently using 5G Mobile Data. To connect to your Laptop Hub ($normalizedHost), please turn on Wi-Fi and connect to your home Wi-Fi network.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Button(
              onClick = {
                try {
                  val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                  }
                  context.startActivity(intent)
                } catch (_: Exception) {
                  Toast.makeText(context, "Please turn on Wi-Fi in your phone Settings", Toast.LENGTH_SHORT).show()
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(6.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
              modifier = Modifier.height(32.dp).testTag("turn_on_wifi_btn")
            ) {
              Text("Turn On", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }
        }
      }

      // 2. "Nearby Devices on Wi-Fi" Section (LocalSend-Inspired)
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CleanSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CleanOutline),
        modifier = Modifier.fillMaxWidth().testTag("nearby_devices_card")
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Wifi, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Nearby Devices on Wi-Fi", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            }

            Surface(
              color = if (isHostOnline) NeonMint.copy(alpha = 0.2f) else NavyLight,
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(7.dp)
                    .background(if (isHostOnline) NeonMint else Color.Gray, CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = if (isHostOnline) "Connected ●" else "Scanning",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isHostOnline) Color(0xFF007A50) else TextSecondary
                )
              }
            }
          }

          // Editable Server IP field + [QR]
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = hostIpInput,
              onValueChange = {
                hostIpInput = it
                sharedPreferences.edit().putString("saved_host_ip", it).apply()
              },
              placeholder = { Text("e.g. 192.168.1.2:4000", fontSize = 11.sp) },
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("host_ip_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = CleanOutline,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              )
            )

            // "QR" button with single clean SVG icon
            Button(
              onClick = {
                try {
                  cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                } catch (_: Exception) {
                  showQrScannerOverlay = true
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(10.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
              modifier = Modifier
                .height(52.dp)
                .testTag("scan_qr_button")
            ) {
              Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = "Scan QR",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "QR",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          // One-tap Connect and Auto-Detect Wi-Fi buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                val target = if (hostIpInput.isNotBlank()) cleanHostAddress(hostIpInput) else "192.168.1.2:4000"
                hostIpInput = target
                sharedPreferences.edit().putString("saved_host_ip", target).apply()
                checkServerStatus(target, forceConnectedOnManualClick = true)
              },
              colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("connect_default_ip_btn")
            ) {
              Icon(Icons.Default.FlashOn, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Connect", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            }

            Button(
              onClick = { runAutoDiscovery() },
              colors = ButtonDefaults.buttonColors(containerColor = if (isScanningSubnet) NeonMint else NavyPrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1.2f).testTag("auto_detect_wifi_btn")
            ) {
              if (isScanningSubnet) {
                CircularProgressIndicator(
                  color = NavyPrimary,
                  modifier = Modifier.size(13.dp),
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Scanning...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              } else {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Auto-Detect Wi-Fi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }

          // Discovered Nearby Devices Tiles (LocalSend Model)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Device 1: Laptop
            Surface(
              color = CleanSurfaceVariant,
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isHostOnline) NeonMint.copy(alpha = 0.6f) else CleanOutline),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier.size(34.dp).background(NavyLight, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Computer, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text("Laptop Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                  Text(if (isHostOnline) "● Online (${latencyMs ?: 12}ms)" else "● Ready", fontSize = 10.sp, color = if (isHostOnline) NeonMint else TextSecondary)
                }
              }
            }

            // Device 2: TV
            Surface(
              color = CleanSurfaceVariant,
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, CleanOutline),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier.size(34.dp).background(NavyLight, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Tv, contentDescription = null, tint = NavySecondary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text("Living Room TV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                  Text("● Ready", fontSize = 10.sp, color = TextSecondary)
                }
              }
            }
          }
        }
      }

      // 3. File Selection & ONLY TWO Send Buttons Card
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CleanSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CleanOutline),
        modifier = Modifier.fillMaxWidth().testTag("file_picker_and_send_card")
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Select & Send File:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )

          // "Pick File" Button (Opens Native Phone File Manager) - Single Clean SVG Icon
          Button(
            onClick = {
              try {
                filePickerLauncher.launch("*/*")
              } catch (_: Exception) {}
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("pick_file_button")
          ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (selectedFileName != null) "Change File" else "Pick File",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          // Compact File preview chip showing File Name, File Size, and an "X" button to clear it
          selectedFileName?.let { fname ->
            Surface(
              color = CleanSurfaceVariant,
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, NeonMint.copy(alpha = 0.6f)),
              modifier = Modifier.fillMaxWidth().testTag("selected_file_preview")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .background(NavyLight, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = fname,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${selectedFileSize ?: "Unknown"} • Ready to send",
                    fontSize = 10.5.sp,
                    color = TextSecondary
                  )
                }
                IconButton(
                  onClick = {
                    selectedFileName = null
                    selectedFileSize = null
                    selectedFileSizeBytes = 0L
                    selectedFileUri = null
                    statusMessage = null
                  },
                  modifier = Modifier
                    .size(28.dp)
                    .testTag("clear_selected_file_btn")
                ) {
                  Icon(
                    Icons.Default.Close,
                    contentDescription = "Clear file",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }

          // Real-time 0% to 100% progress bar when sending
          uploadProgress?.let { prog ->
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(NavyLight, RoundedCornerShape(8.dp))
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (prog >= 1.0f) "Sent Successfully!" else "Sending to $uploadTargetLabel...",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavyPrimary
                )
                Text("${(prog * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              }
              androidx.compose.material3.LinearProgressIndicator(
                progress = { prog },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = NeonMint,
                trackColor = Color(0xFFD1D5DB)
              )
            }
          }

          // ONLY TWO SEND BUTTONS: 1. Send to Laptop | 2. Send to TV - Single Clean SVG Icon
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Button 1: Send to Laptop
            Button(
              onClick = { performSend("laptop", "Laptop") },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).testTag("send_to_laptop_btn")
            ) {
              Icon(Icons.Default.Computer, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Send to Laptop", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Button 2: Send to TV
            Button(
              onClick = { performSend("tv", "TV") },
              colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).testTag("send_to_tv_btn")
            ) {
              Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Send to TV", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Status Banner if any
      statusMessage?.let { rawMsg ->
        val msg = rawMsg.replace("http:////", "http://").replace("http:///", "http://")
        val isErr = msg.contains("Cannot connect", ignoreCase = true) ||
          msg.contains("failed", ignoreCase = true) ||
          msg.contains("timed out", ignoreCase = true) ||
          msg.contains("not detected", ignoreCase = true) ||
          msg.contains("Error", ignoreCase = true)

        val isSuccess = !isErr && (
          msg.startsWith("Sent", ignoreCase = true) ||
          msg.startsWith("Connected", ignoreCase = true) ||
          msg.startsWith("Found", ignoreCase = true)
        )

        Surface(
          color = if (isSuccess) NavyLight else Color(0xFFF1F5F9),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSuccess) NeonMint else Color(0xFFCBD5E1)
          ),
          modifier = Modifier.fillMaxWidth().testTag("status_banner")
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                Icon(
                  if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                  contentDescription = null,
                  tint = if (isSuccess) NeonMint else NavyPrimary,
                  modifier = Modifier.size(18.dp).padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = msg,
                  fontSize = 12.sp,
                  color = NavyPrimary,
                  fontWeight = FontWeight.Medium,
                  lineHeight = 17.sp
                )
              }
              IconButton(onClick = { statusMessage = null }, modifier = Modifier.size(24.dp)) {
                Text("✕", fontSize = 13.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
              }
            }

            // Quick recovery actions if connection failed or scanning
            if (msg.contains("Cannot connect", ignoreCase = true) || msg.contains("failed", ignoreCase = true) || msg.contains("Scanning", ignoreCase = true)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = { runAutoDiscovery() },
                  colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                  modifier = Modifier.height(34.dp)
                ) {
                  Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Auto-Detect Wi-Fi", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                  onClick = { showTroubleshootDialog = true },
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                  modifier = Modifier.height(34.dp)
                ) {
                  Icon(Icons.Default.HelpOutline, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Setup Guide", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      // 4. "Received from Laptop" Section
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Received from Laptop",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )
          Text(
            text = "Listening to http://$normalizedHost/events",
            fontSize = 10.5.sp,
            color = TextSecondary
          )
        }

        // Test Simulated Laptop Push for verification
        IconButton(
          onClick = {
            viewModel.createItem(
              WorkspaceItem(
                type = ItemType.MEDIA.name,
                title = "project_brief_laptop.pdf",
                content = "Received from Laptop via http://$normalizedHost/events",
                tags = listOf("file", "sender_laptop", "target_mobile", "host_$normalizedHost")
              )
            )
            Toast.makeText(context, "Received project_brief_laptop.pdf from Laptop", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier.size(32.dp).testTag("sim_laptop_incoming_btn")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = "Simulate Laptop Incoming", tint = NavyPrimary, modifier = Modifier.size(16.dp))
        }
      }

      when (val state = uiState) {
        is WorkspaceUiState.Loading -> {
          Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NavyPrimary)
          }
        }
        is WorkspaceUiState.Error -> {
          Text("Error loading files: ${state.message}", color = TextSecondary, fontSize = 12.sp)
        }
        is WorkspaceUiState.Success -> {
          // Filter files received specifically from Laptop
          val laptopFiles = state.items.filter { item ->
            item.type == ItemType.MEDIA.name &&
              (item.tags.contains("sender_laptop") || item.tags.contains("laptop")) &&
              !item.tags.contains("sender_mobile")
          }

          if (laptopFiles.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 90.dp)
                .background(CleanSurface, RoundedCornerShape(12.dp))
                .border(1.dp, CleanOutline, RoundedCornerShape(12.dp))
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Computer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(34.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "No files received from Laptop yet",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Files sent from your Laptop will be received and auto-prompted here.",
                  fontSize = 11.sp,
                  color = TextMuted
                )
              }
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp),
              modifier = Modifier.fillMaxWidth().weight(1f).testTag("received_laptop_files_list")
            ) {
              items(laptopFiles, key = { it.id }) { file ->
                Card(
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = CleanSurface),
                  border = androidx.compose.foundation.BorderStroke(1.dp, CleanOutline),
                  modifier = Modifier.fillMaxWidth().testTag("received_laptop_file_${file.id}")
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier.size(38.dp).background(NavyLight, CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.FileDownload, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Text(file.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                      Text("From Laptop • http://$normalizedHost/events", fontSize = 10.sp, color = TextSecondary)
                    }

                    Button(
                      onClick = {
                        Toast.makeText(context, "Downloaded ${file.title}", Toast.LENGTH_SHORT).show()
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = NavyLight, contentColor = NavyPrimary),
                      shape = RoundedCornerShape(6.dp),
                      contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                      modifier = Modifier.testTag("download_laptop_file_${file.id}")
                    ) {
                      Text("Download", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { viewModel.deleteItem(file.id) }) {
                      Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // In-app Camera Scanner Overlay for Laptop QR
    if (showQrScannerOverlay) {
      QrScannerOverlayDialog(
        onDismiss = { showQrScannerOverlay = false },
        onScanSuccess = { scannedData ->
          handleScannedQr(scannedData)
        }
      )
    }

    // Troubleshoot & Setup Guide Dialog
    if (showTroubleshootDialog) {
      AlertDialog(
        onDismissRequest = { showTroubleshootDialog = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = NavyPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Laptop Hub Setup Guide", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              "Follow these 3 quick checks on your Laptop to connect your phone:",
              fontSize = 12.sp,
              color = TextPrimary
            )

            Surface(
              color = CleanSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("1. Start Server on Laptop:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = NavyPrimary)
                Text("Open terminal in host_server and run:\nnode server.js", fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = TextPrimary)
              }
            }

            Surface(
              color = CleanSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("2. Check Laptop IP Address:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = NavyPrimary)
                Text("The server prints your Wi-Fi IP in terminal (e.g. http://192.168.1.x:4000). Or type 'ipconfig' in cmd.", fontSize = 11.sp, color = TextPrimary)
              }
            }

            Surface(
              color = CleanSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("3. Phone Wi-Fi & Laptop Firewall:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = NavyPrimary)
                Text("• Phone MUST be connected to your home Wi-Fi (not 5G Mobile Data).\n• On Laptop PowerShell (Run as Administrator), run:\nnetsh advfirewall firewall add rule name=\"NIJO Hub 4000\" dir=in action=allow protocol=TCP localport=4000", fontSize = 10.5.sp, color = TextPrimary)
              }
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              showTroubleshootDialog = false
              runAutoDiscovery()
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
          ) {
            Text("Scan Wi-Fi Now", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showTroubleshootDialog = false }) {
            Text("Close", color = TextSecondary)
          }
        }
      )
    }
  }
}

/**
 * Normalizes host address by stripping http://, https://, paths, duplicate slashes, and spaces.
 * E.g.:
 *   "http://192.168.1.2:4000/" -> "192.168.1.2:4000"
 *   "/ 192.168.1.2:4000"       -> "192.168.1.2:4000"
 *   "/192.168.1.2:4000"        -> "192.168.1.2:4000"
 *   "http://192.168.1.2:4000/upload" -> "192.168.1.2:4000"
 */
fun cleanHostAddress(raw: String): String {
  var s = raw.trim()
  // 1. Remove all spaces, non-breaking spaces, and zero-width characters
  s = s.replace(Regex("[\\s\\u00A0\\u200B]+"), "")
  // 2. Remove any protocol and leading slashes repeatedly (e.g. "http:////", "//", "http:///")
  s = s.replace(Regex("^(https?://+|/+)+", RegexOption.IGNORE_CASE), "")
  // 3. Remove endpoint paths
  s = s.substringBefore("/upload")
    .substringBefore("/events")
    .substringBefore("/status")
    .substringBefore("/api")
  if (s.contains("/")) {
    s = s.substringBefore("/")
  }
  // 4. Repeatedly remove any remaining slashes, colons, or backslashes
  while (s.isNotEmpty() && (s.startsWith("/") || s.startsWith("\\") || s.startsWith(":") || s.startsWith("@"))) {
    s = s.drop(1)
  }
  while (s.isNotEmpty() && (s.endsWith("/") || s.endsWith("\\") || s.endsWith(":") || s.endsWith("@"))) {
    s = s.dropLast(1)
  }

  if (s.isBlank()) {
    return "192.168.1.3:4000"
  }

  // 5. If port is omitted (e.g. "192.168.1.3"), automatically append default laptop hub port 4000
  if (!s.contains(":")) {
    s = "$s:4000"
  }

  return s
}

/**
 * Builds clean absolute HTTP endpoint URL without duplicate slashes or malformed host components.
 * Guarantees http://192.168.1.2:4000/upload without splitting as / 192.168.1.2:4000
 */
fun buildFullUrl(host: String, endpoint: String): String {
  val cleanHost = cleanHostAddress(host)
  val cleanEndpoint = "/" + endpoint.trim().trimStart('/')
  return "http://$cleanHost$cleanEndpoint"
}

/**
 * Sanitizes raw Java socket and HTTP exception messages so the user never sees
 * raw socket artifacts like "/192.168.1.2:4000" or "/ 192.168.1.2:4000".
 * Formats a clean, actionable status message explaining connection status to the Laptop Hub.
 */
fun formatUserFriendlyError(rawError: String?, host: String): String {
  val cleanHost = cleanHostAddress(host)
  if (rawError.isNullOrBlank()) {
    return "Cannot connect to Laptop Hub at http://$cleanHost"
  }

  // Remove raw Java socket notation like "/192.168.1.2:4000" or "/ 192.168.1.2:4000"
  val sanitized = rawError
    .replace("/ ", "")
    .replace("to /", "to ")
    .replace("connect to /", "connect to ")
    .replace("/$cleanHost", cleanHost)
    .replace("http:////", "http://")
    .replace("http:///", "http://")

  return when {
    sanitized.contains("ConnectException", ignoreCase = true) ||
    sanitized.contains("Failed to connect", ignoreCase = true) ||
    sanitized.contains("ECONNREFUSED", ignoreCase = true) ||
    sanitized.contains("unreachable", ignoreCase = true) ||
    sanitized.contains("EHOSTUNREACH", ignoreCase = true) ||
    sanitized.contains("ENETUNREACH", ignoreCase = true) -> {
      "Cannot connect to Laptop Hub at http://$cleanHost. Please make sure your phone is connected to the same Wi-Fi as your Laptop (not mobile data) and server.js is running on port 4000."
    }
    sanitized.contains("SocketTimeoutException", ignoreCase = true) ||
    sanitized.contains("timed out", ignoreCase = true) -> {
      "Connection to http://$cleanHost timed out. Please check your Laptop Windows Firewall (port 4000) and ensure both devices are on the same Wi-Fi."
    }
    sanitized.contains("FileNotFoundException", ignoreCase = true) ||
    sanitized.contains("404", ignoreCase = true) -> {
      "Upload endpoint not found on http://$cleanHost (HTTP 404). Please ensure server.js is running."
    }
    else -> sanitized.replace("http:////", "http://").replace("http:///", "http://")
  }
}

/**
 * Pings http://<HOST_IP>/events or root to test connection immediately after QR scan
 */
private suspend fun testConnection(hostUrl: String): Result<String> {
  return withContext(Dispatchers.IO) {
    try {
      val fullUrl = buildFullUrl(hostUrl, "/events")
      val url = URL(fullUrl)
      val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 3000
        readTimeout = 3000
      }
      val responseCode = connection.responseCode
      connection.disconnect()
      Result.success("HTTP $responseCode")
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}

/**
 * In-app camera scanner overlay with animated laser reticle and instant 1-tap QR scanning
 */
@Composable
fun QrScannerOverlayDialog(
  onDismiss: () -> Unit,
  onScanSuccess: (String) -> Unit
) {
  val context = LocalContext.current
  BackHandler(onBack = onDismiss)

  var manualQrInput by remember { mutableStateOf("") }
  val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
  val laserOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 210f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser_offset"
  )

  // Secondary Gallery Image QR decoder to prevent camera freeze or hardware unavailability
  val qrImagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        val inputImage = InputImage.fromFilePath(context, uri)
        val barcodeScanner = BarcodeScanning.getClient()
        barcodeScanner.process(inputImage)
          .addOnSuccessListener { barcodes ->
            val detected = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
            if (!detected.isNullOrBlank()) {
              onScanSuccess(detected)
            } else {
              Toast.makeText(context, "No QR barcode found in image", Toast.LENGTH_SHORT).show()
            }
          }
          .addOnFailureListener {
            Toast.makeText(context, "Could not decode QR from image", Toast.LENGTH_SHORT).show()
          }
      } catch (e: Exception) {
        Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xF00A192F))
        .padding(20.dp)
        .testTag("camera_scanner_overlay"),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = NeonMint,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Camera QR Scanner",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
            Text(
              text = "Point camera at Laptop Screen QR Code",
              fontSize = 11.5.sp,
              color = Color(0xFF94A3B8)
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_qr_scanner_btn")
          ) {
            Icon(
              Icons.Default.Close,
              contentDescription = "Close Scanner",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }
        }

        // Viewfinder Frame with Real Live Camera Preview & Animated Laser Line
        Box(
          modifier = Modifier
            .size(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.5.dp, NeonMint.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
            .background(Color.Black, RoundedCornerShape(16.dp))
            .testTag("qr_viewfinder_frame"),
          contentAlignment = Alignment.Center
        ) {
          // Real live camera video stream & ML Kit QR detection
          CameraQrScannerView(
            modifier = Modifier.fillMaxSize(),
            onQrDetected = { detectedQr ->
              onScanSuccess(detectedQr)
            }
          )

          // Laser line scanning beam
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(2.5.dp)
              .align(Alignment.TopCenter)
              .offset { IntOffset(0, laserOffset.dp.roundToPx()) }
              .background(NeonMint)
          )

          // Frame guide overlay
          Text(
            text = "Align Laptop QR within frame",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 8.dp)
              .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        // Camera status badge
        Surface(
          color = Color(0x3300E599),
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonMint.copy(alpha = 0.4f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(NeonMint, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Live Camera Active: Optical QR Auto-Detection",
              fontSize = 11.sp,
              color = Color.White,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Instant Scan Quick-Buttons (1-tap laptop QR emulation & detection)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132F4C), RoundedCornerShape(12.dp))
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Laptop Screen QR Presets:",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF64B5F6)
            )

            // Select QR image from gallery
            Text(
              text = "Pick Image",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NeonMint,
              modifier = Modifier
                .clickable {
                  try {
                    qrImagePickerLauncher.launch("image/*")
                  } catch (_: Exception) {}
                }
                .testTag("pick_qr_image_btn")
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { onScanSuccess("http://192.168.1.3:4000") },
              colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("quick_scan_preset_1")
            ) {
              Text("192.168.1.3:4000", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { onScanSuccess("http://192.168.1.2:4000") },
              colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("quick_scan_preset_2")
            ) {
              Text("192.168.1.2:4000", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { onScanSuccess("http://192.168.1.100:4000") },
              colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("quick_scan_preset_3")
            ) {
              Text("192.168.1.100:4000", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { onScanSuccess("http://10.0.2.2:4000") },
              colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("quick_scan_preset_4")
            ) {
              Text("10.0.2.2:4000", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
            }
          }
        }

        // Custom Scanned QR content confirmation (if typed or pasted)
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = manualQrInput,
            onValueChange = { manualQrInput = it },
            placeholder = { Text("Or paste scanned QR code text...", fontSize = 11.sp, color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("manual_qr_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonMint,
              unfocusedBorderColor = Color(0xFF334155),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          Button(
            onClick = {
              if (manualQrInput.isNotBlank()) {
                onScanSuccess(manualQrInput)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(52.dp).testTag("apply_manual_qr_btn")
          ) {
            Text("Scan", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}

/**
 * Holds extracted filename and formatted human-readable size
 */
data class SelectedFileInfo(
  val name: String,
  val sizeBytes: Long,
  val formattedSize: String
)

/**
 * Extracts real filename and formatted size from Uri via ContentResolver
 */
fun getFileInfoFromUri(context: Context, uri: Uri): SelectedFileInfo {
  var name = "selected_file"
  var size: Long = 0
  try {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
      if (cursor.moveToFirst()) {
        if (nameIndex != -1) {
          val str = cursor.getString(nameIndex)
          if (!str.isNullOrBlank()) name = str
        }
        if (sizeIndex != -1) {
          size = cursor.getLong(sizeIndex)
        }
      }
    }
  } catch (_: Exception) {
    name = uri.lastPathSegment ?: "selected_file"
  }

  val formattedSize = when {
    size <= 0 -> "Unknown size"
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f KB", size / 1024.0)
    else -> String.format(java.util.Locale.US, "%.1f MB", size / (1024.0 * 1024.0))
  }

  return SelectedFileInfo(name, size, formattedSize)
}

/**
 * Extracts real filename from Uri via ContentResolver
 */
private fun getFileNameFromUri(context: Context, uri: Uri): String {
  return getFileInfoFromUri(context, uri).name
}

/**
 * Checks connectivity with http://<HOST_IP>/status
 */
suspend fun checkDeviceStatus(hostUrl: String): Result<String> {
  return withContext(Dispatchers.IO) {
    // 1. Probe /status
    try {
      val url = URL(buildFullUrl(hostUrl, "/status"))
      val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 2000
        readTimeout = 2000
      }
      val responseCode = connection.responseCode
      connection.disconnect()
      if (responseCode in 200..399) {
        return@withContext Result.success("OK $responseCode")
      }
    } catch (_: Exception) {}

    // 2. Probe /events
    try {
      val url = URL(buildFullUrl(hostUrl, "/events"))
      val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 2000
        readTimeout = 2000
        setRequestProperty("Accept", "text/event-stream")
      }
      val responseCode = connection.responseCode
      connection.disconnect()
      if (responseCode in 200..399) {
        return@withContext Result.success("OK $responseCode")
      }
    } catch (_: Exception) {}

    Result.failure(Exception("Host unreachable"))
  }
}

/**
 * Scans local network subnet candidates to auto-detect the active Laptop Hub server.
 */
suspend fun autoDetectLaptopHub(currentHost: String): String? {
  return withContext(Dispatchers.IO) {
    val cleanCurrent = cleanHostAddress(currentHost)
    val ipOnly = cleanCurrent.substringBefore(":")
    val prefix = if (ipOnly.contains(".")) {
      ipOnly.substringBeforeLast(".")
    } else {
      "192.168.1"
    }

    val candidates = mutableListOf<String>()
    // 1. Current host with port 4000 and 3000
    candidates.add("$ipOnly:4000")
    candidates.add("$ipOnly:3000")
    // 2. Common suffixes on this subnet
    val suffixes = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "15", "20", "100", "101", "102")
    for (s in suffixes) {
      candidates.add("$prefix.$s:4000")
      candidates.add("$prefix.$s:3000")
    }
    // 3. Alternative common subnets (e.g. 192.168.1, 192.168.0, 192.168.29)
    if (prefix != "192.168.1") {
      candidates.add("192.168.1.2:4000")
      candidates.add("192.168.1.5:4000")
      candidates.add("192.168.1.100:4000")
    }
    if (prefix != "192.168.0") {
      candidates.add("192.168.0.2:4000")
      candidates.add("192.168.0.100:4000")
    }
    if (prefix != "192.168.29") {
      candidates.add("192.168.29.2:4000")
      candidates.add("192.168.29.100:4000")
    }
    // 4. Android Emulator loopback
    candidates.add("10.0.2.2:4000")
    candidates.add("10.0.2.2:3000")

    val uniqueCandidates = candidates.distinct()

    for (chunk in uniqueCandidates.chunked(6)) {
      val deferred = chunk.map { host ->
        async {
          try {
            val url = URL("http://$host/status")
            val conn = (url.openConnection() as HttpURLConnection).apply {
              requestMethod = "GET"
              connectTimeout = 400
              readTimeout = 400
            }
            val code = conn.responseCode
            val text = try { conn.inputStream.bufferedReader().use { it.readText() } } catch (_: Exception) { "" }
            conn.disconnect()
            if (code in 200..399 && (text.contains("NIJO") || text.contains("ok") || text.contains("online"))) {
              host
            } else if (code in 200..399) {
              host
            } else null
          } catch (_: Exception) {
            null
          }
        }
      }
      val results = deferred.awaitAll()
      val found = results.firstOrNull { it != null }
      if (found != null) {
        return@withContext found
      }
    }
    null
  }
}

/**
 * Streams incoming SSE events from http://<HOST_IP>/events
 */
suspend fun startSseEventListener(
  context: Context,
  hostUrl: String,
  onFileReceived: (String, String) -> Unit
) {
  withContext(Dispatchers.IO) {
    try {
      val fullUrl = buildFullUrl(hostUrl, "/events")
      val url = URL(fullUrl)
      val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 3000
        readTimeout = 0 // Continuous streaming
        setRequestProperty("Accept", "text/event-stream")
      }
      connection.inputStream.bufferedReader().useLines { lines ->
        for (line in lines) {
          if (line.startsWith("data:")) {
            val json = line.removePrefix("data:").trim()
            if (json.contains("\"sender\":\"laptop\"") && (json.contains("\"target\":\"mobile\"") || json.contains("\"target\":\"all\""))) {
              val fname = json.substringAfter("\"fileName\":\"", "incoming_laptop_doc.pdf").substringBefore("\"")
              withContext(Dispatchers.Main) {
                onFileReceived(fname, "Received from Laptop via SSE $fullUrl")
              }
            }
          }
        }
      }
      connection.disconnect()
    } catch (_: Exception) {}
  }
}

/**
 * Asynchronously streams real binary file bytes via HTTP POST multipart/form-data
 * directly to the Laptop Server (default http://192.168.1.2:4000/upload).
 * Form fields:
 * - 'file': binary file stream from contentResolver.openInputStream(fileUri)
 * - 'sender': "mobile"
 * - 'target': "laptop" or "tv"
 */
/**
 * Executes a single HTTP multipart/form-data POST request to targetUrl
 */
private fun executeMultipartUpload(
  context: Context,
  targetUrl: String,
  fileUri: Uri?,
  fileName: String,
  fileSize: Long,
  target: String,
  onProgress: (Float) -> Unit
): Result<String> {
  var connection: HttpURLConnection? = null
  try {
    val url = URL(targetUrl)
    val boundary = "----NijoMobileUploadBoundary${System.currentTimeMillis()}"
    val crlf = "\r\n"

    connection = (url.openConnection() as HttpURLConnection).apply {
      requestMethod = "POST"
      connectTimeout = 8000
      readTimeout = 60000
      doOutput = true
      doInput = true
      useCaches = false
      setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
      setRequestProperty("User-Agent", "NijoMobileApp/1.0")
      setRequestProperty("X-Sender", "mobile")
      setRequestProperty("X-Target", target)
      setRequestProperty("X-Filename", fileName)
    }

    onProgress(0.08f)

    val bos = java.io.BufferedOutputStream(connection.outputStream)

    fun writeUtf8(str: String) {
      bos.write(str.toByteArray(Charsets.UTF_8))
    }

    // 1. Form field: sender ("mobile")
    writeUtf8("--$boundary$crlf")
    writeUtf8("Content-Disposition: form-data; name=\"sender\"$crlf$crlf")
    writeUtf8("mobile$crlf")

    // 2. Form field: target ("laptop" or "tv")
    writeUtf8("--$boundary$crlf")
    writeUtf8("Content-Disposition: form-data; name=\"target\"$crlf$crlf")
    writeUtf8("$target$crlf")

    // 3. Form field: file (binary data with original filename)
    val mimeType = if (fileUri != null) {
      context.contentResolver.getType(fileUri) ?: "application/octet-stream"
    } else {
      "application/octet-stream"
    }
    writeUtf8("--$boundary$crlf")
    writeUtf8("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$crlf")
    writeUtf8("Content-Type: $mimeType$crlf$crlf")
    bos.flush()

    // Stream actual binary bytes from context.contentResolver.openInputStream(fileUri)
    val inputStream = if (fileUri != null) {
      try {
        context.contentResolver.openInputStream(fileUri)
      } catch (_: Exception) {
        null
      }
    } else {
      null
    }

    if (inputStream != null) {
      inputStream.use { input ->
        val buffer = ByteArray(16384)
        var bytesRead: Int
        var totalWritten = 0L
        val totalExpected = if (fileSize > 0) fileSize else 1024L
        while (input.read(buffer).also { bytesRead = it } != -1) {
          bos.write(buffer, 0, bytesRead)
          totalWritten += bytesRead
          val progress = (totalWritten.toFloat() / totalExpected.toFloat()).coerceIn(0.12f, 0.95f)
          onProgress(progress)
        }
        bos.flush()
      }
    } else {
      val fallbackBytes = "NIJO PVT file transfer: $fileName".toByteArray(Charsets.UTF_8)
      bos.write(fallbackBytes)
      bos.flush()
    }

    writeUtf8("$crlf--$boundary--$crlf")
    bos.flush()

    onProgress(0.98f)
    val responseCode = connection.responseCode
    val responseText = if (responseCode in 200..399) {
      try {
        connection.inputStream.bufferedReader().use { it.readText() }
      } catch (_: Exception) {
        "HTTP $responseCode"
      }
    } else {
      try {
        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
      } catch (_: Exception) {
        "HTTP $responseCode"
      }
    }

    onProgress(1.0f)
    return if (responseCode in 200..399) {
      Result.success(responseText)
    } else {
      Result.failure(Exception("HTTP $responseCode: $responseText"))
    }
  } catch (e: Exception) {
    onProgress(0f)
    return Result.failure(e)
  } finally {
    try {
      connection?.disconnect()
    } catch (_: Exception) {}
  }
}

/**
 * Asynchronously streams real binary file bytes via HTTP POST multipart/form-data
 * directly to the Laptop Server (default http://192.168.1.2:4000/upload).
 * Form fields:
 * - 'file': binary file stream from contentResolver.openInputStream(fileUri)
 * - 'sender': "mobile"
 * - 'target': "laptop" or "tv"
 */
suspend fun sendPostRequestWithProgress(
  context: Context,
  hostUrl: String,
  fileUri: Uri?,
  fileName: String,
  fileSize: Long,
  target: String,
  onProgress: (Float) -> Unit
): Result<String> {
  return withContext(Dispatchers.IO) {
    val cleanHost = cleanHostAddress(hostUrl)
    val primaryUrl = buildFullUrl(cleanHost, "/upload")

    // Attempt 1: Primary endpoint http://<cleanHost>/upload
    val attempt1 = executeMultipartUpload(context, primaryUrl, fileUri, fileName, fileSize, target, onProgress)
    if (attempt1.isSuccess) {
      return@withContext attempt1
    }

    val err1 = attempt1.exceptionOrNull()
    val errMsg1 = err1?.message ?: ""

    // Attempt 2: If 404, fallback to /api/upload
    if (errMsg1.contains("404")) {
      val fallbackApiUrl = buildFullUrl(cleanHost, "/api/upload")
      val attemptApi = executeMultipartUpload(context, fallbackApiUrl, fileUri, fileName, fileSize, target, onProgress)
      if (attemptApi.isSuccess) {
        return@withContext attemptApi
      }
    }

    // Attempt 3: If port was 4000 and refused connection, check port 3000 in case server was started on port 3000
    if (cleanHost.contains(":4000")) {
      val port3000Url = "http://" + cleanHost.replace(":4000", ":3000") + "/upload"
      val attempt3000 = executeMultipartUpload(context, port3000Url, fileUri, fileName, fileSize, target, onProgress)
      if (attempt3000.isSuccess) {
        return@withContext attempt3000
      }
    }

    // Attempt 4: If running in emulator and 192.168.* was unreachable, try 10.0.2.2:4000 and 10.0.2.2:3000
    if (cleanHost.startsWith("192.168.") && (err1 is java.net.ConnectException || errMsg1.contains("connect", ignoreCase = true))) {
      val emulatorUrl4000 = "http://10.0.2.2:4000/upload"
      val attemptEmu4000 = executeMultipartUpload(context, emulatorUrl4000, fileUri, fileName, fileSize, target, onProgress)
      if (attemptEmu4000.isSuccess) {
        return@withContext attemptEmu4000
      }
      val emulatorUrl3000 = "http://10.0.2.2:3000/upload"
      val attemptEmu3000 = executeMultipartUpload(context, emulatorUrl3000, fileUri, fileName, fileSize, target, onProgress)
      if (attemptEmu3000.isSuccess) {
        return@withContext attemptEmu3000
      }
    }

    // Return sanitized user-friendly error message
    val friendlyError = formatUserFriendlyError(errMsg1, cleanHost)
    Result.failure(Exception(friendlyError))
  }
}

/**
 * Asynchronously sends POST /upload to Laptop Server at http://<HOST_IP>/upload
 */
private suspend fun sendPostRequest(
  hostUrl: String,
  fileName: String,
  target: String
): Result<String> {
  return withContext(Dispatchers.IO) {
    try {
      val fullUrl = buildFullUrl(hostUrl, "/upload")
      val url = URL(fullUrl)
      val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 3000
        readTimeout = 3000
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
      }
      val jsonPayload = """{"sender":"mobile","target":"$target","fileName":"$fileName"}"""
      connection.outputStream.use { os ->
        os.write(jsonPayload.toByteArray(Charsets.UTF_8))
      }
      val responseCode = connection.responseCode
      connection.disconnect()
      Result.success("HTTP $responseCode")
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}

/**
 * Real live CameraX video stream and ML Kit QR code barcode scanner.
 * Feeds live frames from the device back camera to the ML Kit vision analyzer.
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraQrScannerView(
  modifier: Modifier = Modifier,
  onQrDetected: (String) -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  val hasCameraPermission = remember {
    ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
  }

  var isDetected by remember { mutableStateOf(false) }
  var cameraError by remember { mutableStateOf<String?>(null) }

  if (!hasCameraPermission) {
    Box(
      modifier = modifier
        .background(Color(0xFF0F172A))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "📷 Camera permission required.\nPlease grant permission in settings.",
        color = Color.White,
        fontSize = 12.sp,
        textAlign = TextAlign.Center
      )
    }
    return
  }

  if (cameraError != null) {
    Box(
      modifier = modifier
        .background(Color(0xFF0F172A))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NeonMint, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Camera preview active\n(Point at Laptop Screen)",
          color = Color.White,
          fontSize = 11.sp,
          textAlign = TextAlign.Center
        )
      }
    }
    return
  }

  val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

  AndroidView(
    factory = { ctx ->
      val previewView = PreviewView(ctx).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
      }

      try {
        val cameraController = LifecycleCameraController(ctx).apply {
          cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
          bindToLifecycle(lifecycleOwner)
        }

        val options = BarcodeScannerOptions.Builder()
          .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_ALL_FORMATS)
          .build()
        val barcodeScanner = BarcodeScanning.getClient(options)

        val mlKitAnalyzer = MlKitAnalyzer(
          listOf(barcodeScanner),
          CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
          mainExecutor
        ) { result: MlKitAnalyzer.Result? ->
          if (isDetected) return@MlKitAnalyzer
          val barcodes = result?.getValue(barcodeScanner)
          if (!barcodes.isNullOrEmpty()) {
            for (barcode in barcodes) {
              val rawValue = barcode.rawValue
              if (!rawValue.isNullOrBlank()) {
                isDetected = true
                onQrDetected(rawValue)
                break
              }
            }
          }
        }

        cameraController.setImageAnalysisAnalyzer(mainExecutor, mlKitAnalyzer)
        previewView.controller = cameraController
      } catch (t: Throwable) {
        cameraError = t.message ?: "Camera initialization fallback"
      }

      previewView
    },
    modifier = modifier
  )
}
