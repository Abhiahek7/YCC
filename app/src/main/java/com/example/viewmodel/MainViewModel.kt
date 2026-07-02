package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiRepository
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

sealed class UserRole {
    object Guest : UserRole()
    data class Teacher(val email: String, val name: String = "Mr. Verma") : UserRole()
    data class Student(val rollNumber: String, val name: String = "Arjun Sharma") : UserRole()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = db.dao()
    private val geminiRepository = GeminiRepository()

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<UserRole>(UserRole.Guest)
    val currentUser: StateFlow<UserRole> = _currentUser.asStateFlow()

    // --- Flows from Room Database ---
    val allStudents: StateFlow<List<StudentEntity>> = dao.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHomework: StateFlow<List<HomeworkEntity>> = dao.getAllHomework()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<NoticeEntity>> = dao.getAllNotices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTests: StateFlow<List<TestEntity>> = dao.getAllTests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<MessageEntity>> = dao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatHistory: StateFlow<List<ChatEntity>> = dao.getChatHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudentTestResults: StateFlow<List<StudentTestResultEntity>> = dao.getAllStudentTestResults()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTestResultsForStudent(rollNumber: String): Flow<List<StudentTestResultEntity>> {
        return dao.getTestResultsForStudent(rollNumber)
    }

    // --- Notification & Badge Counter States ---
    private val _unreadAnnouncementsCount = MutableStateFlow(0)
    val unreadAnnouncementsCount: StateFlow<Int> = _unreadAnnouncementsCount.asStateFlow()

    private val _unreadInboxCount = MutableStateFlow(0)
    val unreadInboxCount: StateFlow<Int> = _unreadInboxCount.asStateFlow()

    fun clearAnnouncementsBadge() {
        _unreadAnnouncementsCount.value = 0
    }

    fun clearInboxBadge() {
        _unreadInboxCount.value = 0
    }

