package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.StudioTealLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.WorkspaceViewModel

data class StudySubjectItem(
  val id: String,
  val name: String,
  val icon: ImageVector
)

data class StudyTopic(
  val subjectId: String,
  val unit: String,
  val title: String,
  val conceptNote: String,
  val formula: String,
  val examTip: String
)

data class MockQuestion(
  val question: String,
  val subject: String,
  val options: List<String>,
  val correctOptionIndex: Int,
  val explanation: String
)

@Composable
fun StudyScreen(
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val subjects = listOf(
    StudySubjectItem("MATHS", "Engineering Maths I", Icons.Default.Calculate),
    StudySubjectItem("CHEMISTRY", "Applied Chemistry", Icons.Default.Science),
    StudySubjectItem("PSPP", "PSPP (Python & Problem Solving)", Icons.Default.Code),
    StudySubjectItem("PHYSICS", "Applied Physics", Icons.Default.Lightbulb),
    StudySubjectItem("MOCK", "Daily Mock Test", Icons.Default.Quiz)
  )

  var selectedSubjectId by remember { mutableStateOf("MATHS") }
  var searchQuery by remember { mutableStateOf("") }
  var flippedTopicTitle by remember { mutableStateOf<String?>(null) }

  // Mock Test State
  var currentQuestionIndex by remember { mutableIntStateOf(0) }
  var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
  var score by remember { mutableIntStateOf(0) }

  val mockQuestions = remember {
    listOf(
      MockQuestion(
        question = "What condition must be satisfied to solve linear equations using Cramer's Rule?",
        subject = "Engineering Maths I",
        options = listOf("Determinant |D| = 0", "Determinant |D| != 0", "Matrix must be singular", "Rank = 0"),
        correctOptionIndex = 1,
        explanation = "Cramer's Rule requires the coefficient matrix determinant D != 0 so division by D is defined: x = Dx / D."
      ),
      MockQuestion(
        question = "Which indicator is used in EDTA titration to determine water hardness?",
        subject = "Applied Chemistry",
        options = listOf("Phenolphthalein", "Methyl Orange", "Eriochrome Black T (EBT)", "Litmus Paper"),
        correctOptionIndex = 2,
        explanation = "Eriochrome Black T (EBT) is used at pH 10 buffer. Color changes from wine red to steel blue."
      ),
      MockQuestion(
        question = "In PSPP (Python), which data type is immutable?",
        subject = "PSPP",
        options = listOf("List", "Dictionary", "Tuple", "Set"),
        correctOptionIndex = 2,
        explanation = "Tuples in Python cannot be modified after creation, making them immutable."
      ),
      MockQuestion(
        question = "What is the barrier potential for a Silicon P-N junction diode at room temperature?",
        subject = "Applied Physics",
        options = listOf("0.3 V", "0.7 V", "1.2 V", "2.0 V"),
        correctOptionIndex = 1,
        explanation = "Silicon diodes have a built-in barrier potential of approximately 0.7 V (Germanium is 0.3 V)."
      )
    )
  }

  val studyTopics = remember {
    listOf(
      // Engineering Maths I
      StudyTopic(
        subjectId = "MATHS",
        unit = "Unit 1: Matrices & Determinants",
        title = "Matrix Inverse & Cramer's Rule",
        conceptNote = "To solve linear system AX = B, calculate determinant |A|. If |A| != 0, compute adjoint matrix. For Cramer's rule, substitute column B into columns to find Dx, Dy, Dz.",
        formula = "A^(-1) = adj(A) / |A|  |  x = Dx / D,  y = Dy / D,  z = Dz / D",
        examTip = "Frequently asked 8-mark question. Always calculate |A| first and state it is non-zero."
      ),
      StudyTopic(
        subjectId = "MATHS",
        unit = "Unit 2: Trigonometry",
        title = "Compound Angles & Double Angle Formulas",
        conceptNote = "Master essential compound angle identities for calculus derivations and vector projections.",
        formula = "sin(A ± B) = sinA cosB ± cosA sinB  |  cos(2A) = cos^2(A) - sin^2(A) = 1 - 2sin^2(A)",
        examTip = "Directly used in proving trigonometric LHS = RHS identity problems."
      ),
      StudyTopic(
        subjectId = "MATHS",
        unit = "Unit 3: Vector Algebra",
        title = "Scalar (Dot) & Vector (Cross) Product",
        conceptNote = "Dot product results in a scalar magnitude (work done). Cross product yields a perpendicular normal vector (torque/moment).",
        formula = "A . B = |A||B| cos(θ) = AxBx + AyBy + AzBz  |  Perpendicular when A . B = 0",
        examTip = "Find missing scalar 'p' for two perpendicular vectors by equating their dot product to 0."
      ),

      // Applied Chemistry
      StudyTopic(
        subjectId = "CHEMISTRY",
        unit = "Unit 1: Water Technology",
        title = "Water Hardness & EDTA Estimation",
        conceptNote = "Hardness caused by dissolved Ca2+ and Mg2+ salts. Temporary hardness (bicarbonates) removed by boiling. Permanent hardness (chlorides/sulfates) removed via ion-exchange or soda-lime.",
        formula = "Total Hardness = (Volume of EDTA * Molarity of EDTA * 1000) / Volume of Sample",
        examTip = "State buffer solution used (NH4Cl + NH4OH at pH 10) and end-point color change."
      ),
      StudyTopic(
        subjectId = "CHEMISTRY",
        unit = "Unit 2: Polymers & Plastics",
        title = "Thermoplastics vs Thermosetting Plastics",
        conceptNote = "Thermoplastics soften on heating and can be remolded (PVC, Polyethylene). Thermosetting plastics undergo permanent chemical cross-linking and cannot be remolded (Bakelite, Melamine).",
        formula = "Bakelite = Phenol + Formaldehyde (Condensation Polymerization)",
        examTip = "Standard 4-mark comparison table question in end-semester exams."
      ),
      StudyTopic(
        subjectId = "CHEMISTRY",
        unit = "Unit 3: Electrochemistry",
        title = "Galvanic Cells & Corrosion Prevention",
        conceptNote = "Oxidation occurs at the anode; reduction occurs at the cathode. Prevent metallic rust using sacrificial zinc anodes (galvanization) or cathodic impressed currents.",
        formula = "E_cell = E_cathode - E_anode  |  Rust formula: Fe2O3 . xH2O",
        examTip = "Draw neat galvanic cell schematic and label electron flow vs conventional current."
      ),

      // PSPP (Problem Solving & Python Programming)
      StudyTopic(
        subjectId = "PSPP",
        unit = "Unit 1: Python Fundamentals & Data Types",
        title = "Control Structures, Lists & Slicing",
        conceptNote = "Sequential execution, conditional branching (if-elif-else), and loops (for, while). List slicing syntax array[start:stop:step] facilitates array processing without external libraries.",
        formula = "arr[start:end:step]  |  len(arr),  arr.append(val),  arr.sort()",
        examTip = "Write clean syntax with consistent 4-space indentation for code questions."
      ),
      StudyTopic(
        subjectId = "PSPP",
        unit = "Unit 2: Functions, NumPy & Modules",
        title = "Modular Programming & NumPy Vectors",
        conceptNote = "Decompose programs into reusable def functions with parameter passing and return values. NumPy ndarray provides contiguous C-speed array vectorization.",
        formula = "import numpy as np; a = np.array([1, 2, 3]); np.mean(a), np.dot(a, b)",
        examTip = "Laboratory exam requires writing NumPy code for matrix multiplication and statistics."
      ),

      // Applied Physics
      StudyTopic(
        subjectId = "PHYSICS",
        unit = "Unit 1: Semiconductor Physics",
        title = "P-N Junction & Rectification",
        conceptNote = "Depletion layer formed by carrier recombination. Low resistance in forward bias; high resistance in reverse bias. Rectifiers convert AC to DC.",
        formula = "Half-Wave Efficiency = 40.6%  |  Full-Wave Center-Tap / Bridge = 81.2%",
        examTip = "Draw circuit diagram for Bridge Rectifier showing current paths during positive and negative cycles."
      )
    )
  }

  val filteredTopics = studyTopics.filter { topic ->
    topic.subjectId == selectedSubjectId &&
      (searchQuery.isBlank() ||
        topic.title.contains(searchQuery, ignoreCase = true) ||
        topic.conceptNote.contains(searchQuery, ignoreCase = true) ||
        topic.unit.contains(searchQuery, ignoreCase = true))
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
      // Header Card
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = NavyPrimary),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("study_header_card")
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
                  Icon(Icons.Default.Book, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Study & Exam Preparation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Text(
                    text = "Engineering Maths I, Applied Chemistry, PSPP & Physics",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Simplified step-by-step formula cards, exam tips, and daily mock tests to master your semester subjects with confidence.",
              fontSize = 12.sp,
              color = Color.White.copy(alpha = 0.9f),
              lineHeight = 17.sp
            )
          }
        }
      }

      // Subject Selector Tabs
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(subjects) { subject ->
            val isSelected = subject.id == selectedSubjectId
            FilterChip(
              selected = isSelected,
              onClick = { selectedSubjectId = subject.id },
              label = {
                Text(
                  text = subject.name,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              },
              leadingIcon = {
                Icon(imageVector = subject.icon, contentDescription = null, modifier = Modifier.size(15.dp))
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NavyLight,
                selectedLabelColor = NavyPrimary,
                selectedLeadingIconColor = NavyPrimary,
                containerColor = CleanSurface,
                labelColor = TextSecondary
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = if (isSelected) NavyPrimary else CleanOutline,
                borderWidth = if (isSelected) 1.5.dp else 1.dp,
                enabled = true,
                selected = isSelected
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("study_chip_${subject.id}")
            )
          }
        }
      }

      // If Daily Mock Test Tab is Selected
      if (selectedSubjectId == "MOCK") {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CleanSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, CleanOutline, RoundedCornerShape(14.dp))
              .testTag("daily_mock_test_card")
          ) {
            val q = mockQuestions[currentQuestionIndex]
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  color = NavyLight,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "Question ${currentQuestionIndex + 1} of ${mockQuestions.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                Text(
                  text = "Score: $score",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavyPrimary
                )
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "[${q.subject}] ${q.question}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Options
              q.options.forEachIndexed { optIdx, optText ->
                val isSelected = selectedAnswerIndex == optIdx
                val isCorrect = optIdx == q.correctOptionIndex
                val showResult = selectedAnswerIndex != null

                Surface(
                  color = when {
                    showResult && isCorrect -> NeonMint.copy(alpha = 0.15f)
                    showResult && isSelected -> CleanSurfaceVariant
                    else -> CleanSurfaceVariant
                  },
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                      showResult && isCorrect -> NeonMint
                      isSelected -> NavyPrimary
                      else -> CleanOutline
                    }
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = selectedAnswerIndex == null) {
                      selectedAnswerIndex = optIdx
                      if (optIdx == q.correctOptionIndex) {
                        score++
                        Toast.makeText(context, "Correct Answer!", Toast.LENGTH_SHORT).show()
                      }
                    }
                    .padding(vertical = 4.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "${('A'.code + optIdx).toChar()}.",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = optText,
                      fontSize = 12.sp,
                      color = TextPrimary
                    )
                  }
                }
              }

              // Explanation & Next Button
              if (selectedAnswerIndex != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                  color = NavyLight,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "Explanation: ${q.explanation}",
                    fontSize = 11.5.sp,
                    color = NavyPrimary,
                    modifier = Modifier.padding(10.dp)
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                  onClick = {
                    if (currentQuestionIndex + 1 < mockQuestions.size) {
                      currentQuestionIndex++
                      selectedAnswerIndex = null
                    } else {
                      currentQuestionIndex = 0
                      selectedAnswerIndex = null
                      Toast.makeText(context, "Mock test completed! Final score: $score", Toast.LENGTH_LONG).show()
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = if (currentQuestionIndex + 1 < mockQuestions.size) "Next Question" else "Restart Quiz",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      } else {
        // Search Box
        item {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search topics or formulas...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("study_search_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = CleanSurface,
              unfocusedContainerColor = CleanSurface,
              focusedBorderColor = NavyPrimary,
              unfocusedBorderColor = CleanOutline
            ),
            singleLine = true
          )
        }

        // Topics List
        items(filteredTopics) { topic ->
          val isFlipped = flippedTopicTitle == topic.title

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CleanSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, CleanOutline, RoundedCornerShape(12.dp))
              .testTag("topic_card_${topic.title.take(8)}")
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              // Unit label & Flip button
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = topic.unit,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavySecondary
                )

                Surface(
                  color = CleanSurfaceVariant,
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.clickable {
                    flippedTopicTitle = if (isFlipped) null else topic.title
                  }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Flip", tint = NavyPrimary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = if (isFlipped) "Concept" else "Formula",
                      fontSize = 10.sp,
                      color = NavyPrimary,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = topic.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )

              Spacer(modifier = Modifier.height(6.dp))

              if (!isFlipped) {
                Text(
                  text = topic.conceptNote,
                  fontSize = 12.sp,
                  color = TextSecondary,
                  lineHeight = 17.sp
                )
              } else {
                Surface(
                  color = NavyLight,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                      text = "Key Formula:",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = topic.formula,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextPrimary
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Exam tip
              Surface(
                color = CleanSurfaceVariant,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = "Exam Tip: ${topic.examTip}",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Medium,
                  color = TextSecondary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Add to Study Task
              Button(
                onClick = {
                  val studyTask = WorkspaceItem(
                    type = ItemType.TASK.name,
                    title = "Revise: ${topic.title}",
                    content = "${topic.unit} - Key concept revision",
                    priority = ItemPriority.HIGH.name,
                    tags = listOf("study", selectedSubjectId.lowercase())
                  )
                  viewModel.createItem(studyTask)
                  Toast.makeText(context, "Added to study timetable: ${topic.title}", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("add_to_timetable_btn")
              ) {
                Icon(Icons.Default.AddAlarm, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Study Timetable", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}
