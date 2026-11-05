package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.SlateOutline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import com.example.ui.viewmodel.PhotoAnalysisUiState
import com.example.ui.viewmodel.PhotoAnalysisViewModel

@Composable
fun PhotoAnalysisDialog(
  viewModel: PhotoAnalysisViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val saveStatus by viewModel.saveStatus.collectAsState()

  var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var promptText by remember { mutableStateOf("Analyze this image thoroughly with deep reasoning, identify key elements, extract text, and list actionable next steps.") }
  var noteTitle by remember { mutableStateOf("Photo Inspection Notes") }

  // Zero-permission Android Photo Picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let {
      try {
        context.contentResolver.openInputStream(it)?.use { stream ->
          val rawBitmap = BitmapFactory.decodeStream(stream)
          if (rawBitmap != null) {
            // Scale if needed to keep under 1600px
            val maxDim = 1600
            val scaled = if (rawBitmap.width > maxDim || rawBitmap.height > maxDim) {
              val ratio = minOf(maxDim.toFloat() / rawBitmap.width, maxDim.toFloat() / rawBitmap.height)
              Bitmap.createScaledBitmap(rawBitmap, (rawBitmap.width * ratio).toInt(), (rawBitmap.height * ratio).toInt(), true)
            } else {
              rawBitmap
            }
            selectedBitmap = scaled
            viewModel.reset()
          }
        }
      } catch (e: Exception) {
        // Handle gracefully
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Psychology, contentDescription = null, tint = VividViolet, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Gemini 4 Argon Vision", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Image Picker Area
        if (selectedBitmap != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, SlateOutline, RoundedCornerShape(12.dp))
          ) {
            Image(
              bitmap = selectedBitmap!!.asImageBitmap(),
              contentDescription = "Selected Photo",
              modifier = Modifier.fillMaxWidth(),
              contentScale = ContentScale.Crop
            )

            Button(
              onClick = {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .height(34.dp)
            ) {
              Text("Change", fontSize = 11.sp, color = ElectricCyan)
            }
          }
        } else {
          Surface(
            color = SlateSurfaceVariant,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
              .border(1.dp, SlateOutline, RoundedCornerShape(14.dp)),
            onClick = {
              photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
          ) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(6.dp))
              Text("Tap to choose photo from device", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
              Text("Zero-permission secure Android Photo Picker", fontSize = 10.sp, color = TextMuted)
            }
          }
        }

        // Custom Prompt Input
        OutlinedTextField(
          value = promptText,
          onValueChange = { promptText = it },
          label = { Text("Analysis Instructions", fontSize = 12.sp) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("photo_prompt_input"),
          maxLines = 3,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VividViolet,
            unfocusedBorderColor = SlateOutline,
            focusedContainerColor = SlateSurface,
            unfocusedContainerColor = SlateSurface,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        // Action Trigger
        Button(
          onClick = {
            selectedBitmap?.let { bmp ->
              viewModel.analyzePhoto(bmp, promptText)
            }
          },
          enabled = selectedBitmap != null && uiState !is PhotoAnalysisUiState.Analyzing,
          colors = ButtonDefaults.buttonColors(containerColor = VividViolet, contentColor = Color.White),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("analyze_photo_button")
        ) {
          if (uiState is PhotoAnalysisUiState.Analyzing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Gemini 4 Argon High Thinking...", fontSize = 13.sp)
            }
          } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Analyze with Gemini 4 Argon", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }

        // Save Status Feedback
        if (!saveStatus.isNullOrBlank()) {
          Surface(
            color = Color(0xFF0F3224),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = saveStatus ?: "",
              color = NeonMint,
              fontSize = 12.sp,
              modifier = Modifier.padding(10.dp)
            )
          }
        }

        // Results Section
        when (val state = uiState) {
          is PhotoAnalysisUiState.Analyzing -> {
            Surface(
              color = SlateSurfaceVariant,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                CircularProgressIndicator(color = VividViolet, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Engaging Gemini 4 Argon with ThinkingLevel.HIGH...", fontSize = 12.sp, color = VividViolet)
                Text("Analyzing multimodal visual features and deep concepts", fontSize = 10.sp, color = TextMuted)
              }
            }
          }

          is PhotoAnalysisUiState.Error -> {
            Surface(
              color = Color(0xFF381515),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Error: ${state.message}",
                color = Color(0xFFFF8A80),
                fontSize = 12.sp,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          is PhotoAnalysisUiState.Success -> {
            val result = state.result
            Surface(
              color = SlateSurfaceVariant,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateOutline, RoundedCornerShape(14.dp))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                // Header badge
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .background(VividViolet.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text("gemini-4-argon • High Thinking", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VividViolet)
                  }
                }

                // Thinking summary trace
                if (!result.thinkingSummary.isNullOrBlank()) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Surface(
                    color = Color(0xFF1B172E),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                      Text("Reasoning Trace Summary:", fontSize = 10.sp, color = VividViolet, fontWeight = FontWeight.Bold)
                      Text(result.thinkingSummary, fontSize = 11.sp, color = Color(0xFFD1C4E9), lineHeight = 15.sp)
                    }
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Analysis Result:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = result.text,
                  fontSize = 13.sp,
                  color = TextPrimary,
                  lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Save as note section
                OutlinedTextField(
                  value = noteTitle,
                  onValueChange = { noteTitle = it },
                  label = { Text("Note Title for Sync", fontSize = 11.sp) },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                  onClick = {
                    viewModel.saveAsSyncedNote(
                      title = noteTitle,
                      analysisText = result.text,
                      userPrompt = promptText
                    )
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color(0xFF062A19)),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_analysis_to_workspace_button")
                ) {
                  Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Save as Synced Note", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }
            }
          }

          else -> {
            // Idle state
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", color = ElectricCyan)
      }
    },
    containerColor = SlateSurface,
    shape = RoundedCornerShape(20.dp)
  )
}
