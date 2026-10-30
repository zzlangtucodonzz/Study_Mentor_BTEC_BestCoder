package com.example.study_mentor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.study_mentor.ui.MainScreen
import com.example.study_mentor.ui.theme.Study_MentorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Study_MentorTheme {
                MainScreen()
            }
        }
    }
}