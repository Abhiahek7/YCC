package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// --- 1. Entities ---

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val rollNumber: String,
    val name: String,
    val className: String,
    val section: String,
    val parentName: String,
    val parentMobile: String,
    val schoolName: String,
    val address: String
)

@Entity(
    tableName = "attendance",
    primaryKeys = ["studentId", "date"]
)
data class AttendanceEntity(
    val studentId: String,
    val date: String, // yyyy-MM-dd
    val status: String // "Present", "Absent", "Late"
)

@Entity(tableName = "homework")
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val className: String,
    val subject: String,
    val description: String,
    val dueDate: String,
    val attachmentUrl: String? = null,
    val assignedDate: String
)

@Entity(
    tableName = "homework_completions",
    primaryKeys = ["homeworkId", "studentId"]
)
data class HomeworkCompletionEntity(
    val homeworkId: Int,
    val studentId: String,
    val isCompleted: Boolean = false,
    val completionDate: String? = null
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val type: String, // "General", "Holiday", "Exam", "Emergency"
    val date: String
)

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val chapter: String,
    val maxMarks: Int,
    val testDate: String,
    val questionPaperUrl: String? = null,
    val answerKeyUrl: String? = null,
    val marksJson: String // Serialized Map<studentId, Int>
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val senderRole: String, // "Teacher" or "System"
    val content: String,
    val timestamp: String
)

@Entity(tableName = "chat_history")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_test_results")
data class StudentTestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rollNumber: String,
    val studentName: String,
    val className: String,
    val subject: String,
    val testName: String,
    val marksObtained: Int,
    val totalMarks: Int,
    val percentage: Double,
    val grade: String,
    val strongTopics: String,
    val weakTopics: String,
    val suggestions: String,
    val date: String
)

// --- 2. DAO ---

@Dao
interface EduPilotDao {
    // Students
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE rollNumber = :rollNumber LIMIT 1")
    suspend fun getStudentByRoll(rollNumber: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<StudentEntity>>

    // Attendance
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId")
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: List<AttendanceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleAttendance(attendance: AttendanceEntity)

    // Homework
    @Query("SELECT * FROM homework ORDER BY assignedDate DESC")
    fun getAllHomework(): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homework WHERE className = :className ORDER BY assignedDate DESC")
    fun getHomeworkForClass(className: String): Flow<List<HomeworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkEntity)

    // Homework Completions
    @Query("SELECT * FROM homework_completions WHERE studentId = :studentId")
    fun getCompletionsForStudent(studentId: String): Flow<List<HomeworkCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeworkCompletion(completion: HomeworkCompletionEntity)

