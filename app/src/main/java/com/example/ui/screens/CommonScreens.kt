package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.example.data.NoticeEntity
import com.example.data.TestEntity
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeBoardScreen(
    viewModel: MainViewModel,
    isTeacher: Boolean,
    onBack: () -> Unit
) {
    val notices by viewModel.allNotices.collectAsState()
    var showCreateNoticeDialog by remember { mutableStateOf(false) }

    // Create Notice Form
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("General") } // "General", "Holiday", "Exam", "Emergency"
    var selectedClass by remember { mutableStateOf("Class 10") }
    var attachmentName by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notice Board", fontWeight = FontWeight.Bold, color = BlueDark) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notice_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BlueDark)
                    }
                },
                actions = {
                    if (isTeacher) {
                        IconButton(onClick = { showCreateNoticeDialog = true }, modifier = Modifier.testTag("create_notice_icon")) {
                            Icon(Icons.Default.Add, contentDescription = "Add Notice", tint = BluePrimary)
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
                .padding(16.dp)
        ) {
            if (notices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notices posted.", color = TextMuted)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    notices.forEach { notice ->
                        val badgeColor = when (notice.type) {
                            "Emergency" -> ColorAbsent
                            "Holiday" -> ColorPresent
                            "Exam" -> ColorLate
                            else -> BluePrimary
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(badgeColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(notice.type, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(notice.date, fontSize = 11.sp, color = TextMuted)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlueDark)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(notice.content, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showCreateNoticeDialog = false },
            title = { Text("Publish Announcement", fontWeight = FontWeight.Bold, color = BlueDark) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("notice_title_input")
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("notice_desc_input")
                    )
                    
                    // Category Selection
                    Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("General", "Holiday", "Exam", "Emergency").forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type) }
                            )
                        }
                    }

                    // Class Selection
                    Text("Target Class Selection", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    val classes = viewModel.getAssignedClasses()
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (if (classes.isEmpty()) listOf("Class 10", "Class 9") else classes).forEach { cls ->
                            FilterChip(
                                selected = selectedClass == cls,
                                onClick = { selectedClass = cls },
                                label = { Text(cls) },
                                modifier = Modifier.testTag("notice_class_$cls")
                            )
                        }
                    }

                    // Attachment Mockup (Optional)
                    Text("Attachment (Optional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    if (attachmentName == null) {
                        OutlinedButton(
                            onClick = { attachmentName = "coaching_schedule_doc.pdf" },
                            modifier = Modifier.fillMaxWidth().testTag("notice_attach_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachFile, contentDescription = "Attach", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Attach PDF or Image Document", fontSize = 12.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BlueLight)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = "PDF", tint = BluePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(attachmentName!!, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                            }
                            IconButton(onClick = { attachmentName = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = ColorAbsent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty()) {
                            // Prepend class selection to the title for perfect student-side sorting
                            val finalTitle = "[$selectedClass] $title"
                            val finalContent = if (attachmentName != null) {
                                "$content\n\n📎 Attached Document: $attachmentName"
                            } else {
                                content
                            }

                            viewModel.createNotice(
                                NoticeEntity(
                                    title = finalTitle,
                                    content = finalContent,
                                    type = selectedType,
                                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                )
                            )
                            showCreateNoticeDialog = false
                            title = ""
                            content = ""
                            attachmentName = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    modifier = Modifier.testTag("notice_publish_btn")
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateNoticeDialog = false }, modifier = Modifier.testTag("notice_cancel_btn")) {
                    Text("Cancel", color = BluePrimary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsManagementScreen(
    viewModel: MainViewModel,
    isTeacher: Boolean,
    onBack: () -> Unit
) {
    val tests by viewModel.allTests.collectAsState()
    var showCreateTestDialog by remember { mutableStateOf(false) }

    // Test Creation states
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("") }
    var maxMarks by remember { mutableStateOf("50") }
    var testDate by remember { mutableStateOf("2026-07-15") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isTeacher) "Test Manager" else "Test & Results", fontWeight = FontWeight.Bold, color = BlueDark) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tests_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BlueDark)
                    }
                },
                actions = {
                    if (isTeacher) {
                        IconButton(onClick = { showCreateTestDialog = true }, modifier = Modifier.testTag("create_test_icon")) {
                            Icon(Icons.Default.Add, contentDescription = "Schedule Test", tint = BluePrimary)
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
                .padding(16.dp)
        ) {
            if (tests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tests scheduled.", color = TextMuted)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    tests.forEach { test ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(test.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlueDark)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ColorLate.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Max: ${test.maxMarks}", color = ColorLate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Chapter: ${test.chapter}", fontSize = 13.sp, color = TextSecondary)
                                Text("Exam Date: ${test.testDate}", fontSize = 12.sp, color = TextMuted)

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Attachments", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Download, contentDescription = "Download Paper", tint = BluePrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Question", fontSize = 11.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Download, contentDescription = "Download Key", tint = ColorPresent, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Answer Key", fontSize = 11.sp, color = ColorPresent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateTestDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTestDialog = false },
            title = { Text("Schedule New Test", fontWeight = FontWeight.Bold, color = BlueDark) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, singleLine = true)
                    OutlinedTextField(value = chapter, onValueChange = { chapter = it }, label = { Text("Chapter / Unit") }, singleLine = true)
                    OutlinedTextField(value = maxMarks, onValueChange = { maxMarks = it }, label = { Text("Maximum Marks") }, singleLine = true)
                    OutlinedTextField(value = testDate, onValueChange = { testDate = it }, label = { Text("Test Date (yyyy-MM-dd)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chapter.isNotEmpty()) {
                            viewModel.createTest(
                                TestEntity(
                                    subject = subject,
                                    chapter = chapter,
                                    maxMarks = maxMarks.toIntOrNull() ?: 50,
                                    testDate = testDate,
                                    marksJson = "{}"
                                )
                            )
                            showCreateTestDialog = false
                            chapter = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTestDialog = false }) {
                    Text("Cancel", color = BluePrimary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold, color = BlueDark) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("calendar_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BlueDark)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant calendar headers
            Text("July 2026", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
            Spacer(modifier = Modifier.height(16.dp))

            // Grid layout drawing representing a calendar monthly grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Week headers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                            Text(it, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Simple simulated dates rows
                    for (row in 0..4) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 1..7) {
                                val day = row * 7 + col - 3
                                val isToday = day == 2 // July 2nd
                                val isEvent = day == 3 || day == 15
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(if (isToday) BluePrimary else if (isEvent) BlueLight else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day in 1..31) {
                                        Text(
                                            text = "$day",
                                            fontSize = 13.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) Color.White else if (isEvent) BlueDark else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Calendar events schedule
            Text("Upcoming Events", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EventItem("Monsoon Holiday", "July 3rd • Full Day", ColorPresent)
                    EventItem("Mid-Term Examinations Start", "July 15th • 09:00 AM", ColorLate)
                }
            }
        }
    }
}

@Composable
fun EventItem(title: String, time: String, dotColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Text(time, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.allMessages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcement Board", fontWeight = FontWeight.Bold, color = BlueDark) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("messages_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BlueDark)
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
                .padding(16.dp)
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No announcements.", color = TextMuted)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    messages.forEach { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimary)
                                    Text(msg.timestamp, fontSize = 11.sp, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(msg.content, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = BlueDark) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BlueDark)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Text("E", fontWeight = FontWeight.Bold, color = BlueDark, fontSize = 36.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("EduPilot Member", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Active Account", fontSize = 13.sp, color = ColorPresent, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(32.dp))

            ProfileField(Icons.Default.Person, "Full Name", "Arjun Sharma")
            ProfileField(Icons.Default.Email, "Email Address", "arjun.sharma@edupilot.ai")
            ProfileField(Icons.Default.Phone, "Mobile Number", "+91 98765 43210")
            ProfileField(Icons.Default.School, "Institute", "St. Xavier's Academy")
        }
    }
}

@Composable
fun ProfileField(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = BluePrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMuted)
            Text(value, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var isHindi by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = BlueDark) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BlueDark)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("General Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            
            // Language Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Language Selection", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text(if (isHindi) "Hindi Selected" else "English Selected", fontSize = 12.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = isHindi,
                        onCheckedChange = { isHindi = it },
                        modifier = Modifier.testTag("toggle_language"),
                        colors = SwitchDefaults.colors(checkedThumbColor = BluePrimary)
                    )
                }
            }

            // Push Notifications
            var notifEnabled by remember { mutableStateOf(true) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("App Notifications", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text("Receive alerts for Homework & Tests", fontSize = 12.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = notifEnabled,
                        onCheckedChange = { notifEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = BluePrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = ColorAbsent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button")
            ) {
                Text("Logout From EduPilot", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
