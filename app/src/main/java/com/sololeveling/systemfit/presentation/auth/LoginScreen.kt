package com.sololeveling.systemfit.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    supabase: SupabaseClient,
    onLoginSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val action = supabase.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            isLoading = false
            when (result) {
                is NativeSignInResult.Success -> {
                    onLoginSuccess()
                }
                is NativeSignInResult.Error -> {
                    errorMessage = result.message
                }
                is NativeSignInResult.ClosedByUser -> {
                    errorMessage = "Sign-in cancelled."
                }
                is NativeSignInResult.NetworkError -> {
                    errorMessage = "Network error. Please try again."
                }
            }
        },
        fallback = {
            isLoading = true
            coroutineScope.launch {
                try {
                    supabase.auth.signInWith(Google)
                } catch (e: Exception) {
                    errorMessage = e.message
                    isLoading = false
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SYSTEM FIT",
            color = Color(0xFF38BDF8),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Authenticate to receive your daily quest.",
            color = Color.LightGray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF38BDF8))
        } else {
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    action.startFlow()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
            ) {
                Text(
                    text = "Sign in with Google",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fallback button for Browser OAuth
            OutlinedButton(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            supabase.auth.signInWith(Google)
                        } catch (e: Exception) {
                            errorMessage = e.message
                            isLoading = false
                        }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
            ) {
                Text(
                    text = "Sign in via Browser",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp
            )
        }
    }
}
