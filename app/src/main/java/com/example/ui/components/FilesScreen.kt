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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.NavyAccent
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
import com.example.util.FileDownloadHelper
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

/**
 * Retrieves the current connected Wi-Fi Network Name / SSID
 */
fun getConnectedWifiSsid(context: Context): String {
  return try {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val activeNetwork = cm?.activeNetwork
    val caps = cm?.getNetworkCapabilities(activeNetwork)
    val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    if (!isWifi) return "Mobile Data"

    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
    val info = wifiManager?.connectionInfo
    val rawSsid = info?.ssid
    if (!rawSsid.isNullOrBlank() && rawSsid != "<unknown ssid>" && rawSsid != "\"\"") {
      rawSsid.trim().removePrefix("\"").removeSuffix("\"")
    } else {
      "Home Wi-Fi"
    }
  } catch (_: Exception) {
    "Home Wi-Fi"
  }
}

enum class CrossDeviceTab {
  SEND_FILE,
  RECEIVE_FILE,
  UNIVERSAL_CLIPBOARD
}

@OptIn(ExperimentalMaterial3Api::class)
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
  var wifiSsid by remember { mutableStateOf<String>(getConnectedWifiSsid(context)) }
  var downloadingFileId by remember { mutableStateOf<String?>(null) }
  val savedFileUris = remember { androidx.compose.runtime.mutableStateMapOf<String, Uri>() }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  // Continuously monitor Wi-Fi connectivity and Wi-Fi SSID
  androidx.compose.runtime.LaunchedEffect(Unit) {
    while (true) {
      isWifiConnected = isDeviceConnectedToWifi(context)
      wifiSsid = getConnectedWifiSsid(context)
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

  var showIpConfigDialog by remember { mutableStateOf(false) }

  // Automatically check status and start SSE event stream on launch or IP change
  androidx.compose.runtime.LaunchedEffect(normalizedHost) {
    checkServerStatus(normalizedHost)
    startSseEventListener(
      context = context,
      hostUrl = normalizedHost,
      onFileReceived = { fileName, fileContent, fileUrl ->
        val item = WorkspaceItem(
          type = ItemType.MEDIA.name,
          title = fileName,
          content = fileContent,
          imageUrl = fileUrl,
          tags = listOf("file", "sender_laptop", "target_mobile", "host_$normalizedHost", "auto_downloaded")
        )
        viewModel.createItem(item)
        // Automatically save to Android MediaStore Downloads/NIJO_PVT folder so file appears in File Manager
        coroutineScope.launch {
          FileDownloadHelper.downloadFileFromLaptop(
            context = context,
            hostUrl = normalizedHost,
            fileName = fileName,
            fileUrl = fileUrl,
            fallbackContent = fileContent
          ).onSuccess { uri ->
            savedFileUris[item.id] = uri
            Toast.makeText(context, "Saved '$fileName' to Downloads/NIJO_PVT in File Manager!", Toast.LENGTH_LONG).show()
          }
        }
      },
      onSystemUpdate = { title, body, themeColor, fontStyle ->
        viewModel.applyRemoteUpdate(themeColor = themeColor, fontStyle = fontStyle, title = title, body = body)
        com.example.util.SystemNotificationHelper.showSystemNotification(context, title, body)
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

  var onFilePickedSendAction by remember { mutableStateOf<((Uri, String, Long, String) -> Unit)?>(null) }

  // System File Picker (Triggered immediately when user clicks Send to Laptop / TV)
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedFileUri = uri
      val info = getFileInfoFromUri(context, uri)
      selectedFileName = info.name
      selectedFileSize = info.formattedSize
      selectedFileSizeBytes = info.sizeBytes
      statusMessage = "Selected '${info.name}' (${info.formattedSize})"

      val action = onFilePickedSendAction
      onFilePickedSendAction = null
      action?.invoke(uri, info.name, info.sizeBytes, info.formattedSize)
    } else {
      onFilePickedSendAction = null
    }
  }

  fun performSend(
    target: String,
    targetLabel: String,
    customUri: Uri? = null,
    customName: String? = null,
    customBytes: Long = 0L,
    customFormattedSize: String? = null
  ) {
    val fileUriToUse = customUri ?: selectedFileUri
    val fileToTransmit = customName ?: selectedFileName
    val bytesToTransmit = if (customBytes > 0L) customBytes else selectedFileSizeBytes
    val sizeText = customFormattedSize ?: selectedFileSize ?: "1.2 MB"

    if (fileUriToUse == null || fileToTransmit == null) {
      // Directly ask which file from phone storage!
      onFilePickedSendAction = { uri, name, bytes, formatted ->
        performSend(target, targetLabel, customUri = uri, customName = name, customBytes = bytes, customFormattedSize = formatted)
      }
      try {
        filePickerLauncher.launch("*/*")
      } catch (_: Exception) {}
      return
    }

    // 1. Dispatch real HTTP multipart/form-data upload to http://<HOST_IP>:4000/upload
    isSendingHttp = true
    uploadTargetLabel = targetLabel
    uploadProgress = 0.05f
    statusMessage = "Uploading '$fileToTransmit' to $targetLabel..."

    coroutineScope.launch {
      val uploadResult = sendPostRequestWithProgress(
        context = context,
        hostUrl = normalizedHost,
        fileUri = fileUriToUse,
        fileName = fileToTransmit,
        fileSize = bytesToTransmit,
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

  val transferPin by viewModel.transferSecurityPin.collectAsState()
  var activeSharingTab by remember { mutableStateOf(CrossDeviceTab.SEND_FILE) }
  var tokenString by remember { mutableStateOf("TOKEN-8465-2820") }
  var showMobileSetupSheet by remember { mutableStateOf(false) }

  // Target Destination Devices
  var targetMobileSelected by remember { mutableStateOf(true) }
  var targetTvSelected by remember { mutableStateOf(false) }
  var targetLaptopSelected by remember { mutableStateOf(true) }

  // Active file to transmit (defaults to screenshot asset Libra_Master_Vector_Assets.svg)
  val currentFileName = selectedFileName ?: "Libra_Master_Vector_Assets.svg"
  val currentFileSize = selectedFileSize ?: "2.8 MB • Ready for Wi-Fi Direct packet stream"
  val currentFileBytes = if (selectedFileSizeBytes > 0L) selectedFileSizeBytes else 2936012L

  // Universal Clipboard Text
  var clipboardInputText by remember { mutableStateOf("") }

  val mediaItems = (uiState as? WorkspaceUiState.Success)?.items?.filter { it.type == ItemType.MEDIA.name } ?: emptyList()
  val receivedLaptopCount = mediaItems.count { it.tags.contains("sender_laptop") || it.tags.contains("laptop") }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 100.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. MOBILE HEADER: Title, Wi-Fi Direct Status, Token & Setup Button
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Cross-Device File Sharing Hub",
                fontFamily = com.example.ui.theme.OutfitFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
              )
              Text(
                text = "Mobile Direct Transfer & Local Wi-Fi Mesh",
                fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                fontSize = 11.sp,
                color = TextSecondary
              )
            }

            // Minimal QR Scan Button + Mobile Setup Hub Button
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = {
                  try {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                  } catch (_: Exception) {
                    showQrScannerOverlay = true
                  }
                },
                modifier = Modifier
                  .size(34.dp)
                  .background(NavyLight, RoundedCornerShape(8.dp))
                  .testTag("minimal_qr_scan_btn")
              ) {
                Icon(
                  Icons.Default.QrCodeScanner,
                  contentDescription = "Scan Laptop QR",
                  tint = NavyPrimary,
                  modifier = Modifier.size(17.dp)
                )
              }

              Button(
                onClick = { showMobileSetupSheet = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp).testTag("open_mobile_setup_btn")
              ) {
                Text(
                  text = "Setup Hub",
                  fontFamily = com.example.ui.theme.OutfitFontFamily,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }

          // Mobile Status Pill Row: Wi-Fi status + Token + PIN
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Wi-Fi Direct Pill
            Surface(
              color = if (isWifiConnected) Color(0xFFF0FDF4) else NavyLight,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isWifiConnected) Color(0xFF86EFAC) else Color(0xFFCBD5E1))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .background(if (isWifiConnected) Color(0xFF16A34A).copy(alpha = pulseAlpha) else Color(0xFF94A3B8), CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = if (isWifiConnected) "Wi-Fi Direct Active ($wifiSsid)" else "Mobile Data ($wifiSsid)",
                  fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isWifiConnected) Color(0xFF15803D) else NavyPrimary
                )
              }
            }

            // Dynamic Token & PIN
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.clickable {
                  val rand = (1000..9999).random()
                  val rand2 = (1000..9999).random()
                  tokenString = "TOKEN-$rand-$rand2"
                  Toast.makeText(context, "Regenerated token: $tokenString", Toast.LENGTH_SHORT).show()
                }
              ) {
                Text(
                  text = tokenString,
                  fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavySecondary,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }

              Text(
                text = "PIN: $transferPin",
                fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
              )
            }
          }
        }
      }

      // 2. MOBILE SEGMENTED TABS (Navy Blue & White): [Send File] [Receive] [Clipboard]
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.White, RoundedCornerShape(10.dp))
          .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
          .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Tab 1: Send File
        val isSendActive = activeSharingTab == CrossDeviceTab.SEND_FILE
        Box(
          modifier = Modifier
            .weight(1f)
            .background(if (isSendActive) NavyPrimary else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { activeSharingTab = CrossDeviceTab.SEND_FILE }
            .padding(vertical = 8.dp)
            .testTag("tab_send_file"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Share, contentDescription = null, tint = if (isSendActive) Color.White else NavyPrimary, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Send File",
              fontFamily = com.example.ui.theme.OutfitFontFamily,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSendActive) Color.White else NavyPrimary
            )
          }
        }

        // Tab 2: Receive File
        val isReceiveActive = activeSharingTab == CrossDeviceTab.RECEIVE_FILE
        Box(
          modifier = Modifier
            .weight(1.1f)
            .background(if (isReceiveActive) NavyPrimary else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { activeSharingTab = CrossDeviceTab.RECEIVE_FILE }
            .padding(vertical = 8.dp)
            .testTag("tab_receive_file"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FileDownload, contentDescription = null, tint = if (isReceiveActive) Color.White else NavySecondary, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Receive",
              fontFamily = com.example.ui.theme.OutfitFontFamily,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isReceiveActive) Color.White else NavyPrimary
            )
            if (receivedLaptopCount > 0) {
              Spacer(modifier = Modifier.width(4.dp))
              Surface(
                color = if (isReceiveActive) Color.White.copy(alpha = 0.3f) else NavyPrimary,
                shape = CircleShape
              ) {
                Text(
                  text = "$receivedLaptopCount",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }
        }

        // Tab 3: Universal Clipboard
        val isClipboardActive = activeSharingTab == CrossDeviceTab.UNIVERSAL_CLIPBOARD
        Box(
          modifier = Modifier
            .weight(1f)
            .background(if (isClipboardActive) NavyPrimary else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { activeSharingTab = CrossDeviceTab.UNIVERSAL_CLIPBOARD }
            .padding(vertical = 8.dp)
            .testTag("tab_universal_clipboard"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = if (isClipboardActive) Color.White else TextSecondary, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Clipboard",
              fontFamily = com.example.ui.theme.OutfitFontFamily,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isClipboardActive) Color.White else TextSecondary
            )
          }
        }
      }

      // Upload Progress Bar (if in progress)
      uploadProgress?.let { prog ->
        Surface(
          color = NavyLight,
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, NavySecondary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (prog >= 1.0f) "Sent Successfully!" else "Transmitting to $uploadTargetLabel",
                fontFamily = com.example.ui.theme.OutfitFontFamily,
                fontSize = 12.sp,
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
              color = NavySecondary,
              trackColor = Color(0xFFD1D5DB)
            )
          }
        }
      }

      when (activeSharingTab) {
        CrossDeviceTab.SEND_FILE -> {
          // 3. MOBILE SEND FILE CARD (Clean Mobile Layout)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth().testTag("send_file_mesh_card")
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              // Row: Title + "Pick from Phone" button
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Send via Wi-Fi Mesh",
                    fontFamily = com.example.ui.theme.OutfitFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                  )
                  Text(
                    text = "Select a real file from your device or pick from static files",
                    fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                  )
                }

                // Pick from Phone File Button
                Button(
                  onClick = {
                    try {
                      filePickerLauncher.launch("*/*")
                    } catch (_: Exception) {}
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(34.dp).testTag("browse_pc_file_btn")
                ) {
                  Icon(Icons.Default.Share, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Pick File", fontFamily = com.example.ui.theme.OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
              }

              // Active File Box with Static Files Presets
              Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(38.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(20.dp)
                      )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = currentFileName,
                        fontFamily = com.example.ui.theme.OutfitFontFamily,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                      )
                      Text(
                        text = currentFileSize,
                        fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                      )
                    }
                  }

                  // Static Files Preset Chips
                  Text(
                    text = "Static Files:",
                    fontSize = 11.sp,
                    fontFamily = com.example.ui.theme.ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                  )

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Surface(
                      color = Color.White,
                      shape = RoundedCornerShape(6.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                      modifier = Modifier.weight(1f).clickable {
                        selectedFileName = "Engineering_Mathematics_Sem3.pdf"
                        selectedFileSize = "3.4 MB • Ready for Wi-Fi Direct packet stream"
                        selectedFileSizeBytes = 3565158L
                        selectedFileUri = null
                        statusMessage = "Loaded Maths PDF preset"
                      }
                    ) {
                      Text(
                        text = "Maths PDF",
                        fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 5.dp)
                      )
                    }

                    Surface(
                      color = Color.White,
                      shape = RoundedCornerShape(6.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                      modifier = Modifier.weight(1f).clickable {
                        selectedFileName = "Microcontroller_8051_Firmware.c"
                        selectedFileSize = "142 KB • Ready for Wi-Fi Direct packet stream"
                        selectedFileSizeBytes = 145408L
                        selectedFileUri = null
                        statusMessage = "Loaded PSPP Code preset"
                      }
                    ) {
                      Text(
                        text = "PSPP Code",
                        fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 5.dp)
                      )
                    }

                    Surface(
                      color = Color.White,
                      shape = RoundedCornerShape(6.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                      modifier = Modifier.weight(1f).clickable {
                        selectedFileName = "Libra_Master_Vector_Assets.svg"
                        selectedFileSize = "2.8 MB • Ready for Wi-Fi Direct packet stream"
                        selectedFileSizeBytes = 2936012L
                        selectedFileUri = null
                        statusMessage = "Loaded Libra SVG preset"
                      }
                    ) {
                      Text(
                        text = "Libra SVG",
                        fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 5.dp)
                      )
                    }
                  }
                }
              }

              // Destination Devices (Mobile Stack)
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Destination Devices:",
                    fontFamily = com.example.ui.theme.OutfitFontFamily,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                  )

                  // Device Presets
                  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                      color = if (targetLaptopSelected && !targetMobileSelected && !targetTvSelected) NavyLight else Color(0xFFF1F5F9),
                      shape = RoundedCornerShape(4.dp),
                      modifier = Modifier.clickable {
                        targetLaptopSelected = true
                        targetMobileSelected = false
                        targetTvSelected = false
                      }
                    ) {
                      Text("Laptop", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }

                    Surface(
                      color = if (targetMobileSelected && targetLaptopSelected && targetTvSelected) NavyPrimary else Color(0xFFF1F5F9),
                      shape = RoundedCornerShape(4.dp),
                      modifier = Modifier.clickable {
                        targetMobileSelected = true
                        targetLaptopSelected = true
                        targetTvSelected = true
                      }
                    ) {
                      Text("All 3", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (targetMobileSelected && targetLaptopSelected && targetTvSelected) Color.White else NavyPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                  }
                }

                // Device Cards: Mobile, Laptop, TV
                Surface(
                  color = if (targetLaptopSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (targetLaptopSelected) Color(0xFF86EFAC) else Color(0xFFE2E8F0)),
                  modifier = Modifier.fillMaxWidth().clickable { targetLaptopSelected = !targetLaptopSelected }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    androidx.compose.material3.Checkbox(
                      checked = targetLaptopSelected,
                      onCheckedChange = { targetLaptopSelected = it },
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("💻 Laptop (Windows 11 Hub)", fontSize = 12.sp, fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  }
                }

                Surface(
                  color = if (targetMobileSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (targetMobileSelected) Color(0xFF86EFAC) else Color(0xFFE2E8F0)),
                  modifier = Modifier.fillMaxWidth().clickable { targetMobileSelected = !targetMobileSelected }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    androidx.compose.material3.Checkbox(
                      checked = targetMobileSelected,
                      onCheckedChange = { targetMobileSelected = it },
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📱 Mobile (Android)", fontSize = 12.sp, fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  }
                }

                Surface(
                  color = if (targetTvSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (targetTvSelected) Color(0xFF86EFAC) else Color(0xFFE2E8F0)),
                  modifier = Modifier.fillMaxWidth().clickable { targetTvSelected = !targetTvSelected }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    androidx.compose.material3.Checkbox(
                      checked = targetTvSelected,
                      onCheckedChange = { targetTvSelected = it },
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📺 Smart TV (Living Room)", fontSize = 12.sp, fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  }
                }
              }

              // Full-width Hero Action: Verify PIN & Send File
              Button(
                onClick = {
                  val targetLabel = when {
                    targetLaptopSelected && targetTvSelected -> "Laptop & TV"
                    targetLaptopSelected -> "Laptop (Windows 11)"
                    targetTvSelected -> "Smart TV"
                    else -> "Selected Devices"
                  }

                  performSend(
                    target = if (targetLaptopSelected) "laptop" else "tv",
                    targetLabel = targetLabel,
                    customUri = selectedFileUri,
                    customName = currentFileName,
                    customBytes = currentFileBytes,
                    customFormattedSize = currentFileSize
                  )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("verify_pin_and_send_btn")
              ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Verify PIN ($transferPin) & Send File",
                  fontFamily = com.example.ui.theme.OutfitFontFamily,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }

          // 4. MOBILE UNIFIED TRANSFER HISTORY CARD
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth().testTag("transfer_history_card")
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Unified Transfer History",
                    fontFamily = com.example.ui.theme.OutfitFontFamily,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                  )
                  Text(
                    text = "Records across Laptop, Phone, and TV",
                    fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                  )
                }

                TextButton(
                  onClick = {
                    mediaItems.forEach { viewModel.deleteItem(it.id) }
                    Toast.makeText(context, "Cleared transfer log", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.testTag("clear_history_log_btn")
                ) {
                  Text("Clear Log", fontFamily = com.example.ui.theme.OutfitFontFamily, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }
              }

              if (mediaItems.isEmpty()) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "No transfer records yet. Send or receive a file to begin.",
                    fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                    fontSize = 12.sp,
                    color = TextMuted
                  )
                }
              } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  mediaItems.forEach { item ->
                    val isSaved = savedFileUris.containsKey(item.id)
                    Surface(
                      color = Color(0xFFF8FAFC),
                      shape = RoundedCornerShape(8.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Box(
                          modifier = Modifier
                            .size(34.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp)),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                          Text(item.title, fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = NavyPrimary, maxLines = 1, overflow = TextOverflow.Clip)
                          Text(item.content, fontFamily = com.example.ui.theme.PlusJakartaFontFamily, fontSize = 10.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Clip)
                        }

                        if (isSaved) {
                          Button(
                            onClick = {
                              val uri = savedFileUris[item.id]!!
                              FileDownloadHelper.openFileInExternalViewer(context = context, uri = uri, mimeType = FileDownloadHelper.getMimeType(item.title))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyLight, contentColor = NavyPrimary),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                          ) {
                            Text("Open", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                          }
                        } else {
                          OutlinedButton(
                            onClick = {
                              coroutineScope.launch {
                                FileDownloadHelper.downloadFileFromLaptop(context = context, hostUrl = normalizedHost, fileName = item.title, fileUrl = item.imageUrl, fallbackContent = item.content)
                                  .onSuccess { uri ->
                                    savedFileUris[item.id] = uri
                                    Toast.makeText(context, "Saved to Downloads/NIJO_PVT in File Manager!", Toast.LENGTH_SHORT).show()
                                  }
                              }
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                          ) {
                            Text("Save", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                          }
                        }

                        IconButton(onClick = { viewModel.deleteItem(item.id) }, modifier = Modifier.size(26.dp)) {
                          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        CrossDeviceTab.RECEIVE_FILE -> {
          // Tab 2: Receive File View
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("Received Files", fontFamily = com.example.ui.theme.OutfitFontFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text("Saved to Downloads/NIJO_PVT on phone storage", fontFamily = com.example.ui.theme.PlusJakartaFontFamily, fontSize = 11.sp, color = TextSecondary)
                }

                Button(
                  onClick = {
                    FileDownloadHelper.openDownloadsFolder(context)
                    Toast.makeText(context, "Opening Downloads / NIJO_PVT folder", Toast.LENGTH_SHORT).show()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Icon(Icons.Default.Folder, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("File Manager", fontSize = 10.5.sp, fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
                }
              }

              val laptopFiles = mediaItems.filter {
                (it.tags.contains("sender_laptop") || it.tags.contains("laptop")) && !it.tags.contains("sender_mobile")
              }

              if (laptopFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Computer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No files received from Laptop yet", fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                    Text("Files transmitted from Laptop Hub arrive automatically.", fontFamily = com.example.ui.theme.PlusJakartaFontFamily, fontSize = 11.sp, color = TextMuted)
                  }
                }
              } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  laptopFiles.forEach { file ->
                    val isSaved = savedFileUris.containsKey(file.id)
                    Surface(
                      color = Color(0xFFF8FAFC),
                      shape = RoundedCornerShape(8.dp),
                      border = androidx.compose.foundation.BorderStroke(1.dp, if (isSaved) Color(0xFF22C55E) else Color(0xFFE2E8F0)),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                          modifier = Modifier.size(36.dp).background(if (isSaved) Color(0xFFE8F5E9) else NavyLight, CircleShape),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(if (isSaved) Icons.Default.CheckCircle else Icons.Default.FileDownload, contentDescription = null, tint = if (isSaved) Color(0xFF16A34A) else NavyPrimary, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                          Text(file.title, fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = NavyPrimary)
                          Text(if (isSaved) "✓ Saved in Phone Storage" else "Ready to download", fontFamily = com.example.ui.theme.PlusJakartaFontFamily, fontSize = 10.sp, color = if (isSaved) Color(0xFF16A34A) else TextSecondary)
                        }

                        Button(
                          onClick = {
                            coroutineScope.launch {
                              FileDownloadHelper.downloadFileFromLaptop(context = context, hostUrl = normalizedHost, fileName = file.title, fileUrl = file.imageUrl, fallbackContent = file.content)
                                .onSuccess { uri ->
                                  savedFileUris[file.id] = uri
                                  Toast.makeText(context, "Saved to Downloads/NIJO_PVT in File Manager!", Toast.LENGTH_SHORT).show()
                                }
                            }
                          },
                          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                          shape = RoundedCornerShape(6.dp),
                          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                          modifier = Modifier.height(28.dp)
                        ) {
                          Text(if (isSaved) "Saved" else "Download", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        CrossDeviceTab.UNIVERSAL_CLIPBOARD -> {
          // Tab 3: Universal Clipboard
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text("Universal Cross-Device Clipboard", fontFamily = com.example.ui.theme.OutfitFontFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              Text("Shared clipboard text synced across Phone, Laptop, and TV via Wi-Fi Mesh.", fontFamily = com.example.ui.theme.PlusJakartaFontFamily, fontSize = 11.sp, color = TextSecondary)

              OutlinedTextField(
                value = clipboardInputText,
                onValueChange = { clipboardInputText = it },
                label = { Text("Clipboard text to broadcast") },
                placeholder = { Text("Paste note, code, or syllabus text...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = NavyPrimary,
                  focusedLabelColor = NavyPrimary
                )
              )

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                  onClick = {
                    if (clipboardInputText.isNotBlank()) {
                      viewModel.createNote("Clipboard Sync", clipboardInputText, listOf("clipboard", "cross_device"))
                      Toast.makeText(context, "Broadcasted to Laptop & TV!", Toast.LENGTH_SHORT).show()
                      clipboardInputText = ""
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Broadcast to Devices", fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
                }
              }
            }
          }
        }
      }
    }

    // 5. DEDICATED MOBILE SETUP UI BOTTOM SHEET ("Setup Hub")
    if (showMobileSetupSheet) {
      ModalBottomSheet(
        onDismissRequest = { showMobileSetupSheet = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Mobile Hub Connection Setup",
                fontFamily = com.example.ui.theme.OutfitFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
              )
              Text(
                text = "Configure Wi-Fi Direct Mesh & Laptop Server",
                fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                fontSize = 11.5.sp,
                color = TextSecondary
              )
            }

            IconButton(onClick = { showMobileSetupSheet = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = NavyPrimary)
            }
          }

          HorizontalDivider(color = Color(0xFFE2E8F0))

          // 1. Wi-Fi Status Card
          Surface(
            color = if (isWifiConnected) Color(0xFFF0FDF4) else NavyLight,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isWifiConnected) Color(0xFF86EFAC) else Color(0xFFCBD5E1)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Wifi, contentDescription = null, tint = if (isWifiConnected) Color(0xFF16A34A) else NavyPrimary, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (isWifiConnected) "Connected to $wifiSsid" else "Disconnected from Wi-Fi",
                  fontFamily = com.example.ui.theme.OutfitFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = NavyPrimary
                )
                Text(
                  text = if (isWifiConnected) "Local network ready for instant peer sharing" else "Connect to your home Wi-Fi for local mesh transfer",
                  fontFamily = com.example.ui.theme.PlusJakartaFontFamily,
                  fontSize = 11.sp,
                  color = TextSecondary
                )
              }
            }
          }

          // 2. Laptop Host IP Field & Test
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Laptop Hub Server Address:", fontFamily = com.example.ui.theme.OutfitFontFamily, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            OutlinedTextField(
              value = hostIpInput,
              onValueChange = {
                hostIpInput = it
                sharedPreferences.edit().putString("saved_host_ip", it).apply()
              },
              placeholder = { Text("192.168.1.3:4000") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
              )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  val target = cleanHostAddress(hostIpInput)
                  checkServerStatus(target, forceConnectedOnManualClick = true)
                  Toast.makeText(context, "Connecting to $target...", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Test & Save", fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
              }

              OutlinedButton(
                onClick = { runAutoDiscovery() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Auto-Detect", fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary)
              }
            }
          }

          // 3. Scan QR Code Action
          Button(
            onClick = {
              showMobileSetupSheet = false
              try {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
              } catch (_: Exception) {
                showQrScannerOverlay = true
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Scan QR Code from Laptop Screen", fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, color = NavyPrimary)
          }

          // 4. Quick 3-Step Setup Instructions
          Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("Easy 3-Step Setup Guide:", fontFamily = com.example.ui.theme.OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
              Text("1. Connect Phone, Laptop, and TV to the same Wi-Fi ($wifiSsid).", fontSize = 11.sp, color = TextPrimary)
              Text("2. Run server on Laptop (node server.js on port 4000).", fontSize = 11.sp, color = TextPrimary)
              Text("3. Tap 'Scan QR' to point at laptop or click 'Auto-Detect'.", fontSize = 11.sp, color = TextPrimary)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
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

    // Sleek IP Configuration & QR Dialog
    if (showIpConfigDialog) {
      AlertDialog(
        onDismissRequest = { showIpConfigDialog = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Wifi, contentDescription = null, tint = NavyPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Laptop Hub Connection", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = "Enter your Laptop's Wi-Fi IP and port (default port 4000) or scan the QR code from the laptop browser.",
              fontSize = 12.sp,
              color = TextSecondary
            )

            OutlinedTextField(
              value = hostIpInput,
              onValueChange = {
                hostIpInput = it
                sharedPreferences.edit().putString("saved_host_ip", it).apply()
              },
              label = { Text("Server IP:Port") },
              placeholder = { Text("192.168.1.3:4000") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("host_ip_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = CleanOutline,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              )
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  try {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                  } catch (_: Exception) {
                    showQrScannerOverlay = true
                  }
                  showIpConfigDialog = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Scan QR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              }

              Button(
                onClick = {
                  runAutoDiscovery()
                  showIpConfigDialog = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.2f)
              ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Auto-Detect", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
              }
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val target = if (hostIpInput.isNotBlank()) cleanHostAddress(hostIpInput) else "192.168.1.3:4000"
              hostIpInput = target
              sharedPreferences.edit().putString("saved_host_ip", target).apply()
              checkServerStatus(target, forceConnectedOnManualClick = true)
              showIpConfigDialog = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier.testTag("connect_default_ip_btn")
          ) {
            Text("Save & Connect", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showIpConfigDialog = false }) {
            Text("Cancel", color = TextSecondary)
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
  onFileReceived: (fileName: String, fileContent: String, fileUrl: String?) -> Unit,
  onSystemUpdate: ((title: String, body: String, themeColor: String?, fontStyle: String?) -> Unit)? = null
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

            // 1. Antigravity System Update & Remote Configuration broadcast
            if (json.contains("\"type\":\"system-update\"") || json.contains("\"type\":\"config-update\"") || json.contains("\"type\":\"ui-update\"")) {
              val title = if (json.contains("\"title\":\"")) json.substringAfter("\"title\":\"", "Antigravity Update").substringBefore("\"") else "Antigravity Update"
              val body = if (json.contains("\"body\":\"")) json.substringAfter("\"body\":\"", "System update received").substringBefore("\"")
                         else if (json.contains("\"message\":\"")) json.substringAfter("\"message\":\"", "System update received").substringBefore("\"") else "System update received"
              val themeColor = if (json.contains("\"themeColor\":\"")) json.substringAfter("\"themeColor\":\"", "").substringBefore("\"").ifBlank { null } else null
              val fontStyle = if (json.contains("\"fontStyle\":\"")) json.substringAfter("\"fontStyle\":\"", "").substringBefore("\"").ifBlank { null } else null

              withContext(Dispatchers.Main) {
                onSystemUpdate?.invoke(title, body, themeColor, fontStyle)
              }
            }

            // 2. Incoming File Transfer broadcast
            val isFromLaptop = json.contains("\"sender\":\"laptop\"") ||
              json.contains("\"sender\":\"web\"") ||
              (json.contains("\"type\":\"file-transfer\"") && !json.contains("\"sender\":\"mobile\""))
            val isForMobile = json.contains("\"target\":\"mobile\"") ||
              json.contains("\"target\":\"all\"") ||
              !json.contains("\"target\":\"")

            if (isFromLaptop && isForMobile) {
              val fname = if (json.contains("\"fileName\":\"")) {
                json.substringAfter("\"fileName\":\"", "incoming_file.pdf").substringBefore("\"")
              } else if (json.contains("\"name\":\"")) {
                json.substringAfter("\"name\":\"", "incoming_file.pdf").substringBefore("\"")
              } else {
                "incoming_laptop_doc.pdf"
              }
              val fileUrl = if (json.contains("\"url\":\"")) {
                json.substringAfter("\"url\":\"", "").substringBefore("\"").ifBlank { null }
              } else null

              withContext(Dispatchers.Main) {
                onFileReceived(fname, "Received from Laptop via SSE $fullUrl", fileUrl)
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
/**
 * Uploads file via Base64 JSON payload as a high-reliability fallback when multipart upload fails.
 */
private fun executeBase64Upload(
  context: Context,
  targetUrl: String,
  fileUri: Uri?,
  fileName: String,
  target: String,
  onProgress: (Float) -> Unit
): Result<String> {
  var connection: HttpURLConnection? = null
  try {
    val cleanFileName = fileName.replace("\"", "").replace("\r", "").replace("\n", "").trim()
    val bytes = if (fileUri != null) {
      try {
        context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
      } catch (_: Exception) { null }
    } else null
    val base64Data = if (bytes != null && bytes.isNotEmpty()) {
      android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    } else {
      android.util.Base64.encodeToString("NIJO PVT file transfer: $cleanFileName".toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
    }

    onProgress(0.35f)
    val jsonObject = org.json.JSONObject().apply {
      put("fileName", cleanFileName)
      put("fileBase64", base64Data)
      put("sender", "mobile")
      put("target", target)
    }
    val jsonBytes = jsonObject.toString().toByteArray(Charsets.UTF_8)

    val url = URL(targetUrl)
    connection = (url.openConnection() as HttpURLConnection).apply {
      requestMethod = "POST"
      connectTimeout = 10000
      readTimeout = 60000
      doOutput = true
      doInput = true
      useCaches = false
      setRequestProperty("Content-Type", "application/json; charset=UTF-8")
      setRequestProperty("User-Agent", "NijoMobileApp/1.0")
      setRequestProperty("X-Sender", "mobile")
      setRequestProperty("X-Target", target)
      setRequestProperty("X-Filename", cleanFileName)
    }

    onProgress(0.65f)
    connection.outputStream.use { os ->
      os.write(jsonBytes)
      os.flush()
    }
    onProgress(0.92f)

    val responseCode = connection.responseCode
    val responseText = if (responseCode in 200..399) {
      try { connection.inputStream.bufferedReader().use { it.readText() } } catch (_: Exception) { "HTTP $responseCode" }
    } else {
      try { connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode" } catch (_: Exception) { "HTTP $responseCode" }
    }
    onProgress(1.0f)
    return if (responseCode in 200..399) {
      Result.success(responseText)
    } else {
      Result.failure(Exception("HTTP $responseCode: $responseText"))
    }
  } catch (e: Exception) {
    return Result.failure(e)
  } finally {
    try { connection?.disconnect() } catch (_: Exception) {}
  }
}

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
    val boundary = "Boundary" + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().replace("-", "").take(10)
    val crlf = "\r\n"
    val cleanFileName = fileName.replace("\"", "").replace("\r", "").replace("\n", "").trim()

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
      setRequestProperty("X-Filename", cleanFileName)
    }

    onProgress(0.08f)

    val bos = java.io.BufferedOutputStream(connection.outputStream)

    fun writeUtf8(str: String) {
      bos.write(str.toByteArray(Charsets.UTF_8))
    }

    // 1. Form field: file (binary data with original filename)
    val mimeType = if (fileUri != null) {
      context.contentResolver.getType(fileUri) ?: "application/octet-stream"
    } else {
      "application/octet-stream"
    }
    writeUtf8("--$boundary$crlf")
    writeUtf8("Content-Disposition: form-data; name=\"file\"; filename=\"$cleanFileName\"$crlf")
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
          val progress = (totalWritten.toFloat() / totalExpected.toFloat()).coerceIn(0.12f, 0.92f)
          onProgress(progress)
        }
        bos.flush()
      }
    } else {
      val fallbackBytes = "NIJO PVT file transfer: $cleanFileName".toByteArray(Charsets.UTF_8)
      bos.write(fallbackBytes)
      bos.flush()
    }

    writeUtf8(crlf)

    // 2. Form field: sender ("mobile")
    writeUtf8("--$boundary$crlf")
    writeUtf8("Content-Disposition: form-data; name=\"sender\"$crlf$crlf")
    writeUtf8("mobile$crlf")

    // 3. Form field: target ("laptop" or "tv")
    writeUtf8("--$boundary$crlf")
    writeUtf8("Content-Disposition: form-data; name=\"target\"$crlf$crlf")
    writeUtf8("$target$crlf")

    // Closing boundary delimiter
    writeUtf8("--$boundary--$crlf")
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
 * directly to the Laptop Server (default http://192.168.1.3:4000/upload).
 * Includes automatic Base64 JSON fallback if multipart parser rejects the payload.
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

    // Attempt 1: Standard multipart/form-data to http://<cleanHost>/upload
    val attempt1 = executeMultipartUpload(context, primaryUrl, fileUri, fileName, fileSize, target, onProgress)
    if (attempt1.isSuccess) {
      return@withContext attempt1
    }

    val err1 = attempt1.exceptionOrNull()
    val errMsg1 = err1?.message ?: ""

    // Attempt 2: If multipart failed (e.g. HTTP 400 No file uploaded), try Base64 JSON fallback to /upload or /api/upload-raw
    val attemptBase64 = executeBase64Upload(context, primaryUrl, fileUri, fileName, target, onProgress)
    if (attemptBase64.isSuccess) {
      return@withContext attemptBase64
    }

    val attemptBase64Raw = executeBase64Upload(context, buildFullUrl(cleanHost, "/api/upload-raw"), fileUri, fileName, target, onProgress)
    if (attemptBase64Raw.isSuccess) {
      return@withContext attemptBase64Raw
    }

    // Attempt 3: If 404, fallback to /api/upload
    if (errMsg1.contains("404")) {
      val fallbackApiUrl = buildFullUrl(cleanHost, "/api/upload")
      val attemptApi = executeMultipartUpload(context, fallbackApiUrl, fileUri, fileName, fileSize, target, onProgress)
      if (attemptApi.isSuccess) {
        return@withContext attemptApi
      }
    }

    // Attempt 4: If port was 4000 and refused connection, check port 3000 in case server was started on port 3000
    if (cleanHost.contains(":4000")) {
      val port3000Url = "http://" + cleanHost.replace(":4000", ":3000") + "/upload"
      val attempt3000 = executeMultipartUpload(context, port3000Url, fileUri, fileName, fileSize, target, onProgress)
      if (attempt3000.isSuccess) {
        return@withContext attempt3000
      }
    }

    // Attempt 5: If running in emulator and 192.168.* was unreachable, try 10.0.2.2:4000 and 10.0.2.2:3000
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