    // --- Teacher Assignment Helpers ---
    fun getAssignedClasses(): List<String> {
        val user = _currentUser.value
        if (user is UserRole.Teacher) {
            val email = user.email.lowercase().trim()
            return when {
                email.contains("teachera") || email.contains("teacher_a") -> listOf("LKG", "UKG", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5")
                email.contains("teacherb") || email.contains("teacher_b") -> listOf("Class 6", "Class 7", "Class 8")
                email.contains("teacherd") || email.contains("teacher_d") -> listOf("Class 11", "Class 12")
                email.contains("admin") || email.contains("owner") -> listOf("LKG", "UKG", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12")
                else -> listOf("Class 9", "Class 10") // Default/Teacher C (e.g. mr.verma or teacher@edupilot.ai)
            }
        }
        return emptyList()
    }

    // --- Dynamic Search ---
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery = _studentSearchQuery.asStateFlow()

    val searchedStudents: StateFlow<List<StudentEntity>> = _studentSearchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                dao.getAllStudents()
            } else {
                dao.searchStudents(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI States for AI features ---
    private val _aiResultAnalysis = MutableStateFlow<String>("")
    val aiResultAnalysis: StateFlow<String> = _aiResultAnalysis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _aiStudyPlan = MutableStateFlow<String>("")
    val aiStudyPlan: StateFlow<String> = _aiStudyPlan.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _aiInsights = MutableStateFlow<String>("")
    val aiInsights: StateFlow<String> = _aiInsights.asStateFlow()

    private val _isGeneratingInsights = MutableStateFlow(false)
    val isGeneratingInsights: StateFlow<Boolean> = _isGeneratingInsights.asStateFlow()

    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    // --- Temporary Parent Notification Logs (Simulates Notification Hub) ---
    private val _parentNotifications = MutableStateFlow<List<String>>(emptyList())
    val parentNotifications: StateFlow<List<String>> = _parentNotifications.asStateFlow()

    init {
        // Log basic launch info
        logParentNotification("EduPilot AI Notification Center Activated.")
    }

    fun logParentNotification(message: String) {
        val currentLogs = _parentNotifications.value.toMutableList()
        val timeStamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        currentLogs.add(0, "[$timeStamp] $message")
        _parentNotifications.value = currentLogs
    }

    // --- Authentication Actions ---
    fun loginAsTeacher(email: String) {
        _currentUser.value = UserRole.Teacher(email = email)
        logParentNotification("Teacher $email logged in successfully.")
    }

    fun loginAsStudent(rollNumber: String) {
        viewModelScope.launch {
            val student = dao.getStudentByRoll(rollNumber)
            val name = student?.name ?: "Arjun Sharma"
            _currentUser.value = UserRole.Student(rollNumber = rollNumber, name = name)
            logParentNotification("Student $name (Roll: $rollNumber) logged in successfully.")
        }
    }

    fun logout() {
        _currentUser.value = UserRole.Guest
    }

    fun updateSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    // --- Student Actions ---
    fun addStudent(student: StudentEntity) {
        viewModelScope.launch {
            dao.insertStudent(student)
            logParentNotification("New Student Added: ${student.name} (Roll: ${student.rollNumber})")
        }
    }

    fun editStudent(student: StudentEntity) {
        viewModelScope.launch {
            dao.insertStudent(student)
            logParentNotification("Updated Student Profile: ${student.name}")
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            dao.deleteStudent(student)
            logParentNotification("Removed Student: ${student.name}")
        }
    }

    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>> {
        return dao.getAttendanceByDate(date)
    }

    // --- Attendance Actions ---
    fun submitAttendance(attendanceList: List<AttendanceEntity>) {
        viewModelScope.launch {
            dao.insertAttendance(attendanceList)
            _unreadInboxCount.value += 1
            // Auto trigger Parent Notifications for absentees/lates/presents
            attendanceList.forEach { att ->
                val student = dao.getStudentByRoll(att.studentId)
                student?.let {
                    val statusText = when (att.status) {
                        "Present" -> "is PRESENT today."
                        "Absent" -> "is ABSENT from classes today. Please verify."
                        "Late" -> "arrived LATE to classes today."
                        else -> "attendance marked as ${att.status}."
                    }
                    logParentNotification("PARENT NOTIFICATION sent to ${it.parentName} (${it.parentMobile}): Student ${it.name} $statusText")
                }
            }
            // Add announcement message
            val presentCount = attendanceList.count { it.status == "Present" }
            val absentCount = attendanceList.count { it.status == "Absent" }
            dao.insertMessage(
                MessageEntity(
                    senderName = "EduPilot System",
                    senderRole = "System",
                    content = "Attendance Update: Submitted for ${attendanceList.firstOrNull()?.date ?: "today"}. Present: $presentCount, Absent: $absentCount.",
                    timestamp = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
                )
            )
        }
    }

    // --- Homework Actions ---
    fun createHomework(homework: HomeworkEntity) {
        viewModelScope.launch {
            dao.insertHomework(homework)
            logParentNotification("PARENT NOTIFICATION: New homework assigned for ${homework.className} - Subject: ${homework.subject}. Due: ${homework.dueDate}.")
            
            // Add message
            dao.insertMessage(
                MessageEntity(
                    senderName = "Mr. Verma",
                    senderRole = "Teacher",
                    content = "New Homework assigned in ${homework.subject} for ${homework.className}. Topic: ${homework.description}. Due Date: ${homework.dueDate}.",
                    timestamp = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
                )
            )
        }
    }

    fun markHomeworkCompleted(homeworkId: Int, studentId: String) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dao.insertHomeworkCompletion(
                HomeworkCompletionEntity(
                    homeworkId = homeworkId,
                    studentId = studentId,
                    isCompleted = true,
                    completionDate = dateStr
                )
            )
            logParentNotification("Student $studentId completed Homework #$homeworkId on $dateStr.")
        }
    }

    // --- Notice Actions ---
    fun createNotice(notice: NoticeEntity) {
        viewModelScope.launch {
            dao.insertNotice(notice)
            _unreadAnnouncementsCount.value += 1
            _unreadInboxCount.value += 1
            logParentNotification("PARENT NOTIFICATION: New notice published - '${notice.title}'. Type: ${notice.type}.")
            
            // Add message
            dao.insertMessage(
                MessageEntity(
                    senderName = "EduPilot Office",
                    senderRole = "System",
                    content = "Notice: [${notice.type}] ${notice.title} - ${notice.content}",
                    timestamp = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
                )
            )
        }
    }

    // --- Test Actions ---
    fun createTest(test: TestEntity) {
        viewModelScope.launch {
            dao.insertTest(test)
            _unreadInboxCount.value += 1
            logParentNotification("PARENT NOTIFICATION: Upcoming test scheduled on ${test.testDate} for Chapter: ${test.chapter} (${test.subject}).")
            
            // Add message
            dao.insertMessage(
                MessageEntity(
                    senderName = "System",
                    senderRole = "System",
                    content = "New Test Scheduled: ${test.subject} on Chapter '${test.chapter}'. Max Marks: ${test.maxMarks}. Date: ${test.testDate}.",
                    timestamp = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
                )
            )
        }
    }

    // --- AI Chat Assistant Action ---
    fun sendChatMessage(userText: String) {
        if (userText.trim().isEmpty()) return

        viewModelScope.launch {
            // Save User message
            val userMsg = ChatEntity(role = "user", message = userText)
            dao.insertChat(userMsg)
            _isSendingChat.value = true

            // Gather conversation context
            val history = dao.getChatHistory().firstOrNull() ?: emptyList()
            val promptBuilder = StringBuilder()
            promptBuilder.append("Here is the student's learning query: \"$userText\"\n\n")
            promptBuilder.append("Conversation history for context:\n")
            history.takeLast(6).forEach {
                promptBuilder.append("${it.role}: ${it.message}\n")
            }

            val systemPrompt = """
                You are EduPilot AI, a premium empathetic academic study assistant coach.
                Your tone is highly motivating, clear, and structured (using bold headings, lists).
                Assist the student with queries about chapters, solving step-by-step math or science problems, translating explanations, summarizing notes, or creating simple revisions.
                If the user asks to explain in Hindi, write in simple Romanized Hindi (Hinglish) or Devanagari Hindi. If English, keep it standard and helpful.
                Keep responses detailed, beautiful, and structured.
            """.trimIndent()

            val aiResponse = geminiRepository.generateAIResponse(promptBuilder.toString(), systemPrompt)
            dao.insertChat(ChatEntity(role = "model", message = aiResponse))
            _isSendingChat.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            dao.clearChatHistory()
            dao.insertChat(ChatEntity(role = "model", message = "Chat memory cleared! How can I help you learn today?"))
        }
    }

    // --- AI Result Analyzer Actions ---
    fun analyzeAndStoreResult(
        rollNumber: String,
        studentName: String,
        className: String,
        subject: String,
        testName: String,
        marksObtained: Int,
        totalMarks: Int
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            
            val pct = (marksObtained.toDouble() / totalMarks * 100)
            val percentage = String.format(Locale.getDefault(), "%.1f", pct).toDouble()
            val grade = when {
                percentage >= 90 -> "A+"
                percentage >= 80 -> "A"
                percentage >= 70 -> "B"
                percentage >= 60 -> "C"
                percentage >= 50 -> "D"
                else -> "F"
            }
            
            val defaultStrong = when (subject) {
                "Mathematics" -> "Arithmetic Progression, Linear Equations"
                "Physics" -> "Spherical Mirrors, Lens Formula"
                "Chemistry" -> "Chemical Equations, Balancing reactions"
                "Biology" -> "Plant Hormones, Photosynthesis"
                else -> "Core Concepts, Terminology"
            }
            val defaultWeak = when (subject) {
                "Mathematics" -> "Quadratic Equations, Word Problems"
                "Physics" -> "Refraction Ray Diagrams, Numericals"
                "Chemistry" -> "pH calculations, Acid-Base properties"
                "Biology" -> "Human Nervous System, Diagrams"
                else -> "Advanced Applications, Numerical analytical questions"
            }
            val defaultSuggestions = when (subject) {
                "Mathematics" -> "Practice solving 5 quadratic equations every day. Focus on factorization."
                "Physics" -> "Revise ray diagrams for convex lens. Solve numericals step-by-step."
                "Chemistry" -> "Write out balanced reactions three times. Focus on acid-base standard salts."
                "Biology" -> "Practice labeling nervous system diagrams. Read textbook summaries."
                else -> "Allocate 30 minutes daily to revise key terminology and solve sample worksheets."
            }

            var strongTopics = defaultStrong
            var weakTopics = defaultWeak
            var suggestions = defaultSuggestions

            try {
                val prompt = """
                    Generate educational suggestions for a student who scored $marksObtained out of $totalMarks ($percentage%) in $subject - $testName.
                    Provide a JSON object with strictly these keys:
                    {
                      "strongTopics": "Short list of strong topics",
                      "weakTopics": "Short list of weak topics",
                      "suggestions": "Brief improvement suggestions"
                    }
                    Do not return any markdown formatting or extra text. Only valid JSON.
                """.trimIndent()
                val systemPrompt = "You are an AI Education Expert. Output strictly valid JSON."
                val response = geminiRepository.generateAIResponse(prompt, systemPrompt)
                val cleanResponse = response.replace("```json", "").replace("```", "").trim()
                val json = JSONObject(cleanResponse)
                strongTopics = json.optString("strongTopics", defaultStrong)
                weakTopics = json.optString("weakTopics", defaultWeak)
                suggestions = json.optString("suggestions", defaultSuggestions)
            } catch (e: Exception) {
                // Fallback silently
            }

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val resultEntity = StudentTestResultEntity(
                rollNumber = rollNumber,
                studentName = studentName,
                className = className,
                subject = subject,
                testName = testName,
                marksObtained = marksObtained,
                totalMarks = totalMarks,
                percentage = percentage,
                grade = grade,
                strongTopics = strongTopics,
                weakTopics = weakTopics,
                suggestions = suggestions,
                date = dateStr
            )

            dao.insertStudentTestResult(resultEntity)

            // Trigger notification
            _unreadInboxCount.value += 1
            dao.insertMessage(
                MessageEntity(
                    senderName = "System",
                    senderRole = "System",
                    content = "New Test Result Analyzed: $studentName scored $marksObtained/$totalMarks ($grade) in $subject - $testName.",
                    timestamp = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
                )
            )

            logParentNotification("PARENT NOTIFICATION sent to parents of $studentName: Test Result Analyzed. Score: $marksObtained/$totalMarks ($grade) in $subject.")
            _isAnalyzing.value = false
        }
    }

