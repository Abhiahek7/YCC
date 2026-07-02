package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboard(
    viewModel: MainViewModel,
    onNavigate: (route: String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val assignedClasses = viewModel.getAssignedClasses()

    val teacherName = when (val user = currentUser) {
        is UserRole.Teacher -> {
            val email = user.email.lowercase().trim()
            when {
                email.contains("teachera") || email.contains("teacher_a") -> "Teacher A"
                email.contains("teacherb") || email.contains("teacher_b") -> "Teacher B"
                email.contains("teacherd") || email.contains("teacher_d") -> "Teacher D"
                email.contains("admin") || email.contains("owner") -> "Principal / Owner"
                else -> "Mr. Verma" // Default
            }
        }
        else -> "Educator"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = teacherName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = BlueDark,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("EduPilot AI", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                            Text("Smart Coaching. Better Learning.", fontSize = 11.sp, color = BluePrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Greetings Header
            Text(
                text = "Welcome back, $teacherName!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BlueDark
            )
            Text(
                text = "Tuition Center & School Management System",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Assigned Classes Badges Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Assigned Classes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        assignedClasses.forEach { cls ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BlueLight)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cls,
                                    color = BlueDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Feature Cards Title
            Text(
                text = "Coaching Modules",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Features cards grid (5 simple large feature cards)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TeacherFeatureCard(
                    title = "📅 Attendance",
                    description = "Take daily attendance, add students & view calendar logs.",
                    testTag = "card_attendance",
                    onClick = { onNavigate("attendance") }
                )

                TeacherFeatureCard(
                    title = "📝 Test Manager",
                    description = "Create and schedule tests, view past evaluations.",
                    testTag = "card_test_manager",
                    onClick = { onNavigate("tests") }
                )

                TeacherFeatureCard(
                    title = "📝 Test Analyzer",
                    description = "Analyze student marks and auto-generate AI insights, strong/weak topics.",
                    testTag = "card_test_analyzer",
                    onClick = { onNavigate("analyzer") }
                )

                TeacherFeatureCard(
                    title = "📊 Reports & Search",
                    description = "Search student profiles to see attendance, grade sheets, and progress.",
                    testTag = "card_reports",
                    onClick = { onNavigate("reports") }
                )

                TeacherFeatureCard(
                    title = "📢 Notice Board",
                    description = "Publish announcements and notices instantly to all classes.",
                    testTag = "card_notices",
                    onClick = { onNavigate("notices") }
                )

                TeacherFeatureCard(
                    title = "⚙️ Settings",
                    description = "Configure language preferences, notifications and account sign out.",
                    testTag = "card_settings",
                    onClick = { onNavigate("settings") }
                )
            }
        }
    }
}

@Composable
fun TeacherFeatureCard(
    title: String,
    description: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = BlueDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
