package com.example.study_mentor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study_mentor.data.GamificationRepository
import com.example.study_mentor.data.UserStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Job
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

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val gamificationRepository: GamificationRepository = GamificationRepository()
) : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private var statsJob: Job? = null

    init {
        // Observe current user changes
        val initialUser = auth.currentUser
        _authState.value = _authState.value.copy(currentUser = initialUser)
        observeUserStats(initialUser?.uid)

        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _authState.value = _authState.value.copy(currentUser = user)
            observeUserStats(user?.uid)
        }
    }

    private fun observeUserStats(uid: String?) {
        statsJob?.cancel()
        if (uid.isNullOrBlank()) {
            _userStats.value = UserStats(totalXp = 0)
            return
        }
        statsJob = viewModelScope.launch {
            gamificationRepository.getUserStats(uid).collect { stats ->
                _userStats.value = stats
            }
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

    fun updateDisplayName(newName: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            _authState.value = _authState.value.copy(errorMessage = "Name cannot be empty")
            onResult(false, "Name cannot be empty")
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            try {
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmed)
                    .build()
                user?.updateProfile(profileUpdates)?.await()
                user?.reload()?.await()
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = null,
                    currentUser = auth.currentUser
                )
                onResult(true, null)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage
                )
                onResult(false, e.localizedMessage)
            }
        }
    }

    fun updateProfilePicture(uri: android.net.Uri, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            try {
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build()
                user?.updateProfile(profileUpdates)?.await()
                user?.reload()?.await()
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = null,
                    currentUser = auth.currentUser
                )
                onResult(true, null)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage
                )
                onResult(false, e.localizedMessage)
            }
        }
    }

    fun sendPasswordReset(email: String? = null, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        val targetEmail = email ?: auth.currentUser?.email
        if (targetEmail.isNullOrBlank()) {
            onResult(false, "No email address found")
            return
        }

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(targetEmail).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = _authState.value.copy(currentUser = null)
        observeUserStats(null)
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
