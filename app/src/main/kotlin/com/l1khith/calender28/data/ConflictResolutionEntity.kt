package com.l1khith.calender28.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity logging conflict resolution events and decisions for undo and auditability.
 */
@Entity(tableName = "conflict_resolutions")
data class ConflictResolutionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "conflict_type")
    val conflictType: String,
    @ColumnInfo(name = "event_a_id")
    val eventAId: String,
    @ColumnInfo(name = "event_b_id")
    val eventBId: String,
    val action: String, // "MOVE_A", "MOVE_B", "DELETE_A", "DELETE_B", "MERGE"
    @ColumnInfo(name = "old_start_a")
    val oldStartA: Long? = null,
    @ColumnInfo(name = "new_start_a")
    val newStartA: Long? = null,
    @ColumnInfo(name = "old_start_b")
    val oldStartB: Long? = null,
    @ColumnInfo(name = "new_start_b")
    val newStartB: Long? = null,
    @ColumnInfo(name = "resolved_at_ms")
    val resolvedAtMs: Long = System.currentTimeMillis()
)
