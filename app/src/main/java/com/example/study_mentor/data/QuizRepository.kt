package com.example.study_mentor.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository interface defining quiz retrieval and management.
 * Designed to be swapped seamlessly between local mock data and a remote Firestore database.
 */
interface QuizRepository {
    /**
     * Retrieves all active quizzes available for students to practice.
     */
    fun getAvailableQuizzes(): Flow<List<Quiz>>

    /**
     * Fetches an individual quiz by its unique identifier.
     */
    suspend fun getQuizById(quizId: String): Quiz?
}

/**
 * Initial Repository implementation with seed quiz data.
 * Ready for future extension with Firestore collections for educator/teacher submissions.
 */
class QuizRepositoryImpl : QuizRepository {

    private val seedQuizzes = listOf(
        Quiz(
            id = "quiz_grade_1_math",
            title = "Grade 1 Math",
            description = "Fundamental addition, subtraction, and basic counting skills.",
            maxXp = 100,
            questions = listOf(
                Question(
                    id = "g1_q1",
                    text = "What is 1 + 1?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("1", "2", "3", "4"),
                    correctAnswer = "2"
                ),
                Question(
                    id = "g1_q2",
                    text = "What is 5 - 2?",
                    type = QuestionType.SHORT_ANSWER,
                    options = null,
                    correctAnswer = "3"
                ),
                Question(
                    id = "g1_q3",
                    text = "Which number comes right after 9?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("8", "10", "11", "12"),
                    correctAnswer = "10"
                ),
                Question(
                    id = "g1_q4",
                    text = "If you have 3 apples and eat 1, how many are left?",
                    type = QuestionType.SHORT_ANSWER,
                    options = null,
                    correctAnswer = "2"
                ),
                Question(
                    id = "g1_q5",
                    text = "How many sides does a triangle have?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("2", "3", "4", "5"),
                    correctAnswer = "3"
                )
            )
        ),
        Quiz(
            id = "quiz_grade_2_math",
            title = "Grade 2 Math",
            description = "Two-digit addition, subtraction, early multiplication, and number patterns.",
            maxXp = 150,
            questions = listOf(
                Question(
                    id = "g2_q1",
                    text = "What is 15 + 12?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("25", "26", "27", "28"),
                    correctAnswer = "27"
                ),
                Question(
                    id = "g2_q2",
                    text = "What is 50 - 18?",
                    type = QuestionType.SHORT_ANSWER,
                    options = null,
                    correctAnswer = "32"
                ),
                Question(
                    id = "g2_q3",
                    text = "What is 4 x 5?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("15", "18", "20", "24"),
                    correctAnswer = "20"
                ),
                Question(
                    id = "g2_q4",
                    text = "What number plus 14 equals 30?",
                    type = QuestionType.SHORT_ANSWER,
                    options = null,
                    correctAnswer = "16"
                ),
                Question(
                    id = "g2_q5",
                    text = "Which of the following is an even number?",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("13", "17", "24", "31"),
                    correctAnswer = "24"
                )
            )
        )
    )

    override fun getAvailableQuizzes(): Flow<List<Quiz>> {
        return flowOf(seedQuizzes)
    }

    override suspend fun getQuizById(quizId: String): Quiz? {
        return seedQuizzes.find { it.id == quizId }
    }
}
