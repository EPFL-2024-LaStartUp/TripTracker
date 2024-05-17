package com.example.triptracker.view

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.triptracker.model.network.Connection

@Composable
/**
 * @param navigation (Navigation): Navigation object to navigate to other screens
 * @brief NavigationBar composable that displays the bottom navigation bar once logged in
 */
fun NavigationBar(navigation: Navigation, connection: Connection = Connection()) {
  NavigationBar(
      modifier = Modifier.height(73.dp),
      content = {
        val destinations = navigation.getTopLevelDestinations()
        destinations.forEach { destination ->
          NavigationBarItem(
              // Enable the profile icon only if the user is connected to the internet
              enabled =
                  if (destination.route == Route.PROFILE) true
                  else connection.isDeviceConnectedToInternet(),
              icon = { Icon(destination.icon, contentDescription = destination.textId) },
              label = { Text(destination.textId) },
              selected = navigation.getCurrentDestination().route == destination.route,
              onClick = { navigation.navigateTo(destination) })
        }
      })
}
