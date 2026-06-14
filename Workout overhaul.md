Here is the complete, formatted specification document for the workout engine. This document is written in a strict, data-driven format optimized for ingestion by an AI coding agent to implement the required use cases and data entities.

# **Solo Leveling SystemFit: Workout Engine Architecture Specification v2.0**

## **1\. Engine Scope and Constraints**

This engine strictly generates non-equipment, bodyweight-only workouts. The primary physiological targets are **weight loss** and **cardiovascular stamina**, supported by secondary strength maintenance and mobility protocols.

### **System Limitations**

* **Absolute Minimum Rest:** 30 seconds (physiological floor for anaerobic recovery).  
* **Absolute Maximum Active Interval:** 60 seconds (prevents shifting entirely from anaerobic to aerobic pacing in a single set).  
* **Maximum Daily Volume:** 5 total rounds (prevents overtraining and burnout).  
* **Progression Cap:** Diminishing returns are enforced mathematically; linear progression is prohibited to prevent stat redundancy.

## **2\. The Stat-to-Physiology Mapping Matrix**

Player attributes directly control specific variables within the workout generation and execution states.

| Attribute | Physiological Target | System Impact |
| :---- | :---- | :---- |
| **Vitality (VIT)** | Density & Recovery | Decreases the required rest duration between sets using an exponential decay model. |
| **Agility (AGI)** | Volume & Caloric Burn | Increases the active duration of Cardio and HIIT intervals using a logarithmic growth model. |
| **Strength (STR)** | Biomechanical Intensity | Unlocks higher-tier, higher-leverage variations of exercises (Exercise Evolution). |
| **Level (LVL)** | Total Capacity | Increases the total number of sets/rounds per daily quest and multiplies base XP payouts. |

## **3\. Algorithmic Scaling Formulas**

To calculate timers and thresholds, the engine utilizes the following mathematical formulas to enforce diminishing returns.

### **A. Rest Interval Calculation (VIT)**

Rest periods decay exponentially toward a 30-second floor.  
T\_{\\text{rest}} \= 30 \+ \\lfloor 60 \\times e^{-0.04 \\times \\text{VIT}} \\rfloor

* *Implementation Note:* Evaluate this formula in WorkoutViewModel.kt upon session initialization.

### **B. Active Cardio/HIIT Interval Calculation (AGI)**

Active durations scale logarithmically toward a 60-second ceiling.  
T\_{\\text{active}} \= 20 \+ \\lfloor 40 \\times (1 \- e^{-0.04 \\times \\text{AGI}}) \\rfloor

* *Implementation Note:* For Strength and Flexibility exercises, T\_{\\text{active}} may be overridden by rep counts or static hold ceilings depending on the specific exercise entity configuration.

### **C. Total Volume (Rounds) Calculation (LVL)**

The number of rounds scales linearly but is hard-capped at 5 to ensure workouts remain under 45 minutes.

\\text{Rounds} \= \\min(2 \+ \\lfloor \\text{LVL} / 5 \\rfloor, 5\)

## **4\. Exercise Tier System (Strength Progression)**

To apply progressive overload without equipment, the exercises database table requires two new columns: tier (INTEGER) and required\_str (INTEGER).  
The daily quest generator will filter available exercises based on the player's current STR stat.

| Movement Category | Tier 1 (Base, STR 1-20) | Tier 2 (Intermediate, STR 21-40) | Tier 3 (Advanced, STR 41+) |
| :---- | :---- | :---- | :---- |
| **Upper Body Push** | Wall / Knee Push-ups | Standard Push-ups | Decline / Diamond Push-ups |
| **Lower Body Push** | Assisted Squats | Bodyweight Squats | Jump Squats / Pistol Squats |
| **Core / Flex** | Standard Crunches | Bicycle Crunches | V-Ups / Hollow Body Holds |
| **Cardio / HIIT** | Jumping Jacks | High Knees / Skaters | Burpees / Mountain Climbers |

* *Implementation Note:* The AI agent must update ExerciseEntity.kt and ExerciseDao.kt to support querying by required\_str \<= currentPlayer.str.

## **5\. Daily Quest Generation Logic**

To strictly enforce the weight loss and stamina objective, GenerateDailyQuestUseCase.kt must adhere to a deterministic categorical ratio. It cannot select exercises at random.  
For a standard 4-exercise block, the engine must construct the array as follows:

1. **Slot 1 (Cardio \- 25%):** High heart-rate initiator.  
2. **Slot 2 (Strength \- 25%):** Muscular endurance/hypertrophy focus.  
3. **Slot 3 (Cardio \- 25%):** Secondary heart-rate spike.  
4. **Slot 4 (Flexibility/Isometric \- 25%):** Core stabilization and active cooling.

## **6\. XP and Level Progression System**

The economy of the application relies on an exponential threshold combined with a scaling payout.

### **A. Level-Up Threshold**

The total accumulated XP required to reach the next level (L+1):

\\text{Threshold} \= \\lfloor 100 \\times \\text{LVL}^{1.5} \\rfloor

### **B. Dynamic XP Payout**

Calculated within ProcessWorkoutResultUseCase.kt. Payouts scale with the player's level to prevent mathematical stagnation.  
\\text{XP}\_{\\text{Earned}} \= 50 \\times (1 \+ (\\text{LVL} \\times 0.15)) \\times M\_{\\text{completion}}  
**Completion Multipliers (M\_{\\text{completion}}):**

* Full Completion: 1.0  
* Emergency Halt (Partial clear): 0.5  
* Penalty Zone Survival: 0.2 (A punitive modifier to reflect the penalty status).

## **7\. Data Synchronization & Timezone Resilience**

To resolve the previously identified vulnerability in streak tracking:

1. lastWorkoutTimestamp must strictly store standard **Epoch UTC timestamps**.  
2. The currentStreak calculation must evaluate the difference in UTC days to prevent local timezone shifts from triggering a false penaltyActive state.  
3. The default "player\_1" ID in the user creation routine must be replaced with a randomly generated UUID.randomUUID().toString() to prevent remote sync collisions.

Do you want to formulate the exact JSON or SQL seed schema for the exercises table next, so the AI agent has the complete tiered matrix ready for database ingestion?