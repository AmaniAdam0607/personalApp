package org.dromio.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dromio.Colors
import org.dromio.models.User
import org.dromio.models.UserCredentials
import org.dromio.repository.UserRepository

@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val userRepository = remember { UserRepository() }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.width(400.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Login", style = MaterialTheme.typography.h5)

                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colors.error)
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val user = userRepository.authenticate(
                            UserCredentials(username, password)
                        )
                        if (user != null) {
                            error = ""
                            onLoginSuccess(user)
                        } else {
                            error = "Invalid username or password"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Colors.Primary
                    )
                ) {
                    Text("Login", color = MaterialTheme.colors.onPrimary)
                }
            }
        }
    }
}
