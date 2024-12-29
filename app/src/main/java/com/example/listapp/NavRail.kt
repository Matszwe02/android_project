package com.example.listapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun NavRail(shoppingLists: List<ShoppingList>) {

    val iconDictionary = mapOf(
        1 to Icons.Filled.ShoppingCart,
        2 to Icons.Filled.Place,
        3 to Icons.Filled.Home,
        4 to Icons.Filled.Star,
        5 to Icons.Filled.Favorite,
        // Add more icons as needed
    )


    var selectedItem by remember { mutableIntStateOf(0) }

    val topItem = "Account"
    val bottomItem = "App Settings"

    NavigationRail(modifier = Modifier.widthIn(max = 70.dp)) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(color = Color(0.5f, 0.5f, 0.5f, 0.2f)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            NavigationRailItem(
                modifier = Modifier.padding(bottom = 20.dp),
                icon = { Icon(Icons.Filled.AccountCircle, contentDescription = topItem) },
                label = { Text(topItem) },
                selected = selectedItem == 0,
                onClick = { selectedItem = 0 }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                itemsIndexed(shoppingLists) { index, shoppingList ->
                    NavigationRailItem(
                        modifier = Modifier
                            .padding(vertical = 20.dp),
                        icon = {
                            Icon(
                                iconDictionary[shoppingList.icon] ?: Icons.Filled.ShoppingCart,
                                contentDescription = shoppingList.title
                            )
                        },
                        label = {
                            Text(
                                text = shoppingList.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        selected = selectedItem == index + 1,
                        onClick = {
                            selectedItem = index + 1
                            // TODO: To be replaced with navigation
                            Log.i("nav", "Clicked on $shoppingList")
                        }
                    )
                }
            }

            NavigationRailItem(
                icon = { Icon(Icons.Filled.Settings, contentDescription = bottomItem) },
                label = {
                    Text(
                        text = bottomItem,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                },                selected = selectedItem == shoppingLists.size + 1, // After all shopping lists
                onClick = { selectedItem = shoppingLists.size + 1 }
            )
        }
    }
}