    // Notices
    @Query("SELECT * FROM notices ORDER BY date DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    // Tests
    @Query("SELECT * FROM tests ORDER BY testDate DESC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity)

    // Messages
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    // Chat History
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("DELETE FROM chat_history")
    suspend fun clearChatHistory()

    // Student Test Results
    @Query("SELECT * FROM student_test_results ORDER BY date DESC")
    fun getAllStudentTestResults(): Flow<List<StudentTestResultEntity>>

    @Query("SELECT * FROM student_test_results WHERE rollNumber = :rollNumber ORDER BY date DESC")
    fun getTestResultsForStudent(rollNumber: String): Flow<List<StudentTestResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentTestResult(result: StudentTestResultEntity)
}

// --- 3. Database & Pre-population ---

@Database(
    entities = [
        StudentEntity::class,
        AttendanceEntity::class,
        HomeworkEntity::class,
        HomeworkCompletionEntity::class,
        NoticeEntity::class,
        TestEntity::class,
        MessageEntity::class,
        ChatEntity::class,
        StudentTestResultEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): EduPilotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edupilot_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.dao())
                }
            }
        }

        private suspend fun populateInitialData(dao: EduPilotDao) {
            // 1. Initial Students
            val students = listOf(
                StudentEntity("S101", "Arjun Sharma", "Class 10", "A", "Rajesh Sharma", "+91 98765 43210", "St. Xavier's Academy", "Sector 15, Dwarka, New Delhi"),
                StudentEntity("S102", "Ananya Verma", "Class 10", "A", "Sanjay Verma", "+91 98765 43211", "St. Xavier's Academy", "Pocket C, Vikaspuri, New Delhi"),
                StudentEntity("S103", "Kabir Mehta", "Class 10", "A", "Alok Mehta", "+91 98765 43212", "St. Xavier's Academy", "Golf Course Road, Gurugram"),
                StudentEntity("S104", "Meera Sen", "Class 10", "B", "Amit Sen", "+91 98765 43213", "St. Xavier's Academy", "Salt Lake, Sector 2, Kolkata"),
                StudentEntity("S105", "Rohan Das", "Class 10", "B", "Pradip Das", "+91 98765 43214", "St. Xavier's Academy", "Gariahat Main Road, Kolkata")
            )
            students.forEach { dao.insertStudent(it) }

            // 2. Initial Attendance History for last 3 days
            val dates = listOf("2026-06-29", "2026-06-30", "2026-07-01")
            val statuses = listOf("Present", "Present", "Absent", "Present", "Late")
            for (date in dates) {
                students.forEachIndexed { index, student ->
                    val status = if (index == 2 && date == "2026-06-30") "Absent" else if (index == 4 && date == "2026-07-01") "Late" else "Present"
                    dao.insertSingleAttendance(AttendanceEntity(student.rollNumber, date, status))
                }
            }

            // 3. Initial Homework
            val homeworkList = listOf(
                HomeworkEntity(
                    className = "Class 10",
                    subject = "Mathematics",
                    description = "Complete Chapter 4 Quadratic Equations Exercises 4.1 & 4.2. Show all step-by-step solutions.",
                    dueDate = "2026-07-04",
                    assignedDate = "2026-07-01"
                ),
                HomeworkEntity(
                    className = "Class 10",
                    subject = "Physics",
                    description = "Draw ray diagrams for concave and convex mirrors. Submit high-quality PDF scans of drawings.",
                    dueDate = "2026-07-05",
                    assignedDate = "2026-07-01"
                ),
                HomeworkEntity(
                    className = "Class 10",
                    subject = "English Literature",
                    description = "Analyze the theme of courage in 'A Letter to God'. Word limit: 300 words.",
                    dueDate = "2026-07-03",
                    assignedDate = "2026-06-30"
                )
            )
            homeworkList.forEach { dao.insertHomework(it) }

            // Mark some homework as completed
            dao.insertHomeworkCompletion(HomeworkCompletionEntity(1, "S101", true, "2026-07-01"))
            dao.insertHomeworkCompletion(HomeworkCompletionEntity(1, "S102", true, "2026-07-02"))
            dao.insertHomeworkCompletion(HomeworkCompletionEntity(2, "S101", false, null))
            dao.insertHomeworkCompletion(HomeworkCompletionEntity(3, "S101", true, "2026-07-01"))

            // 4. Initial Notices
            val notices = listOf(
                NoticeEntity(
                    title = "Monthly Parents-Teacher Meeting (PTM)",
                    content = "The monthly PTM will be conducted this Saturday from 9:00 AM to 12:30 PM. Attendance is compulsory to discuss the upcoming terminal exam preparation.",
                    type = "General",
                    date = "2026-07-01"
                ),
                NoticeEntity(
                    title = "Mid-Term Examination Schedule",
                    content = "Mid-term examinations will commence from July 15th, 2026. The detailed date sheet and syllabus topics have been uploaded in the Test Management section.",
                    type = "Exam",
                    date = "2026-06-28"
                ),
                NoticeEntity(
                    title = "Monsoon Holiday Announcement",
                    content = "Due to heavy waterlogging, school classes will run online via the digital classrooms on July 3rd, 2026. Stay safe!",
                    type = "Holiday",
                    date = "2026-07-02"
                )
            )
            notices.forEach { dao.insertNotice(it) }

            // 5. Initial Tests with Student Marks
            // Marks maps S101 -> 23, S102 -> 25, S103 -> 18, S104 -> 21, S105 -> 14
            val test = TestEntity(
                subject = "Mathematics",
                chapter = "Quadratic Equations",
                maxMarks = 25,
                testDate = "2026-06-28",
                questionPaperUrl = "paper_math_q4.pdf",
                answerKeyUrl = "key_math_q4.pdf",
                marksJson = "{\"S101\":22,\"S102\":25,\"S103\":15,\"S104\":19,\"S105\":12}"
            )
            dao.insertTest(test)

            val test2 = TestEntity(
                subject = "Physics",
                chapter = "Light Reflection and Refraction",
                maxMarks = 50,
                testDate = "2026-06-25",
                questionPaperUrl = "paper_physics_light.pdf",
                answerKeyUrl = "key_physics_light.pdf",
                marksJson = "{\"S101\":44,\"S102\":48,\"S103\":32,\"S104\":38,\"S105\":25}"
            )
            dao.insertTest(test2)

            // 6. Messages
            val messages = listOf(
                MessageEntity(
                    senderName = "Mr. Verma",
                    senderRole = "Teacher",
                    content = "Good morning class! I have assigned a new Physics homework on concave mirror ray diagrams. Please verify the attachments.",
                    timestamp = "2026-07-01 08:30 AM"
                ),
                MessageEntity(
                    senderName = "EduPilot System",
                    senderRole = "System",
                    content = "Your attendance has been successfully submitted for 2026-07-01. Total Present: 4, Absent: 1.",
                    timestamp = "2026-07-01 10:00 AM"
                )
            )
            messages.forEach { dao.insertMessage(it) }

            // 7. Initial AI Chat message
            dao.insertChat(ChatEntity(role = "model", message = "Hello! I am your **EduPilot AI Study Assistant**. How can I help you learn today? You can ask me to explain chapters, solve tough problems, or design a custom revision plan!"))
        }
    }
}
