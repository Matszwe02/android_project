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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {
    private val _shoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val shoppingLists: StateFlow<List<ShoppingList>> = _shoppingLists.asStateFlow()


    fun setShoppingLists(lists: List<ShoppingList>) {
        _shoppingLists.value = lists
    }


    fun fetchShoppingListsFromFirebase(userId: String?) {
        Log.i("DB", "Fetching shopping lists for user: $userId")
        if (userId == null) {
            Log.w("DB", "User ID is null, clearing shopping lists")
            _shoppingLists.value = emptyList()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val ref = database.reference.child("shoppingLists")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("DB", "Received data snapshot")
                val lists = mutableListOf<ShoppingList>()
                dataSnapshot.children.forEach { child ->
                    val shoppingList = child.getValue(ShoppingList::class.java)
                    if (shoppingList != null) {
                        Log.d("DB", "Processing shopping list: ${shoppingList.title}")
                        if (shoppingList.users.contains(userId)) {
                            Log.d("DB", "User has access to list: ${shoppingList.title}")
                            lists.add(shoppingList)
                        } else {
                            Log.d("DB", "User does not have access to list: ${shoppingList.title}")
                        }
                    } else {
                        Log.w("DB", "Failed to parse shopping list")
                    }
                }
                Log.i("DB", "Found ${lists.size} accessible shopping lists")
                _shoppingLists.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DB", "Database error: ${error.message}")
                // Handle error
            }
        })
    }


}



class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }


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
                    Row {

                    MyAppNavigation(authViewModel = authViewModel, google = googleAuthUiClient, lists = viewModel.shoppingLists)

                    }
                }
            }
        }

        authViewModel.addAuthCallback {viewModel.fetchShoppingListsFromFirebase(authViewModel.userId)}

    }
}





@Composable
fun MyAppNavigation(modifier: Modifier = Modifier,authViewModel: AuthViewModel, google: GoogleAuthUiClient, lists: StateFlow<List<ShoppingList>>) {
    val navController = rememberNavController()

    val context = LocalContext.current
    FirebaseApp.initializeApp(context)
//    val firebase = FirebaseDatabase.getInstance("https://application-191ac-default-rtdb.europe-west1.firebasedatabase.app")
//    val dbref = firebase.getReference("info")


    NavRail(lists.collectAsState().value, navController, authViewModel)

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
            Home(lists.collectAsState().value, modifier, navController, authViewModel, context, selectedListId)
        }
        composable("account"){
            Account(modifier, navController, authViewModel, context)
        }
        composable("settings"){
            Settings(modifier)
        }
    })
}