package com.sololeveling.systemfit.domain.model

data class Exercise(
    val id: String,
    val name: String,
    val type: ExerciseType,
    val description: String,
    val gifUrl: String,
    val isHtnSafe: Boolean,
    val tier: Int = 1,
    val requiredStr: Int = 1
)

