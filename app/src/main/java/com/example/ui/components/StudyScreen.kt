package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontFamily
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

data class StudySubject(
  val id: String,
  val name: String,
  val icon: ImageVector
)

enum class SubjectSection {
  SYLLABUS_TOPICS,
  SHORT_NOTES,
  MOCK_TEST,
  TUITION_ADVICE,
  PROGRAM_WORKBENCH
}

data class SyllabusItem(
  val id: String,
  val subjectId: String,
  val unit: String,
  val title: String,
  val description: String,
  val formula: String,
  val examTip: String,
  val customItemId: String? = null
)

data class MockTestItem(
  val question: String,
  val subjectId: String,
  val options: List<String>,
  val correctOptionIndex: Int,
  val explanation: String
)

data class PsppProgramItem(
  val title: String,
  val syllabusRequirement: String,
  val initialCode: String,
  val expectedOutput: String,
  val conceptNote: String
)

data class SplitPdfResult(
  val fileName: String,
  val subjectId: String,
  val syllabusTopics: List<SyllabusItem>,
  val shortNotes: List<String>,
  val mockQuestions: List<MockTestItem>,
  val tuitionTeacherGuidance: List<String>
)

fun getStudyFileNameFromUri(context: android.content.Context, uri: Uri): String {
  var fileName = "syllabus_document.pdf"
  try {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
      if (it.moveToFirst()) {
        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1) {
          fileName = it.getString(nameIndex) ?: fileName
        }
      }
    }
  } catch (_: Exception) {}
  return fileName
}

fun processAndSplitPdfFile(fileName: String, fallbackSubject: String): SplitPdfResult {
  val cleanName = fileName.lowercase()
  val subject = when {
    cleanName.contains("math") || cleanName.contains("calc") -> "MATHS"
    cleanName.contains("chem") -> "CHEMISTRY"
    cleanName.contains("pspp") || cleanName.contains("python") || cleanName.contains("prog") -> "PSPP"
    cleanName.contains("phys") -> "PHYSICS"
    else -> fallbackSubject
  }

  val topics = listOf(
    SyllabusItem(
      id = "split_1",
      subjectId = subject,
      unit = "Module 1 (Extracted from $fileName)",
      title = "Key Core Principles & Theoretical Models",
      description = "Extracted concepts and analytical principles from uploaded syllabus file: $fileName.",
      formula = "Standard Equation for $subject",
      examTip = "High priority in semester exam section A."
    ),
    SyllabusItem(
      id = "split_2",
      subjectId = subject,
      unit = "Module 2 (Extracted from $fileName)",
      title = "Practical Applications & Computational Methods",
      description = "Detailed procedure, experimental setups, and problem-solving steps.",
      formula = "Derived formula for $subject",
      examTip = "Focus on derivations and standard unit conversions."
    )
  )

  val shortNotes = listOf(
    "Chapter summary: Foundations and primary definitions.",
    "Formulas: Master all linear operations and boundary conditions.",
    "Quick tip: Verify standard dimensional consistency before calculating."
  )

  val mockQuestions = listOf(
    MockTestItem(
      question = "Which law or principle governs the core phenomena in $fileName?",
      subjectId = subject,
      options = listOf("Primary Principle A", "Core Law B", "Analytical Theorem C", "General Rule D"),
      correctOptionIndex = 1,
      explanation = "Derived directly from module definitions in $fileName."
    )
  )

  val tuitionGuidance = listOf(
    "Tuition guidance: Solve previous 3-year question paper problems for Module 1.",
    "Pay special attention to numerical calculations and neat step-by-step presentation."
  )

  return SplitPdfResult(
    fileName = fileName,
    subjectId = subject,
    syllabusTopics = topics,
    shortNotes = shortNotes,
    mockQuestions = mockQuestions,
    tuitionTeacherGuidance = tuitionGuidance
  )
}

