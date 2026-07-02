package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeworkScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val homeworkList by viewModel.allHomework.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Homework", fontWeight = FontWeight.Bold, color = BlueDark) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("st_homework_back")) {
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
            if (homeworkList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No homework assigned.", color = TextMuted)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    homeworkList.forEach { hw ->
                        var isCompletedLocal by remember { mutableStateOf(false) }

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
                                    Column {
                                        Text(hw.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlueDark)
                                        Text("Assigned: ${hw.assignedDate}", fontSize = 11.sp, color = TextMuted)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCompletedLocal) ColorPresent.copy(alpha = 0.12f) else ColorAbsent.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isCompletedLocal) "Completed" else "Due: ${hw.dueDate}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCompletedLocal) ColorPresent else ColorAbsent
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(hw.description, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Attachment placeholder
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
                                        Icon(Icons.Default.Attachment, contentDescription = "PDF", tint = BluePrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("task_details.pdf", fontSize = 12.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                                    }

                                    // Mark Completed button
                                    Button(
                                        onClick = {
                                            isCompletedLocal = !isCompletedLocal
                                            viewModel.markHomeworkCompleted(hw.id, "S101")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCompletedLocal) ColorPresent else BluePrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("mark_comp_${hw.id}")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isCompletedLocal) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = "Status",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isCompletedLocal) "Done" else "Mark Complete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIStudyAssistantScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isSendingChat by viewModel.isSendingChat.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to latest message on change
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = BluePrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("EduPilot AI Assistant", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                            Text("Online Study Coach", fontSize = 11.sp, color = ColorPresent)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BlueDark)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChat() }, modifier = Modifier.testTag("clear_chat_button")) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = TextMuted)
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
        ) {
            // Chat history list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(chatHistory) { chat ->
                    val isUser = chat.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BlueLight)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = BluePrimary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Card(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .testTag(if (isUser) "user_msg_card" else "ai_msg_card"),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) BluePrimary else Color(0xFFF1F5F9)
                            )
                        ) {
                            Text(
                                text = chat.message,
                                fontSize = 13.sp,
                                color = if (isUser) Color.White else TextPrimary,
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 18.sp
                            )
                        }

                        if (isUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("A", fontWeight = FontWeight.Bold, color = BlueDark, fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (isSendingChat) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI is typing a detailed explanation...", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }

            // Quick action assistant chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickChatChip("Explain in Hindi") {
                    viewModel.sendChatMessage("Explain Chapter 4 Mathematics Quadratic Equations in simple Romanized Hindi (Hinglish).")
                }
                QuickChatChip("Solve this Math") {
                    viewModel.sendChatMessage("Solve step-by-step: x^2 - 5x + 6 = 0")
                }
                QuickChatChip("Generate Practice Questions") {
                    viewModel.sendChatMessage("Generate 3 practice questions on Physics Light reflection with answers.")
                }
                QuickChatChip("Exam preparation tips") {
                    viewModel.sendChatMessage("Give me top 3 bulleted preparation tips for terminal exams.")
                }
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Ask study doubts...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (textInput.trim().isNotEmpty()) {
                            viewModel.sendChatMessage(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_chat_fab"),
                    shape = CircleShape,
                    containerColor = BluePrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun QuickChatChip(text: String, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(text, fontSize = 11.sp, color = BlueDark, fontWeight = FontWeight.SemiBold) },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = BlueLight)
    )
}
