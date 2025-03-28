package com.example.listapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.platform.LocalContext

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    isNotificationsEnabled: Boolean,
    onNotificationSettingChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Settings", fontSize = 32.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enable Notifications",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isNotificationsEnabled,
                onCheckedChange = {
                    onNotificationSettingChanged(it)
                    // Save the setting to shared preferences
                    context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit()
                        .putBoolean("notifications_enabled", it)
                        .apply()
                }
            )
        }

        // Add more settings options here
    }
}
