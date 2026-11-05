package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SlateOutline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.VividViolet

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Attempt silent auto-sign in on initial display
  LaunchedEffect(Unit) {
    AuthManager.attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = { /* Wait for user click */ },
      scope = coroutineScope
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF0A192F),
            Color(0xFF0F294A),
            Color(0xFF0A192F)
          )
        )
      )
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      shape = RoundedCornerShape(24.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Brand Logo: </> symbol in circle (Logo Only)
        Surface(
          shape = CircleShape,
          color = Color(0xFF0F294A),
          modifier = Modifier.size(80.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "</>",
              fontSize = 28.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "NIJO PVT",
          fontSize = 26.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFF0F294A)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
          )
        }

        Button(
          onClick = {
            isLoading = true
            errorMessage = null
            AuthManager.onGoogleSignInClicked(
              context = context,
              credentialManager = credentialManager,
              onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
              },
              onAuthError = { err ->
                isLoading = false
                errorMessage = err
              },
              scope = coroutineScope,
              onAuthCancelled = {
                isLoading = false
              }
            )
          },
          enabled = !isLoading,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("google_sign_in_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0F294A),
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
          } else {
            Text(
              text = "Sign in with Google",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}
