package vn.hcmute.edu.notificationservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENotificationType {
    // Delivery methods
    EMAIL("Email"),
    WEBSITE("Website"),

    // Content types
    EMAIL_VERIFICATION("Email Verification"),
    WELCOME_EMAIL("Welcome Email"),
    PASSWORD_RESET("Password Reset"),
    COURSE_ENROLLMENT("Course Enrollment"),
    QUIZ_REMINDER("Quiz Reminder"),
    STUDY_REMINDER("Study Reminder"),
    FLASHCARD_SHARED("Flashcard Shared"),
    SYSTEM_NOTIFICATION("System Notification");

    private final String displayName;
}