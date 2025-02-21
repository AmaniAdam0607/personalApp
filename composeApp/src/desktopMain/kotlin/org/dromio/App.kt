package org.dromio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import java.awt.Toolkit
import org.dromio.database.Database
import org.dromio.models.User
import org.dromio.screens.*

enum class Screen {
    POS,
    REPORTS,
    INVENTORY,
    SETTINGS,
    DEBTS,
    RETURNS // Add this
}

@Composable
fun App() {
    LaunchedEffect(Unit) {
        Database.ensureInitialized() // Ensure DB is initialized in composition
    }

    var currentUser by remember { mutableStateOf<User?>(null) }
    var currentScreen by remember { mutableStateOf(Screen.POS) }

    if (currentUser == null) {
        LoginScreen { user -> // Accept user parameter
            currentUser = user
            currentScreen = Screen.POS
        }
        return
    }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationBar(
                    currentScreen = currentScreen,
                    onScreenChange = { currentScreen = it },
                    isAdmin = currentUser?.isAdmin ?: false
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                when (currentScreen) {
                    Screen.POS -> POSScreen(onLogout = { currentUser = null })
                    Screen.RETURNS -> ReturnSalesScreen() // Add this
                    Screen.REPORTS -> if (currentUser?.isAdmin == true) ReportsScreen()
                    Screen.INVENTORY -> if (currentUser?.isAdmin == true) InventoryScreen()
                    Screen.SETTINGS ->
                            if (currentUser?.isAdmin == true) {
                                SettingsScreen(
                                    currentUser = currentUser!!,  // Pass the current user
                                    onLogout = { currentUser = null }
                                )
                            }
                    Screen.DEBTS -> if (currentUser?.isAdmin == true) DebtsScreen()
                }
            }
        }
    }
}

@Composable
private fun NavigationBar(
        currentScreen: Screen,
        onScreenChange: (Screen) -> Unit,
        isAdmin: Boolean
) {
    Column(
            modifier =
                    Modifier.width(80.dp)
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
                icon = Icons.Default.Assignment,
                label = "Returns",
                selected = currentScreen == Screen.RETURNS,
                onClick = { onScreenChange(Screen.RETURNS) }
        )

        if (isAdmin) {
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
            NavItem(
                    icon = Icons.Default.Money,
                    label = "Debts",
                    selected = currentScreen == Screen.DEBTS,
                    onClick = { onScreenChange(Screen.DEBTS) }
            )
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
            modifier = Modifier.width(80.dp).clickable(onClick = onClick).padding(8.dp),
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
    Database.init() // Initialize DB first

    if (!initializeApp()) {
        exitApplication()
        return@application
    }

    val windowState = rememberWindowState()
    val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
    windowState.size =
            androidx.compose.ui.unit.DpSize(
                    width = screenSize.width.dp,
                    height = screenSize.height.dp
            )

    Window(
            onCloseRequest = ::exitApplication,
            title = "Avelyn Shop",
            state = windowState,
            undecorated = true
    ) { App() }
}
