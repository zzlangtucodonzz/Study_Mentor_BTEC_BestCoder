package com.example.study_mentor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: FirebaseUser? = null
)

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Observe current user changes
        _authState.value = _authState.value.copy(currentUser = auth.currentUser)
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = _authState.value.copy(currentUser = firebaseAuth.currentUser)
        }
    }

    fun login(email: String, password: String) {
        if (!validateEmail(email)) {
            _authState.value = _authState.value.copy(errorMessage = "Invalid email format")
            return
        }
        if (password.length < 6) {
            _authState.value = _authState.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = null)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun register(email: String, password: String, name: String) {
        if (name.isBlank()) {
            _authState.value = _authState.value.copy(errorMessage = "Name cannot be empty")
            return
        }
        if (!validateEmail(email)) {
            _authState.value = _authState.value.copy(errorMessage = "Invalid email format")
            return
        }
        if (password.length < 6) {
            _authState.value = _authState.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                // Optionally update profile with name
                val user = result.user
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user?.updateProfile(profileUpdates)?.await()
                
                // Force a refresh of the user to get the display name in the current session
                user?.reload()?.await()
                
                _authState.value = _authState.value.copy(
                    isLoading = false, 
                    errorMessage = null, 
                    currentUser = auth.currentUser
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = _authState.value.copy(currentUser = null)
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
