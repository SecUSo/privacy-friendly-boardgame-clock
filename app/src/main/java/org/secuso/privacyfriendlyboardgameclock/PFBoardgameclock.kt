package org.secuso.privacyfriendlyboardgameclock

import android.app.Activity
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Configuration
import org.secuso.pfacore.application.BackupDatabaseConfig
import org.secuso.pfacore.application.RoomDatabaseConfig
import org.secuso.pfacore.ui.PFApplication
import org.secuso.pfacore.ui.PFData
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase

class PFBoardgameclock: PFApplication() {
    override val name: String
        get() = ContextCompat.getString(applicationContext, R.string.app_name)
    override val data: PFData
        get() = PFApplicationData.instance(this).data
    override val mainActivity = MainActivity::class.java
    override val database: BackupDatabaseConfig
        get() = RoomDatabaseConfig(applicationContext, BoardGameClockDatabase.DATABASE_NAME, BoardGameClockDatabase::class.java)
    override val createBackup = true

    override val workManagerConfiguration by lazy {
        Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
    }
}