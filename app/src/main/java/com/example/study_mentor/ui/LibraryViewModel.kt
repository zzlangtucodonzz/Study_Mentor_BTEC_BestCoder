package com.example.study_mentor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study_mentor.data.ChatSession
import com.example.study_mentor.data.FirestoreChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for fetching and exposing the user's saved chat sessions from Firestore.
 */
class LibraryViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repository: FirestoreChatRepository = FirestoreChatRepository()
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    /**
     * Checks if a user is currently authenticated and listens to their chat sessions from Firestore.
     */
    fun fetchSessions() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getChatSessions(uid).collect { sessionList ->
                _sessions.value = sessionList
            }
        }
    }
}
