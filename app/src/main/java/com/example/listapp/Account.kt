package com.example.listapp

import Identicon
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.listapp.ui.theme.ListAppTheme
import IdenticonDrawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.google.firebase.auth.UserProfileChangeRequest


@Composable
fun Account(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, context: Context) {

    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: ""
//    val db = FirebaseDatabase.getInstance("https://application-191ac-default-rtdb.europe-west1.firebasedatabase.app").getReference("users/$userId")

    var username by remember { mutableStateOf(user?.displayName?:"User")}
    var password by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }
    var changePasswordVisible by remember { mutableStateOf(false) }
    var changeNameVisible by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier.height(32.dp))

        Identicon(userId)
//        if (userId == "")
//            Icon(
//                imageVector = Icons.Filled.AccountCircle,
//                contentDescription = "User Icon",
//                modifier = Modifier.size(100.dp)
//            )
//        else
//            Icon(
//                bitmap = icon.toBitmap(100.dp),
//                contentDescription = "User Icon",
//                modifier = Modifier.size(100.dp)
//            )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hello, ${username}!"
        )
        Text(
            text = user?.email?:""
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (changeNameVisible)
        {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Name") },
                placeholder = { Text("John Smith") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        }

        Button(onClick = {

            if (!changeNameVisible) changeNameVisible = true
            else if (username.isNotEmpty())
            {
                user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(username).build())
                Toast.makeText(context, "Updated Name!", Toast.LENGTH_SHORT).show()
                changeNameVisible = false
            }
            else
            {
                changeNameVisible = false
            }
        }) {
            Text(if (username.isNotEmpty() || !changeNameVisible) "Change Name" else "Cancel")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (changePasswordVisible)
        {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.trim() },
                label = { Text("Password") },
                placeholder = { Text("") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Check else Icons.Filled.CheckCircle,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )
            OutlinedTextField(
                value = password2,
                onValueChange = { password2 = it.trim() },
                label = { Text("Confirm Password") },
                placeholder = { Text("") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
        }

        Button(onClick = {
            if (!changePasswordVisible) changePasswordVisible = true
            else if (password.length > 7)
            {
                if (password == password2)
                {
                    user?.updatePassword(password)
                    Toast.makeText(context, "Updated Password!", Toast.LENGTH_SHORT).show()
                    changePasswordVisible = false

                }
                else
                {
                    Toast.makeText(context, "Passwords don't match!", Toast.LENGTH_SHORT).show()

                }
            }
            else if (password.isEmpty())
            {
                changePasswordVisible = false
            }
        }) {
            Text(if (password.isNotEmpty() || !changePasswordVisible) "Change Password" else "Cancel")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            authViewModel.signout()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }) {
            Text("Logout")
        }
    }
}
