package com.sololeveling.systemfit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sololeveling.systemfit.data.local.dao.ExerciseDao
import com.sololeveling.systemfit.data.local.dao.UserDao
import com.sololeveling.systemfit.data.local.dao.WorkoutLogDao
import com.sololeveling.systemfit.data.local.entity.ExerciseEntity
import com.sololeveling.systemfit.data.local.entity.UserEntity
import com.sololeveling.systemfit.data.local.entity.WorkoutLogEntity
import com.sololeveling.systemfit.domain.model.ExerciseType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserEntity::class, ExerciseEntity::class, WorkoutLogEntity::class], version = 4, exportSchema = false)
abstract class SystemDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutLogDao(): WorkoutLogDao

    companion object {
        @Volatile
        private var INSTANCE: SystemDatabase? = null

        fun getDatabase(context: Context): SystemDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SystemDatabase::class.java,
                    "system_fit_database"
                )
                    .addCallback(SystemDatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SystemDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            triggerPopulate()
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            triggerPopulate()
        }

        private fun triggerPopulate() {
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.exerciseDao(), database.userDao())
                }
            }
        }

        suspend fun populateDatabase(exerciseDao: ExerciseDao, userDao: UserDao) {
            val exercises = listOf(
                ExerciseEntity("ex_1", "Wall Sit", ExerciseType.ISOMETRIC, "Hold a seated position against a wall.", "placeholder", true),
                ExerciseEntity("ex_2", "Glute Bridge", ExerciseType.ISOMETRIC, "Lift your hips off the floor and hold.", "placeholder", true),
                ExerciseEntity("ex_3", "Plank", ExerciseType.ISOMETRIC, "Maintain a straight body position on elbows.", "placeholder", true),
                ExerciseEntity("ex_4", "Side Plank", ExerciseType.ISOMETRIC, "Support your body on one side.", "placeholder", true),
                ExerciseEntity("ex_5", "Walking", ExerciseType.CARDIO, "Brisk walking in place.", "placeholder", true),
                ExerciseEntity("ex_6", "Light Jogging", ExerciseType.CARDIO, "Gentle jogging in place.", "placeholder", true),
                ExerciseEntity("ex_7", "Cycling", ExerciseType.CARDIO, "Air cycling movements on back.", "placeholder", true),
                ExerciseEntity("ex_8", "Arm Circles", ExerciseType.FLEXIBILITY, "Controlled circular arm motions.", "placeholder", true),
                ExerciseEntity("ex_9", "Neck Stretches", ExerciseType.FLEXIBILITY, "Gentle stretches for the neck.", "placeholder", true),
                ExerciseEntity("ex_10", "Leg Swings", ExerciseType.FLEXIBILITY, "Controlled leg swings.", "placeholder", true),
                ExerciseEntity("ex_11", "Bodyweight Squats", ExerciseType.STRENGTH, "Basic unweighted squats.", "placeholder", true),
                ExerciseEntity("ex_12", "Modified Pushups", ExerciseType.STRENGTH, "Pushups from the knees.", "placeholder", true),
                ExerciseEntity("ex_13", "Lunges", ExerciseType.STRENGTH, "Controlled forward lunges.", "placeholder", true),
                ExerciseEntity("ex_14", "Calf Raises", ExerciseType.STRENGTH, "Lift onto the toes.", "placeholder", true)
            )
            exerciseDao.insertExercises(exercises)

            // Seed initial user as well
            userDao.insertUser(UserEntity(
                id = "player_1",
                name = "Sung Jin-Woo",
                level = 1,
                currentXp = 0,
                str = 10,
                vit = 10,
                agi = 10,
                availableStatPoints = 0,
                currentStreak = 0,
                bestStreak = 0,
                theme = "SOLO_BLUE",
                targetWorkoutDaysPerWeek = 5,
                customActiveDurationSeconds = 0,
                customRestDurationSeconds = 0,
                lastWorkoutTimestamp = 0L,
                penaltyActive = false,
                bpModeActive = true,
                isDarkMode = true,
                skipIntro = false
            ))
        }
    }
}
