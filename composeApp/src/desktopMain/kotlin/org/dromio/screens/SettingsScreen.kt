package org.dromio.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation  // Add this import
import androidx.compose.ui.unit.dp
import org.dromio.Colors
import org.dromio.repository.UserRepository
import org.dromio.models.UserCredentials
import org.dromio.models.User

@Composable
fun SettingsScreen(currentUser: User, onLogout: () -> Unit) {  // Add currentUser parameter
    var showAddUser by remember { mutableStateOf(false) }
    var showResetPassword by remember { mutableStateOf<User?>(null) }
    var showChangePassword by remember { mutableStateOf(false) }  // Add this
    val userRepository = remember { UserRepository() }
    var users by remember { mutableStateOf(userRepository.getAllUsers()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Logout and Change Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("User Management", style = MaterialTheme.typography.h5)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showChangePassword = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Colors.Secondary)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Change Password")
                    Spacer(Modifier.width(8.dp))
                    Text("Change My Password")
                }
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", color = MaterialTheme.colors.onError)
                }
            }
        }

        // Add User Button
        Button(
            onClick = { showAddUser = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Colors.Primary)
        ) {
            Icon(Icons.Default.PersonAdd, "Add user")
            Spacer(Modifier.width(8.dp))
            Text("Add New User")
        }

        // Users List
        Card(
            modifier = Modifier.weight(1f),
            elevation = 4.dp
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    UserListItem(
                        user = user,
                        onResetPassword = { showResetPassword = user },
                        onDelete = {
                            if (!user.isAdmin) {
                                userRepository.deleteUser(user.id)
                                users = userRepository.getAllUsers()
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Change Password Dialog
    if (showChangePassword) {
        ChangePasswordDialog(
            onDismiss = { showChangePassword = false },
            onConfirm = { oldPassword, newPassword ->
                val success = userRepository.changePassword(
                    currentUser.id,
                    oldPassword,
                    newPassword
                )
                if (!success) {
                    // Show error (you might want to handle this better)
                    println("Failed to change password")
                }
                showChangePassword = false
            }
        )
    }

    // Dialogs
    if (showAddUser) {
        AddUserDialog(
            onDismiss = { showAddUser = false },
            onAdd = { username, password ->
                userRepository.createUser(UserCredentials(username, password))
                users = userRepository.getAllUsers()
                showAddUser = false
            }
        )
    }

    showResetPassword?.let { user ->
        ResetPasswordDialog(
            username = user.username,
            onDismiss = { showResetPassword = null },
            onReset = { newPassword ->
                userRepository.resetPassword(user.id, newPassword)
                showResetPassword = null
            }
        )
    }
}

@Composable
private fun UserListItem(
    user: User,
    onResetPassword: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(user.username, style = MaterialTheme.typography.subtitle1)
                Text(
                    if (user.isAdmin) "Administrator" else "POS User",
                    style = MaterialTheme.typography.caption,
                    color = Colors.TextSecondary
                )
            }

            if (!user.isAdmin) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onResetPassword) {
                        Icon(Icons.Default.Lock, "Reset password")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            "Delete user",
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onAdd: (username: String, password: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(username, password) },
                enabled = username.isNotEmpty() && password.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ResetPasswordDialog(
    username: String,
    onDismiss: () -> Unit,
    onReset: (newPassword: String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password for $username") },
        text = {
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") }
            )
        },
        confirmButton = {
            Button(
                onClick = { onReset(newPassword) },
                enabled = newPassword.isNotEmpty()
            ) {
                Text("Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPassword: String, newPassword: String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                if (newPassword != confirmPassword) {
                    Text(
                        "Passwords don't match",
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(oldPassword, newPassword) },
                enabled = newPassword.isNotEmpty() &&
                         newPassword == confirmPassword &&
                         oldPassword.isNotEmpty()
            ) {
                Text("Change Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
