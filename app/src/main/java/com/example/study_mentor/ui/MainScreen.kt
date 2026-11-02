package com.example.study_mentor.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.study_mentor.ui.theme.PrimaryBlue
import com.example.study_mentor.ui.theme.PrimaryOrange
import com.example.study_mentor.ui.theme.RgbLedColors

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    libraryViewModel: LibraryViewModel = viewModel(),
    practiceViewModel: PracticeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBarRoutes = listOf("login", "register")
    val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes && (currentRoute?.startsWith("quiz_taking") != true)

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomBarWithFab(
                    navController = navController,
                    chatViewModel = chatViewModel
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable("home") { 
                HomeScreen(
                    onNavigateToSignIn = { navController.navigate("login") },
                    onNavigateToAskAI = { initialPrompt ->
                        if (!initialPrompt.isNullOrBlank()) {
                            chatViewModel.startNewChat()
                            chatViewModel.sendMessage(initialPrompt)
                        }
                        navController.navigate("ask_ai") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    authViewModel = authViewModel
                )
            }
            composable("practice") {
                PracticeScreen(
                    onNavigateToQuiz = { quizId ->
                        navController.navigate("quiz_taking/$quizId")
                    },
                    viewModel = practiceViewModel
                )
            }
            composable(
                route = "quiz_taking/{quizId}",
                arguments = listOf(
                    navArgument("quizId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
                QuizTakingScreen(
                    quizId = quizId,
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = practiceViewModel
                )
            }
            composable("ask_ai") { 
                CameraScannerScreen(
                    onBackClick = { navController.popBackStack() },
                    chatViewModel = chatViewModel
                ) 
            }
            composable("library") { 
                LibraryScreen(
                    authViewModel = authViewModel,
                    libraryViewModel = libraryViewModel,
                    onSessionClick = { sessionId ->
                        chatViewModel.loadSession(sessionId)
                        navController.navigate("ask_ai") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login")
                    }
                ) 
            }
            composable("profile") { 
                ProfileScreen(
                    onNavigateToLogin = { navController.navigate("login") },
                    onNavigateToRegister = { navController.navigate("register") },
                    viewModel = authViewModel
                ) 
            }
            composable("login") {
                LoginScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRegister = { 
                        navController.navigate("register") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onAuthSuccess = {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    viewModel = authViewModel
                )
            }
            composable("register") {
                RegisterScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = { 
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onAuthSuccess = {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    viewModel = authViewModel
                )
            }
        }
    }
}

/**
 * Elegant LED-style navigation icon component.
 *
 * Requirements fulfilled:
 * 1. Solid-colored icon (crisp, highly legible in soothing soft orange / white).
 * 2. Continuous, uninterrupted 360-degree rotation without stutter or pause at loop restart.
 * 3. Animated RGB LED ring using Brush.sweepGradient + Modifier.rotate(rotationAngle).
 * 4. Soft ambient RGB drop-shadow / glow behind the icon.
 * 5. Displayed strictly when selected == true.
 */
@Composable
fun RgbLedNavigationIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    selected: Boolean,
    rotationAngle: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(44.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            // 1. Soft ambient RGB drop-shadow / glow behind the icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .rotate(rotationAngle)
                    .graphicsLayer { alpha = 0.35f }
                    .background(
                        brush = Brush.sweepGradient(RgbLedColors),
                        shape = CircleShape
                    )
            )

            // 2. Dynamic RGB LED rotating ring around the icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .rotate(rotationAngle)
                    .background(
                        brush = Brush.sweepGradient(RgbLedColors),
                        shape = CircleShape
                    )
            )

            // 3. Inner surface container (creates a crisp 2.5dp ring thickness and clean backdrop)
            Box(
                modifier = Modifier
                    .size(33.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
            )
        }

        // Solid-colored icon: perfectly sharp and clear
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun BottomBarWithFab(
    navController: NavHostController,
    chatViewModel: ChatViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val chatUiState by chatViewModel.uiState.collectAsState()

    // Smooth, uninterrupted continuous rotation (0f to 360f with LinearEasing & RepeatMode.Restart)
    val infiniteTransition = rememberInfiniteTransition(label = "RgbLedContinuousTransition")
    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RgbLedRotationAngle"
    )

    val isAskAiRoute = currentRoute == "ask_ai"

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // The actual Bottom Navigation Bar
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 16.dp, 
                    spotColor = Color.Black.copy(alpha = 0.08f), 
                    ambientColor = Color.Black.copy(alpha = 0.08f)
                ),
            tonalElevation = 0.dp
        ) {
            // Tab 1: Home
            NavigationBarItem(
                icon = {
                    RgbLedNavigationIcon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        selected = currentRoute == "home",
                        rotationAngle = animatedRotation
                    )
                },
                label = {
                    Text(
                        text = "Home",
                        fontWeight = if (currentRoute == "home") FontWeight.Bold else FontWeight.Medium
                    )
                },
                selected = currentRoute == "home",
                onClick = {
                    if (currentRoute != "home") {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )

            // Tab 2: Practice
            NavigationBarItem(
                icon = {
                    RgbLedNavigationIcon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Practice",
                        selected = currentRoute == "practice",
                        rotationAngle = animatedRotation
                    )
                },
                label = {
                    Text(
                        text = "Practice",
                        fontWeight = if (currentRoute == "practice") FontWeight.Bold else FontWeight.Medium
                    )
                },
                selected = currentRoute == "practice",
                onClick = {
                    if (currentRoute != "practice") {
                        navController.navigate("practice") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )

            // Center space for FAB and Label: Ask AI / Send
            NavigationBarItem(
                icon = { Box(modifier = Modifier.height(24.dp)) },
                label = { 
                    Text(
                        text = if (isAskAiRoute) "Send" else "Ask AI", 
                        fontSize = 12.sp, 
                        fontWeight = if (isAskAiRoute) FontWeight.Bold else FontWeight.Medium
                    ) 
                },
                selected = isAskAiRoute,
                onClick = {
                    if (isAskAiRoute) {
                        if (!chatUiState.isLoading) {
                            if (chatUiState.messages.isEmpty() && chatUiState.selectedImageUri == null) {
                                // Live viewfinder mode: trigger one-tap capture & solve!
                                chatViewModel.triggerCameraCapture()
                            } else {
                                // In active conversation or already selected image: send prompt!
                                chatViewModel.sendMessage()
                            }
                        }
                    } else {
                        navController.navigate("ask_ai") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Transparent,
                    unselectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = Color.Transparent
                )
            )

            // Tab 3: Library
            NavigationBarItem(
                icon = {
                    RgbLedNavigationIcon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Library",
                        selected = currentRoute == "library",
                        rotationAngle = animatedRotation
                    )
                },
                label = {
                    Text(
                        text = "Library",
                        fontWeight = if (currentRoute == "library") FontWeight.Bold else FontWeight.Medium
                    )
                },
                selected = currentRoute == "library",
                onClick = {
                    if (currentRoute != "library") {
                        navController.navigate("library") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )

            // Tab 4: Profile
            NavigationBarItem(
                icon = {
                    RgbLedNavigationIcon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        selected = currentRoute == "profile",
                        rotationAngle = animatedRotation
                    )
                },
                label = {
                    Text(
                        text = "Profile",
                        fontWeight = if (currentRoute == "profile") FontWeight.Bold else FontWeight.Medium
                    )
                },
                selected = currentRoute == "profile",
                onClick = {
                    if (currentRoute != "profile") {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }

        // The overlapping FAB for Ask AI / Contextual Send
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-22).dp)
                .size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            // Rotating RGB LED ring glow around the FAB when on Ask AI
            if (isAskAiRoute) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .rotate(animatedRotation)
                        .background(
                            brush = Brush.sweepGradient(RgbLedColors),
                            shape = CircleShape
                        )
                )
            }

            FloatingActionButton(
                onClick = {
                    if (isAskAiRoute) {
                        // Contextual Enter/Send trigger when already on Ask AI
                        if (!chatUiState.isLoading) {
                            if (chatUiState.messages.isEmpty() && chatUiState.selectedImageUri == null) {
                                // Live viewfinder mode: trigger one-tap capture & solve!
                                chatViewModel.triggerCameraCapture()
                            } else {
                                // In active conversation or already selected image: send prompt!
                                chatViewModel.sendMessage()
                            }
                        }
                    } else {
                        navController.navigate("ask_ai") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier.size(if (isAskAiRoute) 58.dp else 64.dp)
            ) {
                if (isAskAiRoute && chatUiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isAskAiRoute) {
                            Icons.AutoMirrored.Filled.Send
                        } else {
                            Icons.Default.AutoAwesome
                        },
                        contentDescription = if (isAskAiRoute) "Send Message" else "Ask AI",
                        tint = Color.White,
                        modifier = Modifier.size(if (isAskAiRoute) 26.dp else 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
