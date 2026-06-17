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

@Database(entities = [UserEntity::class, ExerciseEntity::class, WorkoutLogEntity::class], version = 6, exportSchema = false)
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
                    .addCallback(SystemDatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SystemDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
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
                    populateDatabase(database.exerciseDao(), database.userDao(), context)
                }
            }
        }

        suspend fun populateDatabase(exerciseDao: ExerciseDao, userDao: UserDao, context: Context) {
            val exercises = listOf(
                // Cardio / HIIT (Normal)
                ExerciseEntity("ex_cardio_t1_1", "Jumping Jacks", ExerciseType.CARDIO, "Standard jumping jacks.", "placeholder", false, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_cardio_t2_1", "High Knees", ExerciseType.CARDIO, "Bring knees up to chest height rapidly.", "placeholder", false, tier = 2, requiredStr = 21),
                ExerciseEntity("ex_cardio_t2_2", "Skaters", ExerciseType.CARDIO, "Lateral leaps simulating skater motion.", "placeholder", false, tier = 2, requiredStr = 21),
                ExerciseEntity("ex_cardio_t3_1", "Burpees", ExerciseType.CARDIO, "Full body squat thrust with jump.", "placeholder", false, tier = 3, requiredStr = 41),
                ExerciseEntity("ex_cardio_t3_2", "Mountain Climbers", ExerciseType.CARDIO, "Rapid alternating knee drives in plank.", "placeholder", false, tier = 3, requiredStr = 41),

                // Cardio Steady (BP-safe)
                ExerciseEntity("ex_cardio_bp_t1", "Marching in Place", ExerciseType.CARDIO, "Low-impact marching in place.", "placeholder", true, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_cardio_bp_t2", "Step Jacks (No jumping)", ExerciseType.CARDIO, "Low-impact side step jacks.", "placeholder", true, tier = 2, requiredStr = 21),
                ExerciseEntity("ex_cardio_bp_t3", "Shadow Boxing (Light pace)", ExerciseType.CARDIO, "Paced punching in athletic stance.", "placeholder", true, tier = 3, requiredStr = 41),

                // Strength Upper Push (Normal)
                ExerciseEntity("ex_push_normal_t1", "Knee Push-ups", ExerciseType.STRENGTH, "Push-ups from the knees.", "placeholder", false, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_push_normal_t2", "Standard Push-ups", ExerciseType.STRENGTH, "Standard bodyweight push-ups.", "placeholder", false, tier = 2, requiredStr = 21),
                ExerciseEntity("ex_push_normal_t3_1", "Decline Push-ups", ExerciseType.STRENGTH, "Push-ups with feet elevated.", "placeholder", false, tier = 3, requiredStr = 41),
                ExerciseEntity("ex_push_normal_t3_2", "Diamond Push-ups", ExerciseType.STRENGTH, "Push-ups with hands close in diamond shape.", "placeholder", false, tier = 3, requiredStr = 41),

                // Strength Upper Push (BP-safe)
                ExerciseEntity("ex_push_bp_t1", "Wall Push-ups", ExerciseType.STRENGTH, "Controlled push-ups against a wall.", "placeholder", true, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_push_bp_t2", "Incline Push-ups (Hands elevated)", ExerciseType.STRENGTH, "Push-ups with hands on elevated surface.", "placeholder", true, tier = 2, requiredStr = 21),
                ExerciseEntity("ex_push_bp_t3", "Standard Push-ups (Paced)", ExerciseType.STRENGTH, "Paced, slow standard push-ups.", "placeholder", true, tier = 3, requiredStr = 41),

                // Strength Lower Push (Normal)
                ExerciseEntity("ex_squat_normal_t1", "Assisted Squats", ExerciseType.STRENGTH, "Squats holding a stable support.", "placeholder", false, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_squat_normal_t3", "Jump Squats", ExerciseType.STRENGTH, "Explosive squats with jump.", "placeholder", false, tier = 3, requiredStr = 41),
                ExerciseEntity("ex_squat_normal_t3_2", "Pistol Squats", ExerciseType.STRENGTH, "Single leg bodyweight squat.", "placeholder", false, tier = 3, requiredStr = 41),

                // Strength Lower Push (BP-safe / Standard)
                ExerciseEntity("ex_squat_bp_t1", "Assisted Box Squats", ExerciseType.STRENGTH, "Controlled squats to a chair or box.", "placeholder", true, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_squat_bp_t2", "Bodyweight Squats", ExerciseType.STRENGTH, "Basic bodyweight squats.", "placeholder", true, tier = 2, requiredStr = 21),
                ExerciseEntity("ex_squat_bp_t3", "Reverse Lunges", ExerciseType.STRENGTH, "Step backward into controlled lunge.", "placeholder", true, tier = 3, requiredStr = 41),

                // Core / Flex (Normal)
                ExerciseEntity("ex_core_normal_t1", "Standard Crunches", ExerciseType.FLEXIBILITY, "Basic abdominal crunches.", "placeholder", false, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_core_normal_t2", "Bicycle Crunches", ExerciseType.FLEXIBILITY, "Alternating elbow to opposite knee crunches.", "placeholder", false, tier = 2, requiredStr = 21),
                ExerciseEntity("ex_core_normal_t3_1", "V-Ups", ExerciseType.FLEXIBILITY, "Fold body into a V shape.", "placeholder", false, tier = 3, requiredStr = 41),
                ExerciseEntity("ex_core_normal_t3_2", "Hollow Body Holds", ExerciseType.FLEXIBILITY, "Static core hollow body position.", "placeholder", false, tier = 3, requiredStr = 41),

                // Core / Flex (BP-safe)
                ExerciseEntity("ex_core_bp_t1", "Bird-Dog", ExerciseType.FLEXIBILITY, "Extend opposite arm and leg on all fours.", "placeholder", true, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_core_bp_t2", "Dead Bugs", ExerciseType.FLEXIBILITY, "Lie on back and extend opposite arm/leg.", "placeholder", true, tier = 2, requiredStr = 21),
                ExerciseEntity("ex_core_bp_t3", "Lying Leg Raises (Single leg)", ExerciseType.FLEXIBILITY, "Alternating single leg raises lying on back.", "placeholder", true, tier = 3, requiredStr = 41),

                // Active Recovery & Stretching (BP-safe / General)
                ExerciseEntity("ex_stretch_1", "Arm Circles", ExerciseType.FLEXIBILITY, "Circular arm motions.", "placeholder", true, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_stretch_2", "Neck Stretches", ExerciseType.FLEXIBILITY, "Gentle stretches for the neck.", "placeholder", true, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_stretch_3", "Leg Swings", ExerciseType.FLEXIBILITY, "Controlled swings for leg mobility.", "placeholder", true, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_cardio_walk", "Walking", ExerciseType.CARDIO, "Brisk walking in place.", "placeholder", true, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_cardio_jog", "Light Jogging", ExerciseType.CARDIO, "Gentle jogging in place.", "placeholder", true, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_cardio_cycle", "Air Cycling", ExerciseType.CARDIO, "Air cycling movements on back.", "placeholder", true, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_strength_calf", "Calf Raises", ExerciseType.STRENGTH, "Lift up onto toes.", "placeholder", true, tier = 1, requiredStr = 1),

                // Isometric / Banned in BP
                ExerciseEntity("ex_1", "Wall Sit", ExerciseType.ISOMETRIC, "Hold a seated position against a wall.", "placeholder", false, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_2", "Glute Bridge", ExerciseType.ISOMETRIC, "Lift your hips off the floor and hold.", "placeholder", false, tier = 1, requiredStr = 1),
                ExerciseEntity("ex_3", "Plank", ExerciseType.ISOMETRIC, "Maintain a straight body position on elbows.", "placeholder", false, tier = 2, requiredStr = 21),
                ExerciseEntity("ex_4", "Side Plank", ExerciseType.ISOMETRIC, "Support your body on one side.", "placeholder", false, tier = 2, requiredStr = 21)
            )
            exerciseDao.insertExercises(exercises)

            // Resolve dynamic UUID to seed initial user
            val sharedPrefs = context.getSharedPreferences("system_fit_user_prefs", Context.MODE_PRIVATE)
            var userId = sharedPrefs.getString("active_user_id", null)
            if (userId == null) {
                userId = java.util.UUID.randomUUID().toString()
                sharedPrefs.edit().putString("active_user_id", userId).apply()
            }

            userDao.insertUser(UserEntity(
                id = userId,
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
                workoutDaysOfWeek = "2,3,4,5,6",
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
