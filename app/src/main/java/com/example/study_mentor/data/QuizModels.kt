package com.example.study_mentor.data

import kotlin.math.roundToInt

/**
 * Question format type supporting scalable assessments created by educators/teachers.
 */
enum class QuestionType {
    MULTIPLE_CHOICE,
    SHORT_ANSWER
}

/**
 * Represents an individual quiz question.
 *
 * @param id Unique identifier for the question.
 * @param text The question prompt presented to the student.
 * @param type The type of question (MULTIPLE_CHOICE or SHORT_ANSWER).
 * @param options List of choices for multiple choice questions, or null for short answer questions.
 * @param correctAnswer The case-insensitive expected answer string.
 */
data class Question(
    val id: String = "",
    val text: String = "",
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val options: List<String>? = null,
    val correctAnswer: String = ""
)

/**
 * Represents a complete Quiz module.
 * Designed to be scalable for future database storage (e.g., Firestore) by Teachers.
 *
 * @param id Unique identifier for the quiz.
 * @param title Title of the quiz (e.g., "Grade 1 Math").
 * @param description Brief description or curriculum guidance.
 * @param maxXp Maximum experience points awarded for a perfect score.
 * @param questions The list of questions included in this quiz.
 */
data class Quiz(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val maxXp: Int = 100,
    val questions: List<Question> = emptyList()
)

/**
 * Utility function to calculate earned XP based on quiz performance.
 *
 * Distributes XP proportionally:
 * E.g., Answering 3 out of 5 questions correctly on a 100 XP quiz returns 60 XP.
 *
 * @param correctAnswers Number of questions correctly answered by the student.
 * @param totalQuestions Total number of questions in the quiz.
 * @param maxXp Maximum XP designated for this quiz.
 * @return Proportionally distributed XP, clamped between 0 and maxXp.
 */
fun calculateEarnedXp(correctAnswers: Int, totalQuestions: Int, maxXp: Int): Int {
    if (totalQuestions <= 0 || maxXp <= 0 || correctAnswers <= 0) return 0
    val safeCorrect = correctAnswers.coerceIn(0, totalQuestions)
    return ((safeCorrect.toDouble() / totalQuestions.toDouble()) * maxXp.toDouble()).roundToInt()
}
