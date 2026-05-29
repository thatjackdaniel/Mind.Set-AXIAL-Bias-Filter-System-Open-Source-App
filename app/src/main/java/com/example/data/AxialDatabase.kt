package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "axial_signals")
data class AxialSignalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val originalText: String,
    val jsonResult: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AxialSignalDao {
    @Query("SELECT * FROM axial_signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<AxialSignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: AxialSignalEntity)

    @Query("DELETE FROM axial_signals WHERE id = :id")
    suspend fun deleteSignalById(id: Int)

    @Query("DELETE FROM axial_signals")
    suspend fun deleteAllSignals()
}

@Database(entities = [AxialSignalEntity::class], version = 1, exportSchema = false)
abstract class AxialDatabase : RoomDatabase() {
    abstract fun axialSignalDao(): AxialSignalDao

    companion object {
        @Volatile
        private var INSTANCE: AxialDatabase? = null

        fun getDatabase(context: Context): AxialDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AxialDatabase::class.java,
                    "axial_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
