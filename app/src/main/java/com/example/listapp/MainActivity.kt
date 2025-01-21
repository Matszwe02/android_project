package com.example.listapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.composable

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.listapp.ui.theme.ListAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.example.listapp.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity
import com.example.listapp.NavRail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay



class MainActivity : ComponentActivity() {

    private val shoppingLists: ShoppingLists by viewModels()

    private val authViewModel: AuthViewModel by viewModels()

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d("AppFlow", "On Resume")
        authViewModel.invokeAuthCallbacks()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AppFlow", "On Create")
        enableEdgeToEdge()
//        val authViewModel : AuthViewModel by viewModels()
        setContent {
            ListAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row {

                    MyAppNavigation(authViewModel = authViewModel, google = googleAuthUiClient, lists = shoppingLists)

                    }
                }
            }
        }

        authViewModel.addAuthCallback { shoppingLists.Callback() }
        shoppingLists.userCallback = {shoppingLists.fetchShoppingListsFromFirebase(FirebaseAuth.getInstance().currentUser?.uid ?: "")}

//        authViewModel.invokeAuthCallbacks()

    }
}





@Composable
fun MyAppNavigation(modifier: Modifier = Modifier,authViewModel: AuthViewModel, google: GoogleAuthUiClient, lists: ShoppingLists) {
    val navController = rememberNavController()

    val context = LocalContext.current
    FirebaseApp.initializeApp(context)
//    val firebase = FirebaseDatabase.getInstance("https://application-191ac-default-rtdb.europe-west1.firebasedatabase.app")
//    val dbref = firebase.getReference("info")


    NavRail(lists, navController, authViewModel)

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginPage(modifier, navController, authViewModel, google = google)
        }
        composable("signup"){
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home/{selectedListId}") { backStackEntry ->
            // Retrieve the selectedListId argument
            val selectedListId = backStackEntry.arguments?.getString("selectedListId")
            Home(lists, modifier, navController, authViewModel, context, selectedListId)
        }
        composable("account"){
            Account(modifier, navController, authViewModel, context)
        }
        composable("settings"){
            Settings(modifier)
        }
    })
}