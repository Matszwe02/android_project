package com.example.listapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.composable

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.listapp.ui.theme.ListAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        setContent {
            ListAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    Greeting("Android")
                    MyAppNavigation(authViewModel = authViewModel)
                }
            }
        }
    }
}





@Composable
fun MyAppNavigation(modifier: Modifier = Modifier,authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    val context = LocalContext.current
    FirebaseApp.initializeApp(context)
//    val firebase = FirebaseDatabase.getInstance("https://application-191ac-default-rtdb.europe-west1.firebasedatabase.app")
//    val dbref = firebase.getReference("info")


    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginPage(modifier,navController,authViewModel)
        }
        composable("signup"){
            SignupPage(modifier,navController,authViewModel)
        }
        composable("home"){
            Home(modifier,navController,authViewModel, context)
        }
//        composable("newinfo")
//        {
//            NewinfoPage(modifier = modifier, context = context, navController, authViewModel)
//        }
    })
}