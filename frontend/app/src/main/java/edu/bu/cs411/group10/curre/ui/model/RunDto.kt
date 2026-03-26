package edu.bu.cs411.group10.curre.ui.model

data class RunDto(
    val id: Long? = null,
    val startedAt: Long,
    val endedAt: Long,
    val distanceMiles: Double,
    val durationSeconds: Int,
    val avgPaceSecsPerMile: Double,
    val calories: Int
)