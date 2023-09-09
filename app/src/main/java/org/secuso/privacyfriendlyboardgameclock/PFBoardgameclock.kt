package org.secuso.privacyfriendlyboardgameclock

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager
import org.secuso.privacyfriendlyboardgameclock.backup.BackupCreator
import org.secuso.privacyfriendlyboardgameclock.backup.BackupRestorer

class PFBoardgameclock : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        BackupManager.backupCreator = BackupCreator()
        BackupManager.backupRestorer = BackupRestorer()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
    }
}