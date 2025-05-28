package com.groink.reservationapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.groink.reservationapp.repository.FirebaseRepository
import com.groink.reservationapp.data.User
import com.google.firebase.Timestamp

@Composable
fun AuthScreen(
    firebaseRepository: FirebaseRepository,
    onAuthSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Titre
        Icon(
            Icons.Default.Place,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Réservation Terrain",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isLoginMode) "Connectez-vous à votre compte" else "Créez votre compte",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Formulaire
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nom (seulement en mode inscription)
                if (!isLoginMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom complet") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                }

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Mot de passe
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "Masquer" else "Afficher",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Confirmer mot de passe (seulement en mode inscription)
                if (!isLoginMode) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmer le mot de passe") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isError = confirmPassword.isNotEmpty() && password != confirmPassword
                    )

                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text(
                            text = "Les mots de passe ne correspondent pas",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Message d'erreur
                errorMessage?.let { message ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Bouton principal
                Button(
                    onClick = {
                        scope.launch {
                            handleAuth(
                                isLoginMode = isLoginMode,
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
                                name = name,
                                firebaseRepository = firebaseRepository,
                                onLoading = { isLoading = it },
                                onError = { errorMessage = it },
                                onSuccess = onAuthSuccess
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() &&
                            (isLoginMode || (name.isNotEmpty() && password == confirmPassword))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (isLoginMode) "Se connecter" else "S'inscrire")
                    }
                }

                // Bouton pour changer de mode
                TextButton(
                    onClick = {
                        isLoginMode = !isLoginMode
                        errorMessage = null
                        password = ""
                        confirmPassword = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(
                        if (isLoginMode) "Pas de compte ? S'inscrire"
                        else "Déjà un compte ? Se connecter"
                    )
                }
            }
        }
    }
}

private suspend fun handleAuth(
    isLoginMode: Boolean,
    email: String,
    password: String,
    confirmPassword: String,
    name: String,
    firebaseRepository: FirebaseRepository,
    onLoading: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onSuccess: (String) -> Unit
) {
    onLoading(true)
    onError(null)

    try {
        if (isLoginMode) {
            // Connexion
            val result = firebaseRepository.signInWithEmail(email, password)
            result.fold(
                onSuccess = { userId ->
                    onSuccess(userId)
                },
                onFailure = { exception ->
                    onError(getErrorMessage(exception))
                }
            )
        } else {
            // Inscription
            if (password != confirmPassword) {
                onError("Les mots de passe ne correspondent pas")
                return
            }

            if (password.length < 6) {
                onError("Le mot de passe doit contenir au moins 6 caractères")
                return
            }

            val result = firebaseRepository.createUserWithEmail(email, password)
            result.fold(
                onSuccess = { userId ->
                    // Créer le profil utilisateur
                    val user = User(
                        id = userId,
                        name = name,
                        email = email,
                        createdAt = Timestamp.now()
                    )

                    val createUserResult = firebaseRepository.createUser(user)
                    createUserResult.fold(
                        onSuccess = { onSuccess(userId) },
                        onFailure = { exception ->
                            onError("Erreur lors de la création du profil: ${exception.message}")
                        }
                    )
                },
                onFailure = { exception ->
                    onError(getErrorMessage(exception))
                }
            )
        }
    } finally {
        onLoading(false)
    }
}

private fun getErrorMessage(exception: Throwable): String {
    return when {
        exception.message?.contains("badly formatted") == true -> "Format d'email invalide"
        exception.message?.contains("no user record") == true -> "Aucun compte trouvé avec cet email"
        exception.message?.contains("wrong password") == true -> "Mot de passe incorrect"
        exception.message?.contains("email already in use") == true -> "Cet email est déjà utilisé"
        exception.message?.contains("weak password") == true -> "Mot de passe trop faible"
        exception.message?.contains("network error") == true -> "Erreur de connexion réseau"
        else -> exception.message ?: "Une erreur s'est produite"
    }
}