package com.sololeveling.systemfit.presentation.workout

/**
 * Configuration object to manage the external dataset source for exercise GIFs.
 * This abstracts away the media provider so it can be swapped out easily in the future
 * if a better open-source dataset is found.
 */
object WorkoutMediaConfig {
    
    // Using a reliable raw GitHub repository as our primary open-source asset provider.
    // Example providers:
    // 1. ExerciseGymGifsDB (https://raw.githubusercontent.com/JahelCuadrado/ExerciseGymGifsDB/main/exercises/)
    // 2. free-exercise-db (https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/)
    
    private const val DATASET_BASE_URL = "https://raw.githubusercontent.com/JahelCuadrado/ExerciseGymGifsDB/main/exercises/"
    
    // Fallback GIF if an exercise is not explicitly mapped.
    // Using a generic 0001 (usually pushups or barbells in most DBs) as a safety net.
    private const val FALLBACK_GIF = "${DATASET_BASE_URL}0001.gif"

    /**
     * Maps our internal exercise IDs (e.g. "ex_push_normal_t2") to the exact 
     * filename or path required by the external dataset provider.
     */
    private val exerciseToDatasetMap = mapOf(
        "ex_push_normal_t1" to "0260.gif", // Knee Push-ups 
        "ex_push_normal_t2" to "0255.gif", // Standard Push-ups
        "ex_squat_bp_t2" to "0043.gif",    // Bodyweight Squats
        "ex_cardio_t1_1" to "3220.gif",    // Jumping Jacks
        // Add more mappings here as you explore the open-source dataset!
    )

    /**
     * Resolves the final GIF URL to display.
     * 
     * @param exerciseId The internal ID of the current exercise.
     * @param defaultDbUrl The URL stored in the local SQLite DB (usually "placeholder").
     */
    fun getGifUrl(exerciseId: String, defaultDbUrl: String): String {
        // If the database has a specific custom URL (not placeholder), respect it.
        if (defaultDbUrl != "placeholder" && defaultDbUrl.isNotBlank() && !defaultDbUrl.startsWith("file:///android_asset")) {
            return defaultDbUrl
        }
        
        // Lookup the external asset filename from our map
        val remoteAssetFileName = exerciseToDatasetMap[exerciseId]
        
        return if (remoteAssetFileName != null) {
            DATASET_BASE_URL + remoteAssetFileName
        } else {
            // Return a safe fallback if we haven't mapped this exercise yet.
            FALLBACK_GIF
        }
    }
}
