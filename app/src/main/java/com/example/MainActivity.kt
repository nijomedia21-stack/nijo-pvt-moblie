package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import com.example.ui.AdaptiveWorkspaceApp
import com.example.ui.auth.AuthManager
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.OmniSyncTheme
import com.example.ui.viewmodel.WorkspaceViewModel
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
    configureAppCheck(this, intent)

    enableEdgeToEdge()

    setContent {
      OmniSyncTheme {
        OmniSyncRoot()
      }
    }
  }
}

@Composable
fun OmniSyncRoot() {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
  var isOfflineModeActive by remember { mutableStateOf(false) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
      if (auth.currentUser != null) {
        isOfflineModeActive = false
      }
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose {
      Firebase.auth.removeAuthStateListener(listener)
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (currentUser == null && !isOfflineModeActive) {
      AuthScreen(
        onAuthSuccess = {
          currentUser = Firebase.auth.currentUser
          isOfflineModeActive = false
        },
        onContinueOffline = {
          isOfflineModeActive = true
        }
      )
    } else {
      val repository = remember(currentUser?.uid, isOfflineModeActive) {
        com.example.data.repository.WorkspaceRepository(context)
      }
      val workspaceViewModel = remember(repository) {
        WorkspaceViewModel(repository)
      }

      AdaptiveWorkspaceApp(
        workspaceViewModel = workspaceViewModel,
        onSignOut = {
          isOfflineModeActive = false
          if (currentUser != null) {
            AuthManager.signOut(
              context = context,
              credentialManager = credentialManager,
              onSignOutComplete = {
                currentUser = null
              },
              scope = coroutineScope
            )
          } else {
            currentUser = null
          }
        }
      )
    }
  }
}
