package org.dromio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import java.awt.Toolkit
import org.dromio.screens.*
import org.dromio.database.Database

enum class Screen {
    POS,
    REPORTS,
    INVENTORY,
    SETTINGS
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.POS) }

    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = Colors.Primary,
            secondary = Colors.Secondary,
            background = Colors.Background,
            surface = Colors.Surface,
            onPrimary = Color.White,
            onSecondary = Color.White
        )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationBar(
                currentScreen = currentScreen,
                onScreenChange = { currentScreen = it }
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                when (currentScreen) {
                    Screen.POS -> POSScreen()
                    Screen.REPORTS -> ReportsScreen()
                    Screen.INVENTORY -> InventoryScreen()
                    Screen.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun NavigationBar(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(Colors.Surface)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NavItem(
            icon = Icons.Default.ShoppingCart,
            label = "POS",
            selected = currentScreen == Screen.POS,
            onClick = { onScreenChange(Screen.POS) }
        )
        NavItem(
            icon = Icons.Default.Assessment,
            label = "Reports",
            selected = currentScreen == Screen.REPORTS,
            onClick = { onScreenChange(Screen.REPORTS) }
        )
        NavItem(
            icon = Icons.Default.Inventory,
            label = "Inventory",
            selected = currentScreen == Screen.INVENTORY,
            onClick = { onScreenChange(Screen.INVENTORY) }
        )
        NavItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            selected = currentScreen == Screen.SETTINGS,
            onClick = { onScreenChange(Screen.SETTINGS) }
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Colors.Primary else Colors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = if (selected) Colors.Primary else Colors.TextSecondary
        )
    }
}

private fun initializeApp(): Boolean {
    return try {
        Database.init()
        true
    } catch (e: Exception) {
        println("Failed to initialize application: ${e.message}")
        false
    }
}

fun main() = application {
    if (!initializeApp()) {
        exitApplication()
        return@application
    }

    val windowState = rememberWindowState()
    val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
    windowState.size = androidx.compose.ui.unit.DpSize(
        width = screenSize.width.dp,
        height = screenSize.height.dp
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "Avelyn Shop",
        state = windowState,
        undecorated = true
    ) {
        App()
    }
}


