package com.example.vendontme.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vendontme.ui.navigation.Screen
import com.example.vendontme.di.AppModule

@Composable
fun SignInScreen(nav: NavController) {

    val vm: AuthViewModel = viewModel(
        factory = AppModule.provideAuthViewModelFactory()
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.checkSession(nav)
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Sign In", style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.loading
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.loading
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { vm.signIn(email, password, nav) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.loading
            ) {
                if (vm.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign In")
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = {
                    nav.navigate(Screen.SignUp)
                },
                enabled = !vm.loading
            ) {
                Text("Don't have an account? Sign Up")
            }

            vm.error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}