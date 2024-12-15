package com.example.listapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.listapp.ui.theme.ListAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


@Composable
fun Home(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, context: Context) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseDatabase.getInstance("https://application-191ac-default-rtdb.europe-west1.firebasedatabase.app").getReference("users/$userId")


    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }
    Text(
        text = "Hello!",
        modifier = modifier
    )

    TextButton(onClick = {
        authViewModel.signout()
    }) {
        Text(text = "Sign out")
    }
}