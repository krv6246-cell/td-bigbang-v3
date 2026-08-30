package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Singularity Archive Record - Stores exported quantum state across universe cycles.
 */
@Entity(tableName = "singularity_records")
data class SingularityRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val universeCycle: Int,
    val levelName: String,
    val outcome: String, // "Evolutionary Ascendance" or "Gravitational Collapse"
    val dcoinsExported: Int,
    val survivalTimeSeconds: Float,
    val peakDensity: Float,
    val maxCombo: Int,
    val entropyResisted: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SingularityRecordDao {
    @Query("SELECT * FROM singularity_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<SingularityRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SingularityRecord): Long

    @Query("SELECT SUM(dcoinsExported) FROM singularity_records")
    fun getTotalDcoins(): Flow<Int?>

    @Query("DELETE FROM singularity_records")
    suspend fun clearArchive()
}

@Database(
    entities = [SingularityRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun singularityRecordDao(): SingularityRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "time_density_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class SingularityRepository(private val dao: SingularityRecordDao) {
    val allRecords: Flow<List<SingularityRecord>> = dao.getAllRecords()
    val totalDcoins: Flow<Int?> = dao.getTotalDcoins()

    suspend fun recordTransition(record: SingularityRecord): Long {
        return dao.insertRecord(record)
    }

    suspend fun clearArchive() {
        dao.clearArchive()
    }
}
