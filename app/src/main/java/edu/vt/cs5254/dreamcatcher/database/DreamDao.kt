package edu.vt.cs5254.dreamcatcher.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import edu.vt.cs5254.dreamcatcher.Dream
import edu.vt.cs5254.dreamcatcher.DreamEntry
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DreamDao {
    @Query("SELECT * FROM dream d JOIN dream_entry e ON e.dreamId = d.id ORDER BY d.lastUpdated DESC")
    fun getDreams(): Flow<Map<Dream, List<DreamEntry>>>

    @Query("SELECT * FROM dream WHERE id=(:id)")
    suspend fun internalGetDream(id: UUID):Dream

    @Query("SELECT * FROM dream_entry WHERE dreamId=(:dreamId)")
    suspend fun internalGetEntriesForDream(dreamId: UUID): List<DreamEntry>

    @Transaction
    suspend fun getDreamAndEntries(id: UUID): Dream {
        return internalGetDream(id).apply { entries = internalGetEntriesForDream(id) }
    }

    @Update
    suspend fun internalUpdateDream(dream: Dream)

    @Insert
    suspend fun internalInsertDreamEntry(dreamEntry: DreamEntry)

    @Query("DELETE FROM dream_entry WHERE dreamId = (:dreamId)")
    suspend fun internalDeleteEntriesFromDream(dreamId: UUID)

    @Transaction
    suspend fun updateDreamAndEntries(dream: Dream){
        internalDeleteEntriesFromDream(dream.id)
        dream.entries.forEach { internalInsertDreamEntry(it) }
        internalUpdateDream(dream)
    }

    @Insert
    suspend fun internalInsertDream(dream: Dream)


    @Transaction
    suspend fun insertDreamAndEntries(dream: Dream){
        internalInsertDream(dream)
        dream.entries.forEach { internalInsertDreamEntry(it) }
    }

    @Delete
    suspend fun internalDeleteDream(dream: Dream)

    @Transaction
    suspend fun deleteDreamAndEntries(dream: Dream){
        internalDeleteEntriesFromDream(dream.id)
        internalDeleteDream(dream)
    }

}