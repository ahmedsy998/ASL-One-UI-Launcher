package com.example.data.db

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
import androidx.room.Update
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.Flow

@JsonClass(generateAdapter = true)
@Entity(tableName = "home_items")
data class HomeItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val page: Int = 0,
    val row: Int = 0,
    val col: Int = 0,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val isDock: Boolean = false,
    val itemType: String, // HomeItemType.name
    val packageName: String = "",
    val title: String = "",
    val iconKindName: String = "GENERIC_APP",
    val folderAppsJson: String = "[]",
    val folderColorHex: String = "#3B82F6",
    val builtInWidgetType: String = "",
    val systemAppWidgetId: Int = -1,
    val widgetBgColorHex: String = "#1E293B",
    val widgetAlpha: Float = 0.85f,
    val customIconUri: String = ""
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "app_overrides")
data class AppOverrideEntity(
    @PrimaryKey val packageName: String,
    val isHidden: Boolean = false,
    val customLabel: String = "",
    val customIconUri: String = "",
    val launchCount: Int = 0,
    val lastLaunchedAt: Long = 0L,
    val drawerFolder: String = ""
)

@Dao
interface LauncherDao {
    @Query("SELECT * FROM home_items ORDER BY isDock ASC, page ASC, row ASC, col ASC")
    fun observeAllHomeItems(): Flow<List<HomeItemEntity>>

    @Query("SELECT * FROM home_items ORDER BY isDock ASC, page ASC, row ASC, col ASC")
    suspend fun getAllHomeItemsOnce(): List<HomeItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeItem(item: HomeItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeItems(items: List<HomeItemEntity>)

    @Update
    suspend fun updateHomeItem(item: HomeItemEntity)

    @Query("DELETE FROM home_items WHERE id = :id")
    suspend fun deleteHomeItemById(id: Long)

    @Query("DELETE FROM home_items WHERE packageName = :packageName")
    suspend fun deleteHomeItemsByPackage(packageName: String)

    @Query("DELETE FROM home_items")
    suspend fun clearAllHomeItems()

    @Query("SELECT * FROM app_overrides")
    fun observeAppOverrides(): Flow<List<AppOverrideEntity>>

    @Query("SELECT * FROM app_overrides")
    suspend fun getAllAppOverridesOnce(): List<AppOverrideEntity>

    @Query("SELECT * FROM app_overrides WHERE packageName = :pkg LIMIT 1")
    suspend fun getAppOverride(pkg: String): AppOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppOverride(overrideEntity: AppOverrideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppOverrides(list: List<AppOverrideEntity>)

    @Query("DELETE FROM app_overrides WHERE packageName = :pkg")
    suspend fun deleteAppOverride(pkg: String)

    @Query("DELETE FROM app_overrides")
    suspend fun clearAllAppOverrides()
}

@Database(
    entities = [HomeItemEntity::class, AppOverrideEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun launcherDao(): LauncherDao

    companion object {
        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        fun getInstance(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "asl_oneui9_launcher.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
