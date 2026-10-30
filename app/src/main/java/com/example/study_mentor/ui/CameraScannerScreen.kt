package com.example.study_mentor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.study_mentor.ui.theme.BackgroundOffWhite
import com.example.study_mentor.ui.theme.PrimaryBlue

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

/**
 * Creates a temporary image file in the application cache and returns a content URI
 * provided by FileProvider for camera capture.
 */
fun createImageUri(context: Context): Uri {
    val tempFile = File.createTempFile(
        "IMG_${System.currentTimeMillis()}_",
        ".jpg",
        context.cacheDir
    ).apply {
        createNewFile()
    }
    return FileProvider.getUriForFile(
        context,
        "com.example.study_mentor.fileprovider",
        tempFile
    )
}

// ─── Main Screen Composable ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScannerScreen(
    onBackClick: () -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by chatViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Clear active chat session when pressing the system back button
    BackHandler {
        chatViewModel.startNewChat()
        onBackClick()
    }

    // Temporary URI holding the target file for camera capture
    var currentCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentCameraUri != null) {
            chatViewModel.onImageSelected(currentCameraUri, context)
        }
    }

    // Photo picker launcher for selecting images from gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        chatViewModel.onImageSelected(uri, context)
    }

    // Auto-scroll to the bottom whenever a new message arrives
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)) // Dark gray/black placeholder for camera
    ) {
        // ── Top Bar ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    chatViewModel.startNewChat()
                    onBackClick()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ask AI",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    chatViewModel.startNewChat()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "New Chat",
                    tint = Color.White
                )
            }
        }

        // ── Camera Bounding Box (only shown when chat is empty) ────────────
        if (uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(200.dp)
                    .align(Alignment.Center)
                    .padding(bottom = 100.dp)
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
            )
        }

        // ── Persistent Bottom Sheet ────────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Chat Message List ──────────────────────────────────────
                if (uiState.messages.isNotEmpty() || uiState.isLoading) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 0.dp, max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.messages) { message ->
                            ChatBubble(message = message)
                        }

                        // Typing indicator — shown while AI is computing
                        if (uiState.isLoading) {
                            item {
                                AiThinkingBubble()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // ── Error Banner ───────────────────────────────────────────
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // ── Section Label (shown only before first message) ────────
                if (uiState.messages.isEmpty()) {
                    Text(
                        text = "OR TYPE YOUR QUESTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                // ── Selected Image Thumbnail Preview ──────────────────────
                if (uiState.selectedImageUri != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(70.dp)
                        ) {
                            AsyncImage(
                                model = uiState.selectedImageUri,
                                contentDescription = "Selected image preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                            )

                            // Small 'X' button to remove the chosen image
                            IconButton(
                                onClick = { chatViewModel.clearSelectedImage() },
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear selected image",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // ── Input Row ─────────────────────────────────────────────
                var text by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Ask something...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = BackgroundOffWhite,
                        unfocusedContainerColor = BackgroundOffWhite
                    ),
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Attach image from gallery",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val uri = createImageUri(context)
                                    currentCameraUri = uri
                                    cameraLauncher.launch(uri)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Capture photo with camera",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        val canSend = (text.isNotBlank() || uiState.selectedImageUri != null) && !uiState.isLoading
                        IconButton(
                            onClick = {
                                if (canSend) {
                                    chatViewModel.sendMessage(text)
                                    text = ""  // Clear the input immediately
                                }
                            },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .background(
                                    if (canSend) PrimaryBlue else Color.LightGray,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )

                // ── Action Pills (hidden when a conversation has started) ──
                if (uiState.messages.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionPill("Solve step-by-step") {
                            chatViewModel.sendMessage("Solve step-by-step: $text")
                            text = ""
                        }
                        ActionPill("Explain concept") {
                            chatViewModel.sendMessage("Explain this concept simply: $text")
                            text = ""
                        }
                        ActionPill("Check my work") {
                            chatViewModel.sendMessage("Check my work and correct any mistakes: $text")
                            text = ""
                        }
                    }
                }

                // Extra bottom padding for safe areas / navigation bar
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ─── Chat Bubble Composable ─────────────────────────────────────────────────

/**
 * Renders a single chat bubble.
 * - User messages: Primary Blue, aligned to the right.
 * - AI messages:   Light gray (#F1F5F9), aligned to the left, full width for
 *                  long step-by-step responses.
 */
@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) PrimaryBlue else Color(0xFFF1F5F9),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else Color(0xFF1E293B),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

// ─── AI Thinking Indicator ──────────────────────────────────────────────────

@Composable
fun AiThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            color = Color(0xFFF1F5F9)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = PrimaryBlue,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "AI is thinking...",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ─── Action Pill Composable ─────────────────────────────────────────────────

@Composable
fun ActionPill(text: String, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFDBEAFE),
        modifier = Modifier.padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Text(
            text = text,
            color = PrimaryBlue,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
