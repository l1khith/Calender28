# 3. Database Design (Room Schema)

## Overview
Calender28 uses **Room Database** over SQLite as its core persistence mechanism.

The schema is optimized for fast query execution on date-indexed tasks and 28-day habit tracking cycles.

---

## Entity Relationship Diagram (ERD)

```
┌─────────────────┐         ┌─────────────────────┐
│   TaskEntity    │         │  RecurringTaskEntity│
├─────────────────┤         ├─────────────────────┤
│ PK id: String   │         │ PK id: String       │
│ title: String   │         │ title: String       │
│ description: Str│         │ description: String │
│ category: String│         │ category: String    │
│ dateStr: String │         │ frequency: String   │
│ timeStr: String?│         │ intervalDays: Int   │
│ reminder: Bool  │         │ startDate: String   │
│ isCompleted:Bool│         │ endDate: String?    │
│ utcTimestamp:Long│        │ reminderTime: String│
│ createdAtMs:Long│         │ isActive: Boolean   │
│                 │         │ createdAtMs: Long   │
└─────────────────┘         └─────────────────────┘

┌─────────────────┐         ┌─────────────────────┐
│   HabitEntity   │◄────────┤  HabitEntryEntity   │
├─────────────────┤    1:N  ├─────────────────────┤
│ PK id: String   │         │ PK id: String       │
│ name: String    │         │ FK habitId: String  │
│ category: String│         │ cycleIndex: Long    │
│ reminderTime:Str│         │ dayInCycle: Int     │
│ isPaused: Bool  │         │ epochDay: Long      │
│ colorHex: Long  │         │ isCompleted: Bool   │
│ createdAtMs:Long│         │ completedAtMs: Long?│
└─────────────────┘         └─────────────────────┘
```

---

## Room Entities Implementation

```kotlin
package com.l1khith.calender28.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// TaskEntity.kt
@Entity(
    tableName = "tasks",
    indices = [
        Index("dateStr"),
        Index("isCompleted")
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val category: String,
    val dateStr: String,        // Format: "YYYY-MM-DD"
    val timeStr: String?,       // Format: "hh:mm AM/PM"
    val reminder: Boolean,
    val isCompleted: Boolean,
    val utcTimestamp: Long?,     // Trigger timestamp for reminders
    val createdAtMs: Long = System.currentTimeMillis()
)

// RecurringTaskEntity.kt
@Entity(tableName = "recurring_tasks")
data class RecurringTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val category: String,
    val frequency: String,       // "daily", "weekly", "monthly"
    val intervalDays: Int,
    val startDate: String,
    val endDate: String?,
    val reminderTime: String?,
    val isActive: Boolean,
    val createdAtMs: Long = System.currentTimeMillis()
)

// HabitEntity.kt
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String = "Health",
    val reminderTime: String?,
    val isPaused: Boolean = false,
    val colorHex: Long = 0xFF6B7280,
    val createdAtMs: Long = System.currentTimeMillis()
)

// HabitEntryEntity.kt
@Entity(
    tableName = "habit_entries",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId", "cycleIndex", "dayInCycle", unique = true)]
)
data class HabitEntryEntity(
    @PrimaryKey val id: String,  // Composite string ID: "${habitId}_${cycleIndex}_${dayInCycle}"
    val habitId: String,
    val cycleIndex: Long,         // Total 28-day cycles elapsed
    val dayInCycle: Int,          // 1-28 days inside cycle
    val epochDay: Long,           // Days since epoch for this entry
    val isCompleted: Boolean = false,
    val completedAtMs: Long? = null
)
```

---

## Data Access Objects (DAOs)

```kotlin
package com.l1khith.calender28.data.local.dao

import androidx.room.*
import com.l1khith.calender28.data.local.entity.HabitEntity
import com.l1khith.calender28.data.local.entity.HabitEntryEntity
import com.l1khith.calender28.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE dateStr = :date ORDER BY utcTimestamp ASC")
    suspend fun getTasksForDate(date: String): List<TaskEntity>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY utcTimestamp ASC")
    fun getActiveTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT dateStr, COUNT(*) as count FROM tasks WHERE isCompleted = 0 GROUP BY dateStr")
    fun getTaskCountsPerDate(): Flow<Map<String, Int>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)
    
    @Update
    suspend fun update(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun delete(taskId: String)
    
    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun toggleCompletion(taskId: String, completed: Boolean)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAtMs DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: String): HabitEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)
    
    @Update
    suspend fun update(habit: HabitEntity)
    
    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface HabitEntryDao {
    @Query("""
        SELECT * FROM habit_entries 
        WHERE habitId = :habitId AND cycleIndex = :cycleIndex 
        ORDER BY dayInCycle ASC
    """)
    suspend fun getEntriesForCycle(habitId: String, cycleIndex: Long): List<HabitEntryEntity>
    
    @Query("""
        SELECT COUNT(*) FROM habit_entries 
        WHERE habitId = :habitId AND cycleIndex = :cycleIndex AND isCompleted = 1
    """)
    suspend fun getCompletedCountInCycle(habitId: String, cycleIndex: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HabitEntryEntity)
    
    @Query("DELETE FROM habit_entries WHERE habitId = :habitId")
    suspend fun deleteAllForHabit(habitId: String)
}
```

---

## Room Database Setup

```kotlin
package com.l1khith.calender28.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.l1khith.calender28.data.local.dao.*
import com.l1khith.calender28.data.local.entity.*

@Database(
    entities = [
        TaskEntity::class, 
        RecurringTaskEntity::class, 
        HabitEntity::class, 
        HabitEntryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CalenderDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun recurringTaskDao(): RecurringTaskDao
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao

    companion object {
        @Volatile private var INSTANCE: CalenderDatabase? = null
        
        fun getInstance(context: Context): CalenderDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CalenderDatabase::class.java,
                    "calender28.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
    }
}
```

---

## Required Indexes

The following indexes MUST exist to ensure query performance:

| Table | Column(s) | Index Name | WHY |
|-------|-----------|------------|-----|
| `tasks` | `associated_date` | `index_tasks_associated_date` | all task lookups are by date |
| `tasks` | `recurring_parent_id` | `index_tasks_recurring_parent_id` | child task lookups |
| `tasks` | `is_completed` | `index_tasks_is_completed` | filtering active/completed |
| `coin_transactions` | `timestamp` | `index_coin_transactions_timestamp` | time-range queries |
| `coin_transactions` | `reason, note` | `index_coin_transactions_reason_note` | idempotency checks |
| `scheduled_alarms` | `item_id` | `index_scheduled_alarms_item_id` | alarm lookups by task/habit |
| `scheduled_alarms` | `scheduled_time_utc` | `index_scheduled_alarms_scheduled_time_utc` | chronological alarm queries |
| `notes` | `updatedAt` | `index_notes_updatedAt` | sort by recent |
| `notes` | `isPinned` | `index_notes_isPinned` | pinned-first sorting |
| `notes` | `linkedType, linkedId` | `index_notes_linkedType_linkedId` | entity-linked note lookups |

---

## Migration Safety Rules
- Always use `exportSchema = true`
- Commit schema JSON to version control
- Write explicit `Migration(from, to)` — NEVER destructive (`fallbackToDestructiveMigration()`) in production
- Test migrations with Room's `MigrationTestHelper`

---

## Query Performance Rules
- NEVER use `getAllItems().find { }` for single lookups — use `@Query WHERE id = :id`
- NEVER use `LEFT JOIN DISTINCT` when a simple `SELECT` suffices
- Use `@Transaction` for related multi-table reads
- Use `GROUP BY` aggregates instead of loading all rows and counting in Kotlin
