package com.example.elearning.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.elearning.MainActivity
import com.example.elearning.R
import com.example.elearning.model.Course
import com.example.elearning.model.Lesson
import com.example.elearning.navigation.Screen
import java.util.concurrent.TimeUnit

class NotificationService(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "eLearning_Channel"
        private const val CHANNEL_NAME = "eLearning Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for course updates and reminders"
        
        // Notification IDs
        private const val RESUME_COURSE_ID = 1
        private const val COMPLETE_MODULE_ID = 2
        private const val QUIZ_REMINDER_ID = 3
        private const val REENGAGEMENT_ID = 4
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun showNotification(notificationId: Int, notification: NotificationCompat.Builder) {
        if (hasNotificationPermission()) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                NotificationManagerCompat.from(context).notify(notificationId, notification.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendResumeCourseNotification(course: Course, lastLesson: Lesson) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("courseId", course.id)
            putExtra("lessonId", lastLesson.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            RESUME_COURSE_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Continue Learning")
            .setContentText("Resume your progress in ${course.title}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        showNotification(RESUME_COURSE_ID, notification)
    }

    fun sendCompleteModuleNotification(course: Course, moduleName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("courseId", course.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            COMPLETE_MODULE_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Complete Your Module")
            .setContentText("Finish $moduleName in ${course.title} to earn your certificate!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        showNotification(COMPLETE_MODULE_ID, notification)
    }

    fun sendQuizReminderNotification(course: Course, quizName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("courseId", course.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            QUIZ_REMINDER_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Quiz Reminder")
            .setContentText("Take the $quizName quiz in ${course.title} to test your knowledge!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        showNotification(QUIZ_REMINDER_ID, notification)
    }

    fun sendReengagementNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REENGAGEMENT_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("We Miss You!")
            .setContentText("Continue your learning journey with new courses and updates!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        showNotification(REENGAGEMENT_ID, notification)
    }
} 