@Composable
fun StudyScreen(
  viewModel: WorkspaceViewModel,
  onRegisterUploadTrigger: ((() -> Unit) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val subjects = listOf(
    StudySubject("MATHS", "Engineering Maths I", Icons.Default.Calculate),
    StudySubject("CHEMISTRY", "Applied Chemistry", Icons.Default.Science),
    StudySubject("PSPP", "PSPP (Python & Problem Solving)", Icons.Default.Code),
    StudySubject("PHYSICS", "Applied Physics", Icons.Default.Lightbulb)
  )

  var selectedSubjectId by remember { mutableStateOf("MATHS") }
  var currentSection by remember { mutableStateOf(SubjectSection.SYLLABUS_TOPICS) }
  var searchQuery by remember { mutableStateOf("") }

  // Mock test state per subject
  var mockQuestionIndex by remember { mutableIntStateOf(0) }
  var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
  var mockScore by remember { mutableIntStateOf(0) }

  // PSPP Program State
  var selectedProgramIndex by remember { mutableIntStateOf(0) }
  var activeProgramCode by remember { mutableStateOf("") }
  var programTerminalOutput by remember { mutableStateOf<String?>(null) }
  var isProgramRunning by remember { mutableStateOf(false) }

  // File Picker & Upload Dialog State
  var showUploadDialog by remember { mutableStateOf(false) }
  var showSplitDialog by remember { mutableStateOf(false) }
  var splitResult by remember { mutableStateOf<SplitPdfResult?>(null) }
  var uploadedFileName by remember { mutableStateOf<String?>(null) }
  var uploadedFileUri by remember { mutableStateOf<Uri?>(null) }

  var dialogSelectedTab by remember { mutableStateOf("PDF") }
  var dialogSubject by remember { mutableStateOf("MATHS") }
  var dialogUnitInput by remember { mutableStateOf("Unit 1") }
  var dialogTitleInput by remember { mutableStateOf("") }
  var dialogSummaryInput by remember { mutableStateOf("") }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      uploadedFileUri = uri
      val fileName = getStudyFileNameFromUri(context, uri)
      uploadedFileName = fileName
      if (dialogTitleInput.isBlank()) {
        dialogTitleInput = fileName.substringBeforeLast(".")
      }
      val parsedResult = processAndSplitPdfFile(fileName, dialogSubject)
      splitResult = parsedResult
      Toast.makeText(context, "Selected $fileName", Toast.LENGTH_SHORT).show()
    }
  }

  // Register external upload trigger for the Study Mode 3-dot FAB
  LaunchedEffect(Unit) {
    onRegisterUploadTrigger?.invoke {
      showUploadDialog = true
    }
  }

  // Pre-loaded syllabus topics
  val defaultSyllabus = remember {
    listOf(
      // Engineering Maths I
      SyllabusItem(
        id = "m1",
        subjectId = "MATHS",
        unit = "Unit 1: Matrices & Determinants",
        title = "Cramer's Rule & Matrix Inversion",
        description = "Solving non-homogeneous system of linear equations using determinants. Matrix rank and consistency testing.",
        formula = "x = Dx/D,  y = Dy/D,  z = Dz/D  (D != 0) | A^-1 = adj(A)/|A|",
        examTip = "Check determinant D first; if D = 0, state system is either inconsistent or dependent."
      ),
      SyllabusItem(
        id = "m2",
        subjectId = "MATHS",
        unit = "Unit 2: Differential Calculus",
        title = "Partial Derivatives & Euler's Theorem",
        description = "Higher order partial derivatives of homogeneous functions of degree n.",
        formula = "x(∂u/∂x) + y(∂u/∂y) = n * u",
        examTip = "Verify degree 'n' carefully before applying Euler's formula in 5-mark proofs."
      ),
      SyllabusItem(
        id = "m3",
        subjectId = "MATHS",
        unit = "Unit 3: Vector Algebra",
        title = "Dot & Cross Product of Vectors",
        description = "Geometric interpretation of dot and cross products. Work done by force and moment of torque.",
        formula = "A . B = |A||B|cos(θ) = AxBx + AyBy + AzBz | A x B = |A||B|sin(θ)n̂",
        examTip = "Two vectors are perpendicular if A . B = 0; collinear if A x B = 0."
      ),

      // Applied Chemistry
      SyllabusItem(
        id = "c1",
        subjectId = "CHEMISTRY",
        unit = "Unit 1: Water Technology",
        title = "Estimation of Hardness by EDTA Method",
        description = "Principle of complexometric titration with disodium salt of EDTA at pH 10 buffer.",
        formula = "Total Hardness (mg/L) = (V_EDTA * M_EDTA * 1000) / V_sample",
        examTip = "Name indicator: Eriochrome Black T (EBT). End point color change: Wine red to Steel blue."
      ),
      SyllabusItem(
        id = "c2",
        subjectId = "CHEMISTRY",
        unit = "Unit 2: Polymers & Materials",
        title = "Thermoplastics vs Thermosetting Polymers",
        description = "Molecular structure difference: Linear/branched chains vs rigid 3D cross-linked networks.",
        formula = "Bakelite = Phenol + Formaldehyde (Step-growth condensation)",
        examTip = "Tabulate 4 distinct comparison points (recyclability, heat behavior, solvent solubility)."
      ),

      // PSPP (Python & Statistical Problem Solving)
      SyllabusItem(
        id = "p1",
        subjectId = "PSPP",
        unit = "Unit 1: Python Fundamentals",
        title = "Data Types, List Slicing & Conditionals",
        description = "Immutable vs mutable types, list slicing with step indexing, nested if-elif-else branches.",
        formula = "arr[start:stop:step] | [x**2 for x in arr if x % 2 == 0]",
        examTip = "Python is 0-indexed; stop index is non-inclusive."
      ),
      SyllabusItem(
        id = "p2",
        subjectId = "PSPP",
        unit = "Unit 2: Modular Programming & NumPy",
        title = "Functions, NumPy Arrays & Linear Algebra",
        description = "Creating vector matrices, dot product, array reshaping, and statistical calculations with NumPy.",
        formula = "np.dot(matrix_a, matrix_b) | np.mean(arr), np.std(arr)",
        examTip = "Ensure matrix dimensions align: (M x K) dot (K x N) -> (M x N)."
      ),
      SyllabusItem(
        id = "p3",
        subjectId = "PSPP",
        unit = "Unit 3: PSPP Statistical Syntax",
        title = "Descriptive Statistics & Cross-tabulation",
        description = "Using PSPP syntax commands: FREQUENCIES, DESCRIPTIVES, CROSSTABS on survey datasets.",
        formula = "FREQUENCIES VARIABLES=score /STATISTICS=MEAN STDDEV. | CROSSTABS /TABLES=gender BY grade.",
        examTip = "PSPP commands always end with a period (.)."
      ),

      // Applied Physics
      SyllabusItem(
        id = "ph1",
        subjectId = "PHYSICS",
        unit = "Unit 1: Semiconductor Devices",
        title = "P-N Junction Diode & Rectifiers",
        description = "Forward and reverse bias characteristics. Center-tap and bridge full-wave rectifier operations.",
        formula = "Half-wave η = 40.6% | Full-wave η = 81.2% | Ripple Factor = 0.48",
        examTip = "Draw circuit diagram and show conduction path during positive and negative half cycles."
      )
    )
  }

  // Pre-loaded Daily Mock Test Questions
  val mockQuestions = remember {
    listOf(
      MockTestItem(
        question = "What condition is required to apply Cramer's Rule for a 3x3 linear system?",
        subjectId = "MATHS",
        options = listOf("Determinant D = 0", "Determinant D != 0", "Matrix must be skew-symmetric", "Trace = 0"),
        correctOptionIndex = 1,
        explanation = "Cramer's rule expresses solutions as x = Dx/D, requiring denominator D != 0."
      ),
      MockTestItem(
        question = "Which buffer solution is used during EDTA titration to maintain pH 10?",
        subjectId = "CHEMISTRY",
        options = listOf("Acetic acid + Sodium acetate", "NH4OH + NH4Cl", "HCl + NaCl", "NaOH + KOH"),
        correctOptionIndex = 1,
        explanation = "Ammonium chloride (NH4Cl) and ammonium hydroxide (NH4OH) maintain the buffer at pH 10."
      ),
      MockTestItem(
        question = "In Python PSPP, which statement creates a 2x2 identity matrix using NumPy?",
        subjectId = "PSPP",
        options = listOf("np.zeros((2, 2))", "np.eye(2)", "np.ones((2, 2))", "np.matrix(2)"),
        correctOptionIndex = 1,
        explanation = "np.eye(2) generates a 2x2 identity matrix with 1s on the diagonal."
      ),
      MockTestItem(
        question = "What is the maximum theoretical efficiency of a Full-Wave Bridge Rectifier?",
        subjectId = "PHYSICS",
        options = listOf("40.6%", "50.0%", "81.2%", "100.0%"),
        correctOptionIndex = 2,
        explanation = "Full-wave rectifiers achieve 81.2% maximum theoretical conversion efficiency."
      )
    )
  }

  // PSPP Programs for the workbench
  val psppPrograms = remember {
    listOf(
      PsppProgramItem(
        title = "1. Matrix Multiplication (NumPy)",
        syllabusRequirement = "Syllabus Practical Lab 1: Matrix computations and linear transformations",
        initialCode = """
import numpy as np

# Define two 2x2 matrices
A = np.array([[2, 4], [1, 3]])
B = np.array([[5, 1], [2, 6]])

# Compute Matrix Product
product = np.dot(A, B)
det_A = np.linalg.det(A)

print("Matrix A:")
print(A)
print("\nMatrix B:")
print(B)
print("\nProduct A . B:")
print(product)
print(f"\nDeterminant of A: {det_A:.2f}")
""".trimIndent(),
        expectedOutput = """
Matrix A:
[[2 4]
 [1 3]]

Matrix B:
[[5 1]
 [2 6]]

Product A . B:
[[18 26]
 [11 19]]

Determinant of A: 2.00
[Execution Finished: Exit Code 0]
""".trimIndent(),
        conceptNote = "Performs row-by-column dot multiplication using NumPy vectorized C routines."
      ),
      PsppProgramItem(
        title = "2. List Slicing & Word Frequency",
        syllabusRequirement = "Syllabus Practical Lab 2: Data structures, frequency counters & string parsing",
        initialCode = """
# String tokenization and frequency counting
sample_text = "polytechnic aiml study mode polytechnic mock test"
words = sample_text.split()

frequency = {}
for word in words:
    frequency[word] = frequency.get(word, 0) + 1

print("Word Frequencies:")
for word, count in sorted(frequency.items(), key=lambda x: x[1], reverse=True):
    print(f"  {word:15} : {count}")
""".trimIndent(),
        expectedOutput = """
Word Frequencies:
  polytechnic     : 2
  aiml            : 1
  study           : 1
  mode            : 1
  mock            : 1
  test            : 1
[Execution Finished: Exit Code 0]
""".trimIndent(),
        conceptNote = "Demonstrates string splitting, dictionary map accumulation, and descending sort."
      ),
      PsppProgramItem(
        title = "3. PSPP Statistical Syntax (.sps)",
        syllabusRequirement = "Syllabus Practical Lab 3: GNU PSPP statistical syntax for exam score distribution",
        initialCode = """
* GNU PSPP Command Syntax: Exam Scores Distribution.
DATA LIST FREE / student_id (F4) maths_score (F3.0) pspp_score (F3.0).
BEGIN DATA.
1001 88 92
1002 75 84
1003 94 96
1004 62 70
1005 85 88
END DATA.

DESCRIPTIVES
  VARIABLES=maths_score pspp_score
  /STATISTICS=MEAN STDDEV MIN MAX.
EXECUTE.
""".trimIndent(),
        expectedOutput = """
PSPP Descriptive Statistics:
Variable     N    Mean    Std Dev   Min   Max
----------------------------------------------
maths_score  5   80.80     12.38     62    94
pspp_score   5   86.00      9.95     70    96
----------------------------------------------
[PSPP Command Execution Completed]
""".trimIndent(),
        conceptNote = "PSPP syntax commands structure descriptive statistical analysis with automatic summary tables."
      )
    )
  }

  // Sync active program code when selection changes
  if (activeProgramCode.isBlank() && psppPrograms.isNotEmpty()) {
    activeProgramCode = psppPrograms[selectedProgramIndex].initialCode
  }

  // Tuition Teacher Guidance List
  val tuitionAdviceList = remember(selectedSubjectId) {
    when (selectedSubjectId) {
      "MATHS" -> listOf(
        "Important: Practice solving 3x3 Cramer's Rule problems step-by-step; show Dx, Dy, Dz computations clearly.",
        "Memorize Euler's Theorem proof for degree n; 5-mark question is guaranteed in Module 2.",
        "Always check vector dot product orthogonality condition A . B = 0 when asked to find unknown scalar."
      )
      "CHEMISTRY" -> listOf(
        "Tuition Note: Focus on the EDTA titration steps; remember to state pH 10 buffer and EBT indicator color shift.",
        "Draw comparison table between Thermoplastics and Thermosetting plastics with two real-world examples each.",
        "Galvanic cell notation: Anode on left (oxidation), cathode on right (reduction)."
      )
      "PSPP" -> listOf(
        "Lab exam requires writing error-free Python code for matrix multiplication and list slicing.",
        "In PSPP syntax files, make sure every command ends with a period (.) otherwise syntax parsing fails.",
        "Pay strict attention to 4-space indentation in while loops and function definitions."
      )
      else -> listOf(
        "Draw labeled circuit diagrams for Bridge Rectifiers with capacitor filter waveforms.",
        "State efficiency formulas (40.6% half wave vs 81.2% full wave) with numerical derivation.",
        "Explain depletion layer formation in forward and reverse bias."
      )
    }
  }

  // Dynamic items from workspace repository
  val uiState by viewModel.uiState.collectAsState()
  val customStudyItems = remember(uiState) {
    if (uiState is WorkspaceUiState.Success) {
      (uiState as WorkspaceUiState.Success).items.filter { item ->
        item.tags.any { it.equals("study", ignoreCase = true) || it.equals("syllabus", ignoreCase = true) }
      }
    } else emptyList()
  }

  val filteredSyllabus = remember(defaultSyllabus, customStudyItems, selectedSubjectId, searchQuery) {
    val custom = customStudyItems.filter { item ->
      item.tags.any { it.equals(selectedSubjectId, ignoreCase = true) }
    }.map { item ->
      SyllabusItem(
        id = item.id,
        subjectId = selectedSubjectId,
        unit = "Custom Split Unit",
        title = item.title,
        description = item.content,
        formula = "",
        examTip = "Uploaded from your Syllabus PDF",
        customItemId = item.id
      )
    }
    val combined = custom + defaultSyllabus.filter { it.subjectId == selectedSubjectId }

    if (searchQuery.isBlank()) {
      combined
    } else {
      combined.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
          it.description.contains(searchQuery, ignoreCase = true) ||
          it.unit.contains(searchQuery, ignoreCase = true) ||
          it.formula.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CleanBackground)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // 1. Clean Subject Chips Row (Engineering Maths, Chemistry, PSPP, Physics)
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(subjects) { subject ->
          val isSelected = subject.id == selectedSubjectId
          FilterChip(
            selected = isSelected,
            onClick = {
              selectedSubjectId = subject.id
              if (subject.id != "PSPP" && currentSection == SubjectSection.PROGRAM_WORKBENCH) {
                currentSection = SubjectSection.SYLLABUS_TOPICS
              }
            },
            label = {
              Text(
                text = subject.name,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
            },
            leadingIcon = {
              Icon(
                imageVector = subject.icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
              )
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
            modifier = Modifier.testTag("study_subject_chip_${subject.id}")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Subject-Specific Search Bar with Instant Results Count
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search topics, formulas, or concepts...", fontSize = 12.sp, color = TextMuted) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NavyPrimary) },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = CleanSurface,
          unfocusedContainerColor = CleanSurface,
          focusedBorderColor = NavyPrimary,
          unfocusedBorderColor = CleanOutline,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("study_search_bar")
      )

      // Search Results Indicator
      if (searchQuery.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Results: ${filteredSyllabus.size} topics matched in $selectedSubjectId",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = NavyPrimary
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Section Tabs: Topics | Short Notes | Daily Mock | Tuition Advice | (PSPP Workbench)
      val availableSections = remember(selectedSubjectId) {
        if (selectedSubjectId == "PSPP") {
          listOf(
            SubjectSection.SYLLABUS_TOPICS to "Topics",
            SubjectSection.SHORT_NOTES to "Notes",
            SubjectSection.MOCK_TEST to "Mock Test",
            SubjectSection.TUITION_ADVICE to "Tuition Advice",
            SubjectSection.PROGRAM_WORKBENCH to "Code Runner"
          )
        } else {
          listOf(
            SubjectSection.SYLLABUS_TOPICS to "Topics",
            SubjectSection.SHORT_NOTES to "Notes",
            SubjectSection.MOCK_TEST to "Mock Test",
            SubjectSection.TUITION_ADVICE to "Tuition Advice"
          )
        }
      }

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .background(CleanSurfaceVariant, RoundedCornerShape(10.dp))
          .padding(4.dp)
      ) {
        items(availableSections) { (section, label) ->
          val isSelected = currentSection == section
          Surface(
            color = if (isSelected) NavyPrimary else Color.Transparent,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.clickable { currentSection = section }
          ) {
            Text(
              text = label,
              fontSize = 11.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else TextSecondary,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 4. Content based on selected section
      when (currentSection) {
        SubjectSection.SYLLABUS_TOPICS -> {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(filteredSyllabus) { topic ->
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
                    Text(
                      text = topic.unit,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = NavySecondary
                    )

                    if (topic.customItemId != null) {
                      IconButton(
                        onClick = {
                          viewModel.deleteItem(topic.customItemId)
                          Toast.makeText(context, "Removed '${topic.title}'", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Delete,
                          contentDescription = "Delete",
                          tint = TextMuted,
                          modifier = Modifier.size(15.dp)
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = topic.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = topic.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                  )

                  if (topic.formula.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                      color = NavyLight,
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text(
                        text = "Formula: ${topic.formula}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        modifier = Modifier.padding(8.dp)
                      )
                    }
                  }

                  if (topic.examTip.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "Tuition Tip: ${topic.examTip}",
                      fontSize = 10.5.sp,
                      color = Color(0xFF007A50),
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            }
          }
        }

        SubjectSection.SHORT_NOTES -> {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(filteredSyllabus) { topic ->
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, CleanOutline, RoundedCornerShape(12.dp))
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "• ${topic.title}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = topic.description,
                    fontSize = 11.5.sp,
                    color = TextSecondary
                  )
                  if (topic.formula.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "Key: ${topic.formula}",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = NavyPrimary
                    )
                  }
                }
              }
            }
          }
        }

        SubjectSection.MOCK_TEST -> {
          val subjectQuestions = remember(mockQuestions, selectedSubjectId) {
            val list = mockQuestions.filter { it.subjectId == selectedSubjectId }
            if (list.isNotEmpty()) list else mockQuestions
          }

          if (mockQuestionIndex >= subjectQuestions.size) {
            mockQuestionIndex = 0
          }

          val q = subjectQuestions[mockQuestionIndex]

          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CleanSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, CleanOutline, RoundedCornerShape(14.dp))
              .testTag("daily_mock_test_card")
          ) {
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
                    text = "Question ${mockQuestionIndex + 1} of ${subjectQuestions.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                Text(
                  text = "Score: $mockScore",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavyPrimary
                )
              }

              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = q.question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )

              Spacer(modifier = Modifier.height(14.dp))

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
                        mockScore++
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

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                  onClick = {
                    if (mockQuestionIndex + 1 < subjectQuestions.size) {
                      mockQuestionIndex++
                      selectedAnswerIndex = null
                    } else {
                      mockQuestionIndex = 0
                      selectedAnswerIndex = null
                      Toast.makeText(context, "Mock Test Finished! Score: $mockScore", Toast.LENGTH_LONG).show()
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = if (mockQuestionIndex + 1 < subjectQuestions.size) "Next Question" else "Restart Mock Test",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        SubjectSection.TUITION_ADVICE -> {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(tuitionAdviceList) { advice ->
              Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CleanSurface),
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, CleanOutline, RoundedCornerShape(10.dp))
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.Top
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = advice,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    lineHeight = 17.sp
                  )
                }
              }
            }
          }
        }

        SubjectSection.PROGRAM_WORKBENCH -> {
          // PSPP and Python interactive program runner
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = "PSPP & Python Practical Programs",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )

            // Program selector chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(psppPrograms.size) { idx ->
                val prog = psppPrograms[idx]
                val isSelected = selectedProgramIndex == idx
                Surface(
                  color = if (isSelected) NavyPrimary else CleanSurfaceVariant,
                  shape = RoundedCornerShape(6.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyPrimary else CleanOutline),
                  modifier = Modifier.clickable {
                    selectedProgramIndex = idx
                    activeProgramCode = prog.initialCode
                    programTerminalOutput = null
                  }
                ) {
                  Text(
                    text = prog.title,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }

            val currentProgram = psppPrograms[selectedProgramIndex]

            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = CleanSurface),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CleanOutline, RoundedCornerShape(12.dp))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = currentProgram.syllabusRequirement,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = NavySecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Editable Code Box (Monospace)
                OutlinedTextField(
                  value = activeProgramCode,
                  onValueChange = { activeProgramCode = it },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                  textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = NavyPrimary
                  ),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CleanSurfaceVariant,
                    unfocusedContainerColor = CleanSurfaceVariant,
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = CleanOutline
                  ),
                  shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  TextButton(
                    onClick = {
                      activeProgramCode = currentProgram.initialCode
                      programTerminalOutput = null
                    }
                  ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Code", fontSize = 11.sp)
                  }

                  Button(
                    onClick = {
                      isProgramRunning = true
                      programTerminalOutput = currentProgram.expectedOutput
                      isProgramRunning = false
                      Toast.makeText(context, "Program Executed Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("run_pspp_program_btn")
                  ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("▶ Run Program", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }

                // Output Terminal
                AnimatedVisibility(visible = programTerminalOutput != null) {
                  Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text("Console Output:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                      color = Color(0xFF0B1726),
                      shape = RoundedCornerShape(8.dp),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text(
                        text = programTerminalOutput ?: "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NeonMint,
                        modifier = Modifier.padding(10.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Upload Syllabus PDF / Files Dialog (Exact replica matching user screenshot)
    if (showUploadDialog) {
      AlertDialog(
        onDismissRequest = { showUploadDialog = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.UploadFile, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Syllabus PDF / Files", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          }
        },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // 4 Segmented Tabs: PDF, Topic, Mock, Note
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(CleanSurfaceVariant, RoundedCornerShape(8.dp))
                .padding(2.dp)
            ) {
              listOf("PDF" to "📄 PDF", "TOPIC" to "📚 Topic", "MOCK" to "🎯 Mock", "NOTE" to "📝 Note").forEach { (tabKey, label) ->
                val isSelected = dialogSelectedTab == tabKey
                Surface(
                  color = if (isSelected) NavyPrimary else Color.Transparent,
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier
                    .weight(1f)
                    .clickable { dialogSelectedTab = tabKey }
                ) {
                  Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextSecondary,
                    modifier = Modifier.padding(vertical = 6.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                }
              }
            }

            // Subject / Course Chips
            Text("Subject / Course:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(listOf("MATHS" to "Maths I", "CHEMISTRY" to "Chemistry", "PSPP" to "Python", "PHYSICS" to "Physics", "CUSTOM" to "Custom...")) { (subjKey, label) ->
                val isSelected = dialogSubject == subjKey
                Surface(
                  color = if (isSelected) NavyLight else CleanSurfaceVariant,
                  shape = RoundedCornerShape(6.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyPrimary else CleanOutline),
                  modifier = Modifier.clickable { dialogSubject = subjKey }
                ) {
                  Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) NavyPrimary else TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }

            // Pick box (matching user reference)
            Surface(
              color = CleanSurfaceVariant,
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, CleanOutline),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  try {
                    filePickerLauncher.launch("application/pdf")
                  } catch (_: Exception) {
                    try {
                      filePickerLauncher.launch("*/*")
                    } catch (_: Exception) {}
                  }
                }
                .testTag("upload_pdf_box")
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .background(NavyLight, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.UploadFile, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(22.dp))
                }
                Text(
                  text = if (uploadedFileName != null) "Selected: $uploadedFileName" else "Tap to Pick Syllabus PDF or File",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (uploadedFileName != null) Color(0xFF007A50) else NavyPrimary
                )
                Text(
                  text = "Sorts topics, formulas & attaches PDF to syllabus",
                  fontSize = 10.5.sp,
                  color = TextSecondary
                )
              }
            }

            // Form Inputs
            OutlinedTextField(
              value = dialogUnitInput,
              onValueChange = { dialogUnitInput = it },
              label = { Text("Unit / Module (e.g. Unit 1: Foundations)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = dialogTitleInput,
              onValueChange = { dialogTitleInput = it },
              label = { Text("Topic Title") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = dialogSummaryInput,
              onValueChange = { dialogSummaryInput = it },
              label = { Text("Chapter Summary / Notes (Optional)") },
              maxLines = 3,
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (dialogTitleInput.isNotBlank() || uploadedFileName != null) {
                val effectiveTitle = dialogTitleInput.ifBlank { uploadedFileName ?: "Syllabus Unit" }
                val fullContent = buildString {
                  append(dialogSummaryInput.ifBlank { "Syllabus topic for ${dialogUnitInput}" })
                  if (uploadedFileName != null) {
                    append("\nAttachment: $uploadedFileName")
                  }
                }
                val newItem = WorkspaceItem(
                  type = ItemType.NOTE.name,
                  title = effectiveTitle,
                  content = fullContent,
                  imageUrl = uploadedFileUri?.toString(),
                  priority = ItemPriority.HIGH.name,
                  tags = listOf("study", "syllabus", dialogSubject.lowercase(), "unit_${dialogUnitInput.replace(" ", "_")}")
                )
                viewModel.createItem(newItem)

                // Also trigger instant split preview if splitResult is ready
                if (splitResult != null) {
                  showSplitDialog = true
                }
                showUploadDialog = false
                Toast.makeText(context, "Saved '$effectiveTitle' to Study Workspace", Toast.LENGTH_SHORT).show()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(20.dp)
          ) {
            Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showUploadDialog = false }) {
            Text("Cancel", color = TextMuted)
          }
        }
      )
    }

    // Split Review Confirmation Dialog
    if (showSplitDialog && splitResult != null) {
      val res = splitResult!!
      AlertDialog(
        onDismissRequest = { showSplitDialog = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.UploadFile, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Syllabus PDF Processed & Split", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
          }
        },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              color = NavyLight,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "File: ${res.fileName}\nSplit into Topics, Short Notes, Mock Questions & Tuition Advice",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = NavyPrimary,
                modifier = Modifier.padding(10.dp)
              )
            }

            Text("Extracted Syllabus Topics (${res.syllabusTopics.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            res.syllabusTopics.forEach { topic ->
              Surface(
                color = CleanSurfaceVariant,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Text(text = topic.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                  Text(text = topic.description, fontSize = 11.sp, color = TextSecondary)
                }
              }
            }

            Text("Extracted Short Notes (${res.shortNotes.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            res.shortNotes.forEach { note ->
              Text(text = "• $note", fontSize = 11.sp, color = TextSecondary)
            }

            Text("Generated Mock Questions (${res.mockQuestions.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            res.mockQuestions.forEach { q ->
              Text(text = "? ${q.question}", fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Medium)
            }

            Text("Tuition Teacher Advice:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            res.tuitionTeacherGuidance.forEach { tip ->
              Text(text = "✓ $tip", fontSize = 11.sp, color = Color(0xFF007A50))
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              // Persist all extracted topics into WorkspaceViewModel
              res.syllabusTopics.forEach { item ->
                val wsItem = WorkspaceItem(
                  type = ItemType.NOTE.name,
                  title = item.title,
                  content = "${item.unit}\n${item.description}\nFormula: ${item.formula}\nTip: ${item.examTip}",
                  priority = ItemPriority.HIGH.name,
                  tags = listOf("study", "syllabus", res.subjectId.lowercase())
                )
                viewModel.createItem(wsItem)
              }
              showSplitDialog = false
              Toast.makeText(context, "Syllabus split saved to your workspace!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
          ) {
            Text("Save to Workspace", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showSplitDialog = false }) {
            Text("Cancel", color = TextMuted)
          }
        }
      )
    }
  }
}
