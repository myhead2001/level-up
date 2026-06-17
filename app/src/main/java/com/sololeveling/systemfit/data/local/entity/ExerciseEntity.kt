package com.sololeveling.systemfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sololeveling.systemfit.domain.model.ExerciseType

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: ExerciseType,
    val description: String,
    val gifUrl: String,
    val isHtnSafe: Boolean,
    val tier: Int = 1,
    val requiredStr: Int = 1
)

