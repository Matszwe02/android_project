package com.example.listapp

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList


import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext





class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    var userId: String? = null

//    var authCallback = {}
    private val authCallbacks = CopyOnWriteArrayList<() -> Unit>()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }


    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }else{
            _authState.value = FirebaseAuth.getInstance().currentUser?.let { AuthState.Authenticated(it) }
        }
    }

    fun login(email : String,password : String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = FirebaseAuth.getInstance().currentUser?.let { AuthState.Authenticated(it) }
                    Log.i("DB", "Logged in")
                    userId = FirebaseAuth.getInstance().currentUser?.uid
                    Log.i("DB", "Logged in as $userId")
                    invokeAuthCallbacks()
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }

    }

    fun signup(email : String,password : String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = FirebaseAuth.getInstance().currentUser?.let {
                        AuthState.Authenticated(
                            it
                        )
                    }
                    userId = FirebaseAuth.getInstance().currentUser?.uid
                    Log.i("DB", "Logged in as $userId")
                    invokeAuthCallbacks()
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }
    fun addAuthCallback(callback: () -> Unit) {
        authCallbacks.add(callback)
    }

    fun removeAuthCallback(callback: () -> Unit) {
        authCallbacks.remove(callback)
    }

    fun invokeAuthCallbacks() {
        authCallbacks.forEach { it.invoke() }
    }



    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        userId = null
        Log.i("DB", "Logged out")
        invokeAuthCallbacks()
    }


    fun updateAuthState(user: FirebaseUser?) {
        if (user != null) {
            _authState.value = AuthState.Authenticated(user)
            userId = user.uid
            Log.i("DB", "Logged in as $userId")
            invokeAuthCallbacks()
        } else {
            _authState.value = AuthState.Unauthenticated
            userId = null
            Log.i("DB", "Logged out")
            invokeAuthCallbacks()
        }
    }

}


sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}




@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    onSignInSuccess: (FirebaseUser) -> Unit,
    onSignInFailure: (Exception) -> Unit
) {
    val context = LocalContext.current

    val googleAuthUiClient = remember { GoogleAuthUiClient(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val signInResult = googleAuthUiClient.signInWithIntent(result.data ?: Intent())
                    signInResult.data?.let { userData ->
                        val firebaseUser = Firebase.auth.currentUser
                        if (firebaseUser != null) {
                            withContext(Dispatchers.Main) {
                                onSignInSuccess(firebaseUser)
                            }
                        } else {
                            throw Exception("FirebaseUser is null")
                        }
                    } ?: throw Exception("UserData is null")
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        onSignInFailure(e)
                    }
                }
            }
        }
    )

    Button(
        onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                val intentSender = googleAuthUiClient.signIn()
                intentSender?.let {
                    launcher.launch(IntentSenderRequest.Builder(it).build())
                }
            }
        },
        modifier = modifier.padding(16.dp),
    ) {
        Text(text = "Sign in with Google")
    }
}