    // --- AI Result Analyzer Actions ---
    fun analyzeResult(studentId: String, subject: String, recentScore: Int, maxMarks: Int) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val prompt = """
                Perform a deep pedagogical analysis of the following exam result:
                - Student ID: $studentId
                - Subject: $subject
                - Score: $recentScore / $maxMarks
                - Performance ratio: ${(recentScore.toDouble() / maxMarks * 100).toInt()}%
                
                Please generate a structured report covering:
                1. Overall Performance Summary
                2. Subject-wise & Chapter-wise Analysis
                3. Topic-wise Weakness & Strengths
                4. Estimated Accuracy Percentage & Common Pitfalls (e.g., Calculation errors, Formula errors, Time management)
                5. Highly Actionable Improvement Suggestions
                6. Difficulty Analysis & Learning Trend
                
                Structure with bold headers, bulleted lists, and a friendly, encouraging coaching tone.
            """.trimIndent()

            val systemPrompt = "You are a professional educational AI Result Analyzer. Provide a polished analytical report with precise insights."
            val result = geminiRepository.generateAIResponse(prompt, systemPrompt)
            _aiResultAnalysis.value = result
            _isAnalyzing.value = false
        }
    }

    // --- Personalized AI Study Planner Action ---
    fun generateStudyPlan(studentId: String, weakSubjects: List<String>) {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val weakSubjectsStr = weakSubjects.joinToString(", ")
            val prompt = """
                Generate a Personalized 4-Week Study Roadmap for student $studentId.
                Focus on improving performance in: $weakSubjectsStr.
                
                The roadmap must have a highly structured weekly breakdown:
                - Week 1: Topics to Improve, Practice Questions, Revision Goals
                - Week 2: Weak Chapters, Suggested Assignments
                - Week 3: Mock Test Preparation, Focus Areas
                - Week 4: Final Revision, Mock Test & Strategy
                
                Add a section on 'AI Continuous Adaptive Learning' showing how this plan will auto-update after the next quiz score.
                Use bullet points, clear bold headings, and an encouraging tone.
            """.trimIndent()

            val systemPrompt = "You are a personalized academic planner coach. Provide a detailed, highly actionable 4-week roadmap."
            val plan = geminiRepository.generateAIResponse(prompt, systemPrompt)
            _aiStudyPlan.value = plan
            _isGeneratingPlan.value = false
        }
    }

    // --- AI Insights Action (Teacher Overview) ---
    fun generateTeacherInsights(totalStudents: Int, classesToday: Int, presentCount: Int, absentCount: Int) {
        viewModelScope.launch {
            _isGeneratingInsights.value = true
            val prompt = """
                As an AI Coaching Director, analyze today's school coaching metrics and give strategic recommendations:
                - Total Students: $totalStudents
                - Classes Today: $classesToday
                - Present: $presentCount
                - Absent: $absentCount (Attendance Rate: ${(presentCount.toDouble() / totalStudents * 100).toInt()}%)
                
                Generate a 3-part strategic insight:
                1. Attendance Trend & Engagement Analysis
                2. Risk Alert (Action for students missing classes)
                3. Strategic Daily Focus (How the teacher can optimize today's classes and homework assignments)
                
                Keep it highly professional, precise, and immediately actionable. Use bold lists.
            """.trimIndent()

            val systemPrompt = "You are an AI School Director giving expert advisory metrics and insights to teachers."
            val insights = geminiRepository.generateAIResponse(prompt, systemPrompt)
            _aiInsights.value = insights
            _isGeneratingInsights.value = false
        }
    }
}
