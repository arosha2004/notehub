package com.example.notehub.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.example.notehub.ui.theme.*

/**
 * Note — Data model representing a single note in the application.
 */
data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val date: String,
    val category: String,
    val color: Color
)

/**
 * SampleData — Static data for demonstration purposes.
 * Using mutableStateListOf so that changes (adding/deleting) are reflected across all screens.
 */
object SampleData {
    val notes = mutableStateListOf(
        Note(
            1, 
            "Semester 1 Project Ideas", 
            "Focus on clean UI and Material 3 components. Use Jetpack Compose for modern development. Implement Dark Mode and Responsive layouts.", 
            "Oct 24, 2023", 
            "Education", 
            PrimaryBlue
        ),
        Note(
            2, 
            "Grocery List", 
            "Apples, Milk, Bread, Coffee, Eggs, Spinach, Chicken Breast, Pasta Sauce.", 
            "Oct 22, 2023", 
            "Personal", 
            SuccessGreen
        ),
        Note(
            3, 
            "Meeting Notes: UI/UX Design", 
            "Discussion about the new design system. Prioritize accessibility and smooth transitions. Use rounded corners (20dp) for cards.", 
            "Oct 20, 2023", 
            "Work", 
            WarningYellow
        ),
        Note(
            4, 
            "Travel Plans - Tokyo", 
            "Visit Shibuya Crossing, Meiji Shrine, and Akihabara. Book the JR Pass before departure.", 
            "Oct 15, 2023", 
            "Personal", 
            InfoBlue
        ),
        Note(
            5, 
            "Kotlin Best Practices", 
            "Use extension functions, sealed classes for navigation, and avoid nullable types where possible. Keep functions small.", 
            "Oct 12, 2023", 
            "Work", 
            AccentPurple
        )
    )

    val stats = mapOf(
        "Total Notes" to "12",
        "This Month" to "5",
        "Categories" to "8",
        "Uploads" to "3"
    )
}
