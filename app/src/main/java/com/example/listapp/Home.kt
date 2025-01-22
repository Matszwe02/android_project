package com.example.listapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.listapp.ui.theme.ListAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


@Composable
fun Home(
    shoppingListsClass: ShoppingLists,
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    context: Context,
    selectedListId: String?
) {

    var shoppingLists by remember { mutableStateOf(shoppingListsClass.getState()) }
    shoppingListsClass.modifyCallbacks.add { shoppingLists = shoppingListsClass.getState() }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseDatabase.getInstance("https://application-191ac-default-rtdb.europe-west1.firebasedatabase.app").getReference()

    val selectedShoppingList = shoppingLists.find { it.id == selectedListId }

    var currentlyEditingIndex by remember { mutableStateOf<Int?>(null) }

    val shoppingList = remember {
        mutableStateListOf<ShoppingItem>()
    }

    // Save user metadata to the database
    fun saveUserMetadataToDatabase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userMetadata = mapOf(
                "email" to user.email,
                "name" to (user.displayName ?: "Anonymous")
            )

            // Check if the "users" node exists for the current user
            val userRef = db.child("users").child(user.uid)

            // Set the user metadata, this will create the child if it doesn't exist
            userRef.setValue(userMetadata).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Successfully saved user data
                    Log.i("Firebase", "Saved metadata")
                } else {
                    // Handle the error
                    Log.e("Firebase", "Error saving user data", task.exception)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        saveUserMetadataToDatabase()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Wait for 5 seconds
            shoppingListsClass.Callback() // Trigger the refresh logic
            shoppingLists = shoppingListsClass.getState() // Update the local state
        }
    }

    LaunchedEffect(selectedShoppingList) {
        shoppingList.clear()
        selectedShoppingList?.content?.split(";")?.forEach { itemString ->
            val parts = itemString.split(",")
            val name = parts.getOrNull(0) ?: ""
            val price = parts.getOrNull(1)?.let {
                it.toFloatOrNull()?.toString() ?: "0.00"
            } ?: "0.0"
            val shop = parts.getOrNull(2) ?: ""
            val isChecked = parts.getOrNull(3)?.toBoolean() ?: false

            shoppingList.add(
                ShoppingItem(
                    name = name,
                    price = price,
                    shop = shop,
                    isChecked = isChecked
                )
            )
        }
    }

    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    fun saveShoppingListToFirebase() {
        val content = shoppingList.joinToString(";") { item ->
            "${item.name},${item.price},${item.shop},${item.isChecked}"
        }
        selectedShoppingList?.let { selectedList ->
            db.child("shoppingLists").child(selectedList.id).child("content").setValue(content)
                .addOnSuccessListener {
                    Log.d("Firebase", "Shopping list updated successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Error updating shopping list", exception)
                }
        }
    }

    fun addItem() {
        shoppingList.add(ShoppingItem("", "", ""))
        saveShoppingListToFirebase()
    }

    fun removeItem(item: ShoppingItem) {
        shoppingList.remove(item)
        saveShoppingListToFirebase()
    }

    LaunchedEffect(shoppingList) {
        snapshotFlow { shoppingList.toList() }
            .collect {
                saveShoppingListToFirebase()
            }
    }

    // Dynamically generate filters based on unique shops from the shoppingList
    val filters = remember(shoppingList) {
        derivedStateOf {
            listOf("Checked") + shoppingList.map { it.shop.lowercase() }.distinct().map { it.replaceFirstChar { char -> char.uppercase() } }.filter { it.isNotEmpty() }
        }
    }.value

    // Track the state of selected filters
    val selectedFilters = remember { mutableStateMapOf<String, Boolean>() }

    if (selectedShoppingList == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select a list",
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Dynamically generate filters based on unique shops from the shoppingList
            val filters = remember(shoppingList) {
                derivedStateOf {
                    listOf("Checked") + shoppingList.map { it.shop.lowercase() }.distinct().map { it.replaceFirstChar { char -> char.uppercase() } }.filter { it.isNotEmpty() }
                }
            }.value

            // Track the state of selected filters
            val selectedFilters = remember { mutableStateMapOf<String, Boolean>() }

            // Filter bar
            LazyRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters.size) { index ->
                    val filter = filters[index]
                    val isSelected = selectedFilters[filter] ?: false

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 5.dp, vertical = 4.dp)
                            .background(
                                if (isSelected) Color.LightGray else Color.DarkGray,
                                CircleShape
                            )
                            .clickable {
                                selectedFilters[filter] = !isSelected
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.DarkGray else Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))



            // Filtered shopping list
            val filteredShoppingList = remember(shoppingList, selectedFilters) {
                derivedStateOf {
                    shoppingList.filterIndexed { index, item ->
                        val isCheckedFilter = selectedFilters["Checked"] == true
                        val shopFilters = selectedFilters.filter { it.key != "Checked" && it.value }.keys.map { it.lowercase() }

                        // Exclusive filtering logic
                        val matchesChecked = !isCheckedFilter || (isCheckedFilter && item.isChecked)
                        val matchesShop = shopFilters.isEmpty() || shopFilters.contains(item.shop.lowercase())

                        matchesChecked && matchesShop
                    }
                }
            }.value

            val totalPrice = remember(filteredShoppingList) {
                derivedStateOf {
                    filteredShoppingList.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
                }
            }.value

            // Display total price
            Text(
                text = "Total price: $totalPrice",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(filteredShoppingList) { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = {
                                val originalIndex = shoppingList.indexOf(item)
                                if (originalIndex != -1) {
                                    shoppingList[originalIndex] = item.copy(isChecked = it)
                                    saveShoppingListToFirebase()
                                }
                            }
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.name.ifBlank { "empty in here..." },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    currentlyEditingIndex = shoppingList.indexOf(item)
                                }
                            )

                            Text(
                                text = "Price: ${item.price}, Shop: ${item.shop}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val newItem = ShoppingItem("", "", "")
                                shoppingList.add(newItem)
                                currentlyEditingIndex = shoppingList.lastIndex
                                saveShoppingListToFirebase()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add New",
                                tint = Color.LightGray
                            )
                        }
                        Text(
                            "Add new element",
                            modifier = Modifier.clickable {
                                val newItem = ShoppingItem("", "", "")
                                shoppingList.add(newItem)
                                currentlyEditingIndex = shoppingList.lastIndex
                                saveShoppingListToFirebase()
                            },
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Show popup if an item is being edited
        currentlyEditingIndex?.let { editingIndex ->
            val editingItem = shoppingList[editingIndex]
            EditItemPopup(
                item = editingItem,
                onDismiss = { currentlyEditingIndex = null },
                onSave = { updatedItem ->
                    shoppingList[editingIndex] = updatedItem
                    saveShoppingListToFirebase()
                    currentlyEditingIndex = null
                },
                onDelete = {
                    shoppingList.removeAt(editingIndex)
                    saveShoppingListToFirebase()
                    currentlyEditingIndex = null
                }
            )
        }
    }

}

@Composable
fun EditItemPopup(
    item: ShoppingItem,
    onDismiss: () -> Unit,
    onSave: (ShoppingItem) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.price) }
    var shop by remember { mutableStateOf(item.shop) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = shop,
                onValueChange = { shop = it },
                label = { Text("Shop") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(onClick = {
                    onSave(
                        item.copy(
                            name = name.replace(",", "."),
                            price = price.replace(",", ".").toFloatOrNull()?.toString() ?: "0.00",
                            shop = shop.replace(",", ".")
                        )
                    )
                }) {
                    Text("Save")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = onDismiss) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))



                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
}

data class ShoppingItem(
    val name: String,
    val price: String,
    val shop: String,
    val isChecked: Boolean = false
)







