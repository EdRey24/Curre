package edu.bu.cs411.group10.curre.ui.model

data class PastRun(
    val id: Int,
    val pace: Double,
    val durationMinutes: Int,
    val date: String
)

// Simple data model used by the end run screen.
data class RunSummary(
    val miles: Double,
    val durationText: String,
    val avgPaceText: String,
    val calories: Int
)