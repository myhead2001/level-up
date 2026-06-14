# **Solo Leveling SystemFit: Hypertension Protocol (BP-Safe) Specification v1.0**

## **1\. Engine Scope and Medical Constraints**

When the BP/Hypertension toggle (bpModeActive \= true) is engaged, the workout engine bypasses the standard generation logic. The primary objective shifts from aggressive anaerobic fatigue to **steady-state cardiovascular conditioning and moderate-intensity strength maintenance**.

### **System Limitations & Contraindications**

* **Zero Isometrics:** Isometric holds (e.g., Planks, Wall Sits) are strictly prohibited. They induce the Valsalva maneuver, causing acute and dangerous spikes in blood pressure.  
* **Zero Inversions:** Exercises where the head drops below the heart (e.g., Decline Push-ups, Pike Push-ups) are removed.  
* **No Rapid Postural Transitions:** Movements requiring rapid shifts from the floor to a standing position (e.g., Burpees) are excluded to prevent orthostatic hypotension/hypertension (dizziness or fainting).  
* **Absolute Minimum Rest:** Increased to 45 seconds (ensures heart rate returns to a safe baseline between sets).  
* **Absolute Maximum Active Interval:** Capped at 45 seconds (prevents shifting into extreme anaerobic zones).  
* **Maximum Daily Volume:** Capped at 4 total rounds.

## **2\. The Stat-to-Physiology Mapping Matrix**

The player attributes still control the workout parameters, but the mathematical boundaries are compressed to prioritize cardiovascular safety.

| Attribute | Physiological Target | System Impact (BP Protocol) |
| :---- | :---- | :---- |
| **Vitality (VIT)** | Recovery Rate | Decreases rest duration, but halts at a higher, medically safe floor (45s). |
| **Agility (AGI)** | Sustained Output | Increases active cardio intervals, capped at a moderate ceiling (45s). |
| **Strength (STR)** | Load Management | Unlocks higher-tier exercises that rely on volume or stability rather than high leverage or inversions. |
| **Level (LVL)** | Total Capacity | Increases sets/rounds, hard-capped earlier than the standard protocol. |

## **3\. Algorithmic Scaling Formulas (BP-Safe)**

The exponential decay models are adjusted to prevent the engine from mathematically forcing a hypertensive user into dangerous intensity zones.

### **A. Rest Interval Calculation (VIT)**

Rest periods decay exponentially toward a strict 45-second floor.  
T\_{\\text{rest}} \= 45 \+ \\lfloor 45 \\times e^{-0.04 \\times \\text{VIT}} \\rfloor  
*At VIT 10, rest is \~75 seconds. At VIT 50, rest approaches 50 seconds.*

### **B. Active Cardio/HIIT Interval Calculation (AGI)**

Active durations scale logarithmically toward a safe 45-second ceiling.  
T\_{\\text{active}} \= 20 \+ \\lfloor 25 \\times (1 \- e^{-0.04 \\times \\text{AGI}}) \\rfloor  
*At AGI 10, active time is \~28 seconds. At AGI 50, active time approaches 41 seconds.*

### **C. Total Volume (Rounds) Calculation (LVL)**

The number of rounds scales linearly but is hard-capped at 4 to prevent sustained cardiovascular fatigue.

\\text{Rounds} \= \\min(2 \+ \\lfloor \\text{LVL} / 8 \\rfloor, 4\)

## **4\. Exercise Tier System (Hypertension Safe Matrix)**

The ExerciseDao.kt must filter the database using both required\_str \<= currentPlayer.str AND isHtnSafe \== true.

| Movement Category | Tier 1 (Base, STR 1-20) | Tier 2 (Intermediate, STR 21-40) | Tier 3 (Advanced, STR 41+) |
| :---- | :---- | :---- | :---- |
| **Upper Body Push** | Wall Push-ups | Incline Push-ups (Hands elevated) | Standard Push-ups (Paced) |
| **Lower Body Push** | Assisted Box Squats | Standard Bodyweight Squats | Reverse Lunges |
| **Core / Flex** | Bird-Dog | Dead Bugs | Lying Leg Raises (Single leg) |
| **Cardio (Steady)** | Marching in Place | Step Jacks (No jumping) | Shadow Boxing (Light pace) |

*Implementation Note:* All exercises in this matrix must be accompanied by a UI prompt emphasizing "Continuous Breathing \- Do Not Hold Breath."

## **5\. Daily Quest Generation Logic**

The GenerateDailyQuestUseCase.kt must restructure the routine to ensure heart rate elevation is gradual and sustained, rather than spiked.  
For a standard 4-exercise block, the engine must construct the array as follows:

1. **Slot 1 (Warm-up Cardio \- 25%):** Low-impact movement (e.g., Marching in Place).  
2. **Slot 2 (Strength Push \- 25%):** Controlled upper or lower body movement.  
3. **Slot 3 (Core \- 25%):** Floor-based, continuous breathing core exercise (e.g., Dead Bugs).  
4. **Slot 4 (Active Recovery \- 25%):** Dynamic stretching or very light cardio.

## **6\. The Emergency Recovery Zone Protocol**

The existing BP/Hypertension toggle's "panic button" must be integrated into this new logic flow.  
When the user presses the **Emergency Halt** button during a session:

1. The active workout immediately terminates.  
2. The UI transitions entirely to the **Controlled Recovery Zone** (3-minute guided nasal breathing).  
3. **Crucial Rule:** The workout is marked as isCompleted \= false in workout\_logs, but the user is **not** penalized.  
4. The ProcessWorkoutResultUseCase.kt grants the 0.5x Emergency Halt XP multiplier, maintaining the psychological reward loop without encouraging users to push through dangerous physical symptoms.

## **7\. XP and Level Progression System**

To ensure users are not penalized for utilizing the safety protocols, the XP algorithms remain mathematically identical to the standard engine.  
\\text{Threshold} \= \\lfloor 100 \\times \\text{LVL}^{1.5} \\rfloor \\text{XP}\_{\\text{Earned}} \= 50 \\times (1 \+ (\\text{LVL} \\times 0.15)) \\times M\_{\\text{completion}}  
The only progression difference is that total XP accumulation per session may be slightly lower due to the volume cap (4 rounds vs 5 rounds), which organically represents the slower, safer pace required for physiological adaptation in hypertensive individuals.