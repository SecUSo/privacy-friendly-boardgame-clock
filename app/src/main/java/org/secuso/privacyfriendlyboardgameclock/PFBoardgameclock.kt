package org.secuso.privacyfriendlyboardgameclock

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Configuration
import org.secuso.pfacore.ui.PFApplication
import org.secuso.pfacore.ui.PFData
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity
import org.secuso.privacyfriendlyboardgameclock.backup.BackupCreator
import org.secuso.privacyfriendlyboardgameclock.backup.BackupRestorer

class PFBoardgameclock: PFApplication() {
    override val name: String
        get() = ContextCompat.getString(applicationContext, R.string.app_name)
    override val data: PFData
    override val mainActivity = MainActivity::class.java

    override val workManagerConfiguration by lazy {
        Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
    }
}