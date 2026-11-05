package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemPriority
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
import com.example.ui.theme.StudioTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.WorkspaceViewModel

data class DesignArchiveItem(
  val title: String,
  val client: String,
  val fileType: String,
  val fileSize: String,
  val completedDate: String,
  val status: String
)

data class InstagramTrendIdea(
  val title: String,
  val category: String,
  val hookFormula: String,
  val layoutNotes: String
)

data class AiVideoPromptItem(
  val tool: String,
  val title: String,
  val promptText: String,
  val cameraMotion: String
)

@Composable
fun FreelanceDesignScreen(
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var activeSection by remember { mutableStateOf("FOLDERS") } // "FOLDERS", "TRENDS", "ARCHIVES", "PROMPTS"

  val archives = remember {
    listOf(
      DesignArchiveItem(
        title = "Libra Brand Guidelines & Vector Kit",
        client = "Libra",
        fileType = "AI / SVG / PDF",
        fileSize = "48.2 MB",
        completedDate = "Oct 28, 2026",
        status = "Completed"
      ),
      DesignArchiveItem(
        title = "SMM Promotional Motion Reel (60fps)",
        client = "Libra SMM",
        fileType = "MP4 (1080x1920)",
        fileSize = "94.5 MB",
        completedDate = "Nov 01, 2026",
        status = "Synced to Host"
      ),
      DesignArchiveItem(
        title = "Instagram Carousel Pack - 10 Slides",
        client = "Tech SMM",
        fileType = "PNG (1080x1350)",
        fileSize = "18.4 MB",
        completedDate = "Oct 22, 2026",
        status = "Archived"
      )
    )
  }

  val instagramTrends = remember {
    listOf(
      InstagramTrendIdea(
        title = "Kinetic Typography Hook Carousel",
        category = "Instagram Carousel",
        hookFormula = "Slide 1: 'Stop making this 1 typography mistake' (Bold, Sans-Serif, High Contrast).",
        layoutNotes = "Aspect ratio: 4:5 (1080x1350). Grid padding 80px. Swipe arrows on bottom right."
      ),
      InstagramTrendIdea(
        title = "Minimalist Clean Aesthetic Reel",
        category = "Reels / Shorts",
        hookFormula = "First 1.5 seconds: Rapid 3-clip jump cut with soft ambient electronic beat.",
        layoutNotes = "Aspect ratio: 9:16 (1080x1920). Safe margins: top 220px, bottom 360px."
      ),
      InstagramTrendIdea(
        title = "Glassmorphic 3D Product Showcase",
        category = "Brand Post",
        hookFormula = "Center floating product mockup on clean white pearl background with soft directional shadow.",
        layoutNotes = "Aspect ratio: 1:1 (1080x1080). Subtle navy gradient border."
      )
    )
  }

  val videoPrompts = remember {
    listOf(
      AiVideoPromptItem(
        tool = "Runway Gen-3",
        title = "Studio Product Commercial Reveal",
        promptText = "Commercial studio lighting, floating luxury perfume glass bottle with iridescent liquid reflection, slow orbital pan, soft volumetric light rays, hyper-photorealistic 8K.",
        cameraMotion = "Slow 360 Orbital Pan + Gentle Zoom In"
      ),
      AiVideoPromptItem(
        tool = "Kling AI",
        title = "Futuristic Kerala Drone Sequence",
        promptText = "Aerial drone shot over lush Alappuzha backwaters at golden hour twilight, traditional houseboat with glowing cyber accents, smooth cinematic glide.",
        cameraMotion = "High-altitude Forward Push-In"
      ),
      AiVideoPromptItem(
        tool = "Sora AI",
        title = "Macro Kinetic Motion Branding",
        promptText = "Macro lens capture of chrome 3D letters 'LIBRA' floating in viscous fluid, studio strobes refracting, ultra slow motion 120fps.",
        cameraMotion = "Extreme Close-Up Macro Rack Focus"
      )
    )
  }

  fun copyToClipboard(label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied: $label", Toast.LENGTH_SHORT).show()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CleanBackground)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Freelance Studio Header Card (Navy Blue & White)
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = NavyPrimary),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("freelance_hero_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Palette, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Freelance Designer Studio",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Text(
                    text = "Nijo • Graphic Design & SMM Management",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                  )
                }
              }

              Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "Client: Libra",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Manage Libra client folders, trending Instagram layouts, project archives, and AI video prompt libraries in clean Navy Blue styling.",
              fontSize = 12.sp,
              color = Color.White.copy(alpha = 0.9f),
              lineHeight = 17.sp
            )
          }
        }
      }

      // Section Navigation Tabs
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val sections = listOf(
            Pair("FOLDERS", "Folders"),
            Pair("TRENDS", "Instagram Trends"),
            Pair("ARCHIVES", "Archives"),
            Pair("PROMPTS", "AI Prompts")
          )

          sections.forEach { (sId, sLabel) ->
            val isSelected = activeSection == sId
            Surface(
              color = if (isSelected) NavyLight else CleanSurface,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) NavyPrimary else CleanOutline
              ),
              modifier = Modifier
                .weight(1f)
                .clickable { activeSection = sId }
                .testTag("design_tab_${sId.lowercase()}")
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              ) {
                Text(
                  text = sLabel,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) NavyPrimary else TextSecondary
                )
              }
            }
          }
        }
      }

      // Section Content
      when (activeSection) {
        "FOLDERS" -> {
          item {
            ClientProjectFolderCard(
              title = "Libra Brand Master Kit",
              description = "Core logo variations, brand guideline decks, typography pairings, vector SVG assets, and social post templates.",
              fileCount = "14 assets",
              onAddTask = {
                val item = WorkspaceItem(
                  type = ItemType.TASK.name,
                  title = "Libra Deliverable: Reel Cover & Template",
                  content = "Export final 9:16 cover template",
                  priority = ItemPriority.HIGH.name,
                  tags = listOf("libra", "design", "client")
                )
                viewModel.createItem(item)
                Toast.makeText(context, "Added task to Libra folder", Toast.LENGTH_SHORT).show()
              }
            )
          }

          item {
            ClientProjectFolderCard(
              title = "SMM Reels & Storyboards",
              description = "Short-form video scripts, trending audio pairings, caption copy, and thumbnail exports.",
              fileCount = "8 active reels",
              onAddTask = {
                val item = WorkspaceItem(
                  type = ItemType.TASK.name,
                  title = "Draft SMM Carousel Script",
                  content = "Write 5-slide educational copy",
                  priority = ItemPriority.MEDIUM.name,
                  tags = listOf("smm", "reels", "design")
                )
                viewModel.createItem(item)
                Toast.makeText(context, "Added SMM task", Toast.LENGTH_SHORT).show()
              }
            )
          }
        }

        "TRENDS" -> {
          item {
            Text(
              text = "Instagram Design Trends & Hook Layouts",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
          }

          items(instagramTrends) { trend ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = CleanSurface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CleanOutline, RoundedCornerShape(12.dp))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(trend.title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                  Surface(color = NavyLight, shape = RoundedCornerShape(4.dp)) {
                    Text(trend.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Hook Formula: ${trend.hookFormula}", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Layout Specs: ${trend.layoutNotes}", fontSize = 11.sp, color = TextMuted)
              }
            }
          }
        }

        "ARCHIVES" -> {
          items(archives) { archive ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = CleanSurface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CleanOutline, RoundedCornerShape(12.dp))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(34.dp)
                        .background(NavyLight, RoundedCornerShape(6.dp)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.CloudDone, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                      Text(text = archive.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                      Text(text = "${archive.client} • ${archive.fileType}", fontSize = 11.sp, color = TextSecondary)
                    }
                  }

                  Surface(color = NavyLight, shape = RoundedCornerShape(4.dp)) {
                    Text(archive.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = "Completed: ${archive.completedDate} • ${archive.fileSize}", fontSize = 10.5.sp, color = TextMuted)
                  OutlinedButton(
                    onClick = { copyToClipboard(archive.title, "host://nijo_laptop/workspace/archives/${archive.title.lowercase().replace(" ", "_")}") },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Text("Copy Host Path", fontSize = 10.sp, color = NavyPrimary)
                  }
                }
              }
            }
          }
        }

        "PROMPTS" -> {
          items(videoPrompts) { p ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = CleanSurface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CleanOutline, RoundedCornerShape(12.dp))
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = NavyLight, shape = RoundedCornerShape(4.dp)) {
                      Text(p.tool, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(p.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                  }

                  IconButton(
                    onClick = { copyToClipboard(p.title, p.promptText) },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Prompt", tint = NavyPrimary, modifier = Modifier.size(15.dp))
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                  color = CleanSurfaceVariant,
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(p.promptText, fontSize = 11.5.sp, color = TextPrimary, lineHeight = 16.sp, modifier = Modifier.padding(8.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Motion: ${p.cameraMotion}", fontSize = 10.sp, color = NavySecondary, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ClientProjectFolderCard(
  title: String,
  description: String,
  fileCount: String,
  onAddTask: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = CleanSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, CleanOutline, RoundedCornerShape(12.dp))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(NavyLight, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = fileCount, fontSize = 11.sp, color = TextMuted)
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(text = description, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)

      Spacer(modifier = Modifier.height(10.dp))
      Button(
        onClick = onAddTask,
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("add_deliverable_${title.take(5)}")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add Deliverable Task", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }
    }
  }
}
