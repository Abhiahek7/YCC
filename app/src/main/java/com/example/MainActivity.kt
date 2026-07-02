package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.UserRole

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EduPilotApp(viewModel)
            }
        }
    }
}

@Composable
fun EduPilotApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTeacher = currentUser is UserRole.Teacher

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom navigation on dashboards and primary screens for Teachers, once logged in
            if (isTeacher && currentRoute != null && currentRoute != "splash" && currentRoute != "login") {
                NavigationBar(
                    modifier = Modifier.testTag("app_navigation_bar")
                ) {
                    // Teacher Bottom Navigation Tabs
                    NavigationBarItem(
                        selected = currentRoute == "teacher_dashboard",
                        onClick = { navController.navigate("teacher_dashboard") { popUpTo("teacher_dashboard") { inclusive = false } } },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_teacher_home")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "notices",
                        onClick = { navController.navigate("notices") },
                        icon = { Icon(Icons.Default.Campaign, contentDescription = "Notices") },
                        label = { Text("Notices") },
                        modifier = Modifier.testTag("nav_teacher_notices")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "tests",
                        onClick = { navController.navigate("tests") },
                        icon = { Icon(Icons.Default.Quiz, contentDescription = "Tests") },
                        label = { Text("Tests") },
                        modifier = Modifier.testTag("nav_teacher_tests")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "messages",
                        onClick = { navController.navigate("messages") },
                        icon = { Icon(Icons.Default.Message, contentDescription = "Announcements") },
                        label = { Text("Inbox") },
                        modifier = Modifier.testTag("nav_teacher_messages")
                    )
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = { navController.navigate("settings") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_teacher_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen
            composable("splash") {
                SplashScreen(
                    onSplashComplete = {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            // Login Screen
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { isTeacherLogin ->
                        if (isTeacherLogin) {
                            navController.navigate("teacher_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            navController.navigate("student_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Teacher Dashboard Screen
            composable("teacher_dashboard") {
                TeacherDashboard(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Student Dashboard Screen
            composable("student_dashboard") {
                StudentDashboard(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Student Directory (Management) Screen
            composable("students") {
                StudentManagementScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Take Attendance Screen
            composable("attendance") {
                AttendanceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Homework Module Screen
            composable("homework") {
                if (isTeacher) {
                    CreateHomeworkScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    StudentHomeworkScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // Notice Board Screen
            composable("notices") {
                NoticeBoardScreen(
                    viewModel = viewModel,
                    isTeacher = isTeacher,
                    onBack = { navController.popBackStack() }
                )
            }

            // Test Schedule & Manager Screen
            composable("tests") {
                TestsManagementScreen(
                    viewModel = viewModel,
                    isTeacher = isTeacher,
                    onBack = { navController.popBackStack() }
                )
            }

            // AI Result Analyzer Screen
            composable("analyzer") {
                AIResultAnalyzerScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // AI Study Planner Screen
            composable("planner") {
                AIStudyPlannerScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // AI Study Assistant Chatbot Screen
            composable("assistant") {
                AIStudyAssistantScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Teacher Reports Screen
            composable("reports") {
                TeacherReportsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Calendar Screen
            composable("calendar") {
                CalendarScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Messages/Announcements Screen
            composable("messages") {
                MessagesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Profile Screen
            composable("profile") {
                ProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Settings Screen
            composable("settings") {
                SettingsScreen(
                    onLogout = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
