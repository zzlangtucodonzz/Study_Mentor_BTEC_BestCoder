package com.example.study_mentor.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study_mentor.data.GamificationRepository
import com.example.study_mentor.data.Quiz
import com.example.study_mentor.data.QuizRepository
import com.example.study_mentor.data.QuizRepositoryImpl
import com.example.study_mentor.data.calculateEarnedXp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Event emitted when a user finishes a quiz or earns XP.
 */
data class XpRewardEvent(
    val earnedXp: Int,
    val quizTitle: String,
    val difficulty: String
)

/**
 * ViewModel managing practice quizzes, dynamic question models, and XP reward calculations.
 */
class PracticeViewModel(
    private val quizRepository: QuizRepository = QuizRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val gamificationRepository: GamificationRepository = GamificationRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "PracticeViewModel"
        const val BASE_QUIZ_XP = 50

        // Standard difficulty multipliers for RPG XP scaling
        const val MULTIPLIER_EASY = 1.0f
        const val MULTIPLIER_MEDIUM = 1.5f
        const val MULTIPLIER_HARD = 2.0f
    }

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _rewardEvents = MutableSharedFlow<XpRewardEvent>()
    val rewardEvents: SharedFlow<XpRewardEvent> = _rewardEvents.asSharedFlow()

    init {
        loadQuizzes()
    }

    /**
     * Observes available quizzes from QuizRepository.
     */
    fun loadQuizzes() {
        viewModelScope.launch {
            quizRepository.getAvailableQuizzes().collect { quizList ->
                _quizzes.value = quizList
            }
        }
    }

    /**
     * Submits results for a completed quiz, proportionally calculates earned XP,
     * logs/prints the output, and securely persists the earned XP to the Gamification database.
     *
     * @param quizId The ID of the quiz completed.
     * @param correctAnswers The number of correct answers achieved.
     * @return The calculated earned XP.
     */
    fun submitQuizResults(quizId: String, correctAnswers: Int): Int {
        val quiz = _quizzes.value.find { it.id == quizId }
        val totalQuestions = quiz?.questions?.size ?: 5
        val maxXp = quiz?.maxXp ?: 100

        val earnedXp = calculateEarnedXp(
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions,
            maxXp = maxXp
        )

        val logMessage = "Submitted Quiz '$quizId': $correctAnswers/$totalQuestions correct. Earned: $earnedXp XP (Max: $maxXp XP)"
        Log.d(TAG, logMessage)
        println(logMessage)

        val uid = auth.currentUser?.uid
        if (!uid.isNullOrBlank() && earnedXp > 0) {
            viewModelScope.launch {
                try {
                    gamificationRepository.addXp(uid, earnedXp)
                    _rewardEvents.emit(
                        XpRewardEvent(
                            earnedXp = earnedXp,
                            quizTitle = quiz?.title ?: "Practice Quiz",
                            difficulty = "Normal"
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to persist earned XP to GamificationRepository", e)
                }
            }
        }

        return earnedXp
    }

    /**
     * Resolves difficulty text ("Easy", "Medium", "Hard") into its corresponding RPG coefficient multiplier.
     */
    fun getMultiplierForDifficulty(difficulty: String): Float {
        return when (difficulty.lowercase().trim()) {
            "easy" -> MULTIPLIER_EASY
            "medium" -> MULTIPLIER_MEDIUM
            "hard" -> MULTIPLIER_HARD
            else -> 1.0f
        }
    }

    /**
     * Awards RPG XP when a practice quiz is completed with a multiplier.
     */
    fun completeQuiz(
        quizTitle: String = "Practice Quiz",
        difficulty: String = "Medium",
        difficultyMultiplier: Float = getMultiplierForDifficulty(difficulty),
        baseXp: Int = BASE_QUIZ_XP,
        onComplete: (Int) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Log.w(TAG, "Cannot award XP: User is not authenticated.")
            onComplete(0)
            return
        }

        val finalEarnedXp = (baseXp * difficultyMultiplier).toInt().coerceAtLeast(1)

        viewModelScope.launch {
            try {
                gamificationRepository.addXp(uid, finalEarnedXp)
                _rewardEvents.emit(
                    XpRewardEvent(
                        earnedXp = finalEarnedXp,
                        quizTitle = quizTitle,
                        difficulty = difficulty
                    )
                )
                onComplete(finalEarnedXp)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to award quiz completion XP", e)
                onComplete(0)
            }
        }
    }

    /**
     * Overload accepting an explicit userId for flexible cross-module calling.
     */
    fun awardQuizCompletionXp(
        userId: String,
        baseXp: Int = BASE_QUIZ_XP,
        difficultyMultiplier: Float = 1.0f,
        onComplete: (Int) -> Unit = {}
    ) {
        if (userId.isBlank()) {
            onComplete(0)
            return
        }

        val finalEarnedXp = (baseXp * difficultyMultiplier).toInt().coerceAtLeast(1)

        viewModelScope.launch {
            try {
                gamificationRepository.addXp(userId, finalEarnedXp)
                onComplete(finalEarnedXp)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to award XP directly for user $userId", e)
                onComplete(0)
            }
        }
    }
}
