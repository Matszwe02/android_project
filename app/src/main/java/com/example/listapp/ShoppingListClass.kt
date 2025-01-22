package com.example.listapp

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList


data class ShoppingList(
    val id: String = "",
    val users: List<String> = emptyList(),
    val title: String = "",
    val icon: Int = 0,
    val content: String = ""
)



class ShoppingLists : ViewModel()
{
    private val _shoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())

    var updated = false
    val shoppingLists: StateFlow<List<ShoppingList>> = _shoppingLists.asStateFlow()

    var userCallback = {}
    var modifyCallbacks = CopyOnWriteArrayList<() -> Unit>()


    fun setShoppingLists(lists: List<ShoppingList>) {
        _shoppingLists.value = lists
    }

    fun getState(): List<ShoppingList> {
        val currentState = shoppingLists.value
        Log.d("ShoppingLists", "Current state: $currentState")
        return currentState
    }


//    @Composable
    fun Callback(): Boolean
    {
        Log.i("ShoppingLists", "Callback called")
        userCallback()
        val state = shoppingLists.value
        for (i in modifyCallbacks)
        {
            i()
        }

        return state.isNotEmpty()
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

                updated = true
                val lists = mutableListOf<ShoppingList>()
                dataSnapshot.children.forEach { child ->
                    val shoppingList = child.getValue(ShoppingList::class.java)
                    if (shoppingList != null) {
                        if (shoppingList.users.contains(userId)) {
                            lists.add(shoppingList)
                        }
                    }
                }
                _shoppingLists.value = lists

                for (i in modifyCallbacks)
                {
                    i()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DB", "Database error: ${error.message}")
                // Handle error
            }
        })
    }
}