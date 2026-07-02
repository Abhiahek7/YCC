package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class StudentTab {
    Attendance,
    TestManager,
    TestAnalyzer,
    Announcements,
    Inbox,
    Profile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    viewModel: MainViewModel,
    onNavigate: (route: String) -> Unit
) {
    var currentTab by remember { mutableStateOf(StudentTab.Attendance) }

    val unreadAnnouncements by viewModel.unreadAnnouncementsCount.collectAsState()
    val unreadInbox by viewModel.unreadInboxCount.collectAsState()

    // Whenever we land on Announcements, auto clear its badge
    LaunchedEffect(currentTab) {
        if (currentTab == StudentTab.Announcements) {
            viewModel.clearAnnouncementsBadge()
        }
        if (currentTab == StudentTab.Inbox) {
            viewModel.clearInboxBadge()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("student_bottom_navigation"),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Tab 1: Attendance
                NavigationBarItem(
                    selected = currentTab == StudentTab.Attendance,
                    onClick = { currentTab = StudentTab.Attendance },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Attendance") },
                    label = { Text("Attendance", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_student_attendance")
                )

                // Tab 2: Test Manager
                NavigationBarItem(
                    selected = currentTab == StudentTab.TestManager,
                    onClick = { currentTab = StudentTab.TestManager },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = "Tests") },
                    label = { Text("Tests", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_student_tests")
                )

                // Tab 3: Test Analyzer
                NavigationBarItem(
                    selected = currentTab == StudentTab.TestAnalyzer,
                    onClick = { currentTab = StudentTab.TestAnalyzer },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Analyzer") },
                    label = { Text("Analyzer", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_student_analyzer")
                )

                // Tab 4: Announcements
                NavigationBarItem(
                    selected = currentTab == StudentTab.Announcements,
                    onClick = { currentTab = StudentTab.Announcements },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadAnnouncements > 0) {
                                    Badge(containerColor = ColorAbsent) {
                                        Text(unreadAnnouncements.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = "Announcements")
                        }
                    },
                    label = { Text("Announcements", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_student_announcements")
                )

                // Tab 5: Inbox
                NavigationBarItem(
                    selected = currentTab == StudentTab.Inbox,
                    onClick = { currentTab = StudentTab.Inbox },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadInbox > 0) {
                                    Badge(containerColor = ColorAbsent) {
                                        Text(unreadInbox.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Mail, contentDescription = "Inbox")
                        }
                    },
                    label = { Text("Inbox", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_student_inbox")
                )

                // Tab 6: Profile
                NavigationBarItem(
                    selected = currentTab == StudentTab.Profile,
                    onClick = { currentTab = StudentTab.Profile },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_student_profile")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                StudentTab.Attendance -> StudentAttendanceView(viewModel)
                StudentTab.TestManager -> StudentTestManagerView(viewModel)
                StudentTab.TestAnalyzer -> StudentTestAnalyzerView(viewModel)
                StudentTab.Announcements -> StudentAnnouncementsView(viewModel)
                StudentTab.Inbox -> StudentInboxView(viewModel)
                StudentTab.Profile -> StudentProfileView(viewModel, onLogout = { onNavigate("login") })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceView(viewModel: MainViewModel) {
    val dateStrForToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var selectedCalDate by remember { mutableStateOf(dateStrForToday) }
    
    // Fetch student attendance list
    val dbAttendance by viewModel.getAttendanceByDate(selectedCalDate).collectAsState(initial = emptyList())
    val myAttendance = dbAttendance.firstOrNull { it.studentId == "S101" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📅 Attendance Ledger", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
        Text("Track your daily presence and overall coaching classes consistency.", fontSize = 13.sp, color = TextSecondary)

        // Today's Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Today's Status", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BlueDark)
                    Text(dateStrForToday, fontSize = 12.sp, color = TextSecondary)
                }
                
                val todayStatus = myAttendance?.status ?: "Present"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (todayStatus == "Present") ColorPresent else ColorAbsent)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(todayStatus, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Attendance Percentage and Rate Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = "Trend", tint = BluePrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Monthly Attendance Rate", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BlueDark)
                        Text("Classes attended in current term", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Text("92.5%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlueDark)
            }
        }

        // Interactive Calendar View
        Text("Calendar View", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlueDark)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("July 2026", fontWeight = FontWeight.Bold, color = BlueDark, fontSize = 14.sp)
                    Text("Select any date to check status", fontSize = 11.sp, color = TextMuted)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Days of week header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                        Text(
                            text = it,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Interactive days grid
                val daysList = (1..31).toList()
                var currentWeek = mutableListOf<Int?>()
                val weeks = mutableListOf<List<Int?>>()

                // Adding empty slots for calendar starting offset (e.g., Wednesday start)
                currentWeek.add(null)
                currentWeek.add(null)

                daysList.forEach { day ->
                    currentWeek.add(day)
                    if (currentWeek.size == 7) {
                        weeks.add(currentWeek)
                        currentWeek = mutableListOf()
                    }
                }
                if (currentWeek.isNotEmpty()) {
                    while (currentWeek.size < 7) {
                        currentWeek.add(null)
                    }
                    weeks.add(currentWeek)
                }

                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        week.forEach { day ->
                            if (day == null) {
                                Spacer(modifier = Modifier.size(36.dp))
                            } else {
                                val dayStr = String.format(Locale.getDefault(), "2026-07-%02d", day)
                                val isSelected = selectedCalDate == dayStr
                                
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) BluePrimary else Color.Transparent)
                                        .clickable { selectedCalDate = dayStr }
                                        .testTag("day_cell_$day"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Selected Date Status", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedCalDate, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BlueDark)
                    
                    // Mock statuses for dates to make it visually real
                    val queryStatus = myAttendance?.status ?: when (selectedCalDate) {
                        "2026-07-01" -> "Present"
                        "2026-07-02" -> "Present"
                        "2026-07-03" -> "Absent"
                        "2026-07-04" -> "Present"
                        else -> "Present"
                    }
                    Text(
                        text = queryStatus,
                        fontWeight = FontWeight.Bold,
                        color = if (queryStatus == "Present") ColorPresent else ColorAbsent,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTestManagerView(viewModel: MainViewModel) {
    val dbTests by viewModel.allTests.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📝 Test Management Ledger", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
        Text("View schedules and syllabus details for upcoming and completed coaching evaluations.", fontSize = 13.sp, color = TextSecondary)

        // Upcoming Tests List
        Text("Upcoming Tests", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlueDark)

        if (dbTests.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No upcoming tests scheduled.", color = TextMuted)
            }
        } else {
            dbTests.forEach { test ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(test.subject, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BlueDark)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ColorAbsent.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(test.testDate, color = ColorAbsent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Chapter: ${test.chapter}", fontSize = 13.sp, color = TextSecondary)
                        Text("Maximum Marks: ${test.maxMarks}", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        }

        // Previous Tests History (Static mock for perfect UI design)
        Text("Previous Term Tests History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlueDark)

        val previousTests = listOf(
            Pair("Mathematics", "June 25, 2026"),
            Pair("Physics", "June 18, 2026"),
            Pair("Chemistry", "June 10, 2026")
        )

        previousTests.forEach { test ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(test.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BlueDark)
                        Text("Mock Evaluation", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text(test.second, fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTestAnalyzerView(viewModel: MainViewModel) {
    val dbResults by viewModel.getTestResultsForStudent("S101").collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📊 Test Results & AI Analyzer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
        Text("Pedagogical overview of your marks, weak areas, and study plans.", fontSize = 13.sp, color = TextSecondary)

        if (dbResults.isEmpty()) {
            // High-fidelity standard mock analysis when database has no entry yet
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mathematics - Mock Test 1", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BlueDark)
                            Text("June 28, 2026", fontSize = 11.sp, color = TextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BlueLight)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("A (88.0%)", color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("🎯 Strong Topics", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorPresent)
                    Text("Arithmetic Progression, Matrices", fontSize = 13.sp, color = TextPrimary, modifier = Modifier.padding(bottom = 12.dp))

                    Text("⚠️ Weak Topics", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorAbsent)
                    Text("Word problems in Quadratic equations", fontSize = 13.sp, color = TextPrimary, modifier = Modifier.padding(bottom = 12.dp))

                    Text("💬 Teacher Remarks", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BlueDark)
                    Text("Good grasp of quadratic formula, needs to read word problems slower to convert them to equations.", fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
                }
            }
        } else {
            dbResults.forEach { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${result.subject} - ${result.testName}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BlueDark)
                                Text(result.date, fontSize = 11.sp, color = TextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BlueLight)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("${result.grade} (${result.percentage}%)", color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("🎯 Strong Topics", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorPresent)
                        Text(result.strongTopics, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.padding(bottom = 12.dp))

                        Text("⚠️ Weak Topics", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorAbsent)
                        Text(result.weakTopics, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.padding(bottom = 12.dp))

                        Text("💬 Suggestions & Remarks", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BlueDark)
                        Text(result.suggestions, fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAnnouncementsView(viewModel: MainViewModel) {
    val notices by viewModel.allNotices.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📢 Notice Board & Alerts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
        Text("Keep up with general administrative announcements and holiday lists.", fontSize = 13.sp, color = TextSecondary)

        if (notices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No official announcements posted.", color = TextMuted)
            }
        } else {
            notices.forEach { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BlueDark)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BlueLight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(notice.type, color = BlueDark, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notice.date, fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(notice.content, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentInboxView(viewModel: MainViewModel) {
    val messages by viewModel.allMessages.collectAsState()
    
    // We maintain a local read state map of message IDs we clicked in this session
    val readMessageIds = remember { mutableStateListOf<Int>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📥 Academic Inbox", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
        Text("View system updates, attendance notifications, and test submissions. Click to read.", fontSize = 13.sp, color = TextSecondary)

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Inbox is empty.", color = TextMuted)
            }
        } else {
            messages.forEach { msg ->
                val isRead = readMessageIds.contains(msg.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isRead) {
                                readMessageIds.add(msg.id)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRead) Color(0xFFF8FAFC) else Color(0xFFEFF6FF)
                    ),
                    border = BorderStroke(1.dp, if (isRead) Color(0xFFE2E8F0) else Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Unread indicator dot
                        if (!isRead) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp, end = 8.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BluePrimary)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BlueDark)
                                Text(msg.timestamp, fontSize = 11.sp, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(msg.content, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentProfileView(viewModel: MainViewModel, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Large Profile Avatar
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(BlueLight),
            contentAlignment = Alignment.Center
        ) {
            Text("A", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = BlueDark)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Arjun Sharma", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BlueDark)
            Text("Student • Roll No: S101", fontSize = 13.sp, color = BluePrimary, fontWeight = FontWeight.SemiBold)
        }

        Divider()

        // Details Block
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileRowItem(label = "Target Class", value = "Class 10")
                ProfileRowItem(label = "Assigned Section", value = "Section A")
                ProfileRowItem(label = "Parent Name", value = "Karan Sharma")
                ProfileRowItem(label = "Parent Mobile", value = "+91 98765 43210")
                ProfileRowItem(label = "Registered Academy", value = "St. Xavier's Academy")
                ProfileRowItem(label = "Address", value = "New Delhi, India")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LOGOUT BUTTON
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_student_logout"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorAbsent)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Log Out")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out Student Account", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, color = BlueDark, fontWeight = FontWeight.Bold)
    }
}
