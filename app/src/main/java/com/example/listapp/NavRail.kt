package com.example.listapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed

@Composable
fun NavRail() {
    var selectedItem by remember { mutableIntStateOf(0) }

    val topItem = "Account"
    val bottomItem = "Settings"
    // TODO: To be replaced with getter from firebase
    val middleItems = listOf("List1", "List2", "List3", "List4", "List5", "List6", "List7", "List8", "List9", "List10", "List11") // Example list, can be longer
    val icons = mapOf(
        "Account" to Icons.Filled.AccountCircle,
        "Settings" to Icons.Filled.Settings,
        "List1" to Icons.Filled.ShoppingCart,
        "List2" to Icons.Filled.Place,
        "List3" to Icons.Filled.Home,
        "List4" to Icons.Filled.Star,
        "List5" to Icons.Filled.Favorite, // Add more icons as needed
        "List6" to Icons.Filled.ShoppingCart,
        "List7" to Icons.Filled.ShoppingCart,
        "List8" to Icons.Filled.ShoppingCart,
        "List9" to Icons.Filled.ShoppingCart,
        "List10" to Icons.Filled.ShoppingCart,
        "List11" to Icons.Filled.ShoppingCart,
    )

    NavigationRail {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(color = Color(0.5f, 0.5f, 0.5f, 0.2f)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            NavigationRailItem(
                modifier = Modifier.padding(bottom = 20.dp),
                icon = { Icon(icons[topItem]!!, contentDescription = topItem) },
                label = { Text(topItem) },
                selected = selectedItem == 0,
                onClick = { selectedItem = 0 }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                itemsIndexed(middleItems) { index, item ->
                    NavigationRailItem(
                        modifier = Modifier
                            .padding(vertical = 20.dp),
                        icon = { Icon(icons[item]!!, contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index + 1, // Offset by 1 because Account is at index 0
                        onClick = {
                            selectedItem = index + 1
                            // TODO: To be replaced with navigation
                            Log.i("nav", "Clicked on $index")
                        }
                    )
                }
            }

            NavigationRailItem(
                icon = { Icon(icons[bottomItem]!!, contentDescription = bottomItem) },
                label = { Text(bottomItem) },
                selected = selectedItem == middleItems.size + 1, // After all middle items
                onClick = { selectedItem = middleItems.size + 1 }
            )
        }
    }
}
