/*
 This file is part of Privacy Friendly Board Game Clock.

 Privacy Friendly Board Game Clock is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Board Game Clock is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Board Game Clock. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlyboardgameclock.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.secuso.pfacore.model.DrawerElement
import org.secuso.pfacore.model.dialog.AbortElseDialog
import org.secuso.pfacore.model.permission.FileLoadOptions
import org.secuso.pfacore.model.permission.FileSaveOptions
import org.secuso.pfacore.model.permission.loadFileFromUserChosenPlace
import org.secuso.pfacore.model.permission.saveToUserChosenPlace
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Activity Class for the Back Up Page
 */
class BackUpActivity : BaseActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private val useReadExternalStorage = loadFileFromUserChosenPlace { inputStream ->
        val currentDBPath = this.getDatabasePath(BoardGameClockDatabase.DATABASE_NAME).getPath()
        val currentDB = File(currentDBPath)

        val bufferedInputStream = BufferedInputStream(inputStream)
        bufferedInputStream.mark(1024)
        val bytes = ByteArray(16)
        bufferedInputStream.read(bytes)
        val header = String(bytes, Charsets.US_ASCII)
        if (!header.contains("SQLite format 3")) {
            Log.e("DatabaseImport", "File is not a valid sqlite db: ${header}")
            Toast.makeText(this, R.string.noBackupFound, Toast.LENGTH_LONG).show()
            return@loadFileFromUserChosenPlace
        }

        bufferedInputStream.reset()
        FileOutputStream(currentDB).use { outputStream ->
            copy(bufferedInputStream, outputStream)
        }

        Toast.makeText(
            this,
            R.string.importDatabaseBackupSuccess,
            Toast.LENGTH_LONG
        ).show()
    }

    private val useWriteExternalStorage = saveToUserChosenPlace { outputStream ->
        val currentDBPath = this.getDatabasePath(BoardGameClockDatabase.DATABASE_NAME).getPath()
        val currentDB = File(currentDBPath)
        FileInputStream(currentDB).use { inputStream ->
            copy(inputStream, outputStream)
        }

        Toast.makeText(
            this,
            R.string.exportDatabaseBackupSuccess,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun isActiveDrawerElement(element: DrawerElement) = element.icon == R.drawable.ic_menu_backup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        findViewById<Button>(R.id.importBackupButton).setOnClickListener {
            AbortElseDialog.build(this) {
                title = { ContextCompat.getString(this@BackUpActivity, R.string.importDatabaseBackup) }
                content = { ContextCompat.getString(this@BackUpActivity, R.string.importDatabaseBackupInfoMessage) }
                acceptLabel = ContextCompat.getString(this@BackUpActivity, R.string.yes)
                abortLabel = ContextCompat.getString(this@BackUpActivity, R.string.no)
                icon = android.R.drawable.ic_menu_help

                onElse = {
                    useReadExternalStorage(FileLoadOptions())
                }
            }.show()
        }
        findViewById<Button>(R.id.exportBackupButton).setOnClickListener {
            AbortElseDialog.build(this) {
                title = { ContextCompat.getString(this@BackUpActivity, R.string.importDatabaseBackup) }
                content = { ContextCompat.getString(this@BackUpActivity, R.string.importDatabaseBackupInfoMessage) }
                acceptLabel = ContextCompat.getString(this@BackUpActivity, R.string.yes)
                abortLabel = ContextCompat.getString(this@BackUpActivity, R.string.no)
                icon = android.R.drawable.ic_menu_help

                onElse = {
                    useWriteExternalStorage(FileSaveOptions("pfa-boardgameclock-database.db", "application/x-sqlite3"))
                }
            }.show()
        }
    }

    @Throws(IOException::class)
    private fun copy(src: InputStream, dst: OutputStream) {
        // Transfer bytes from in to out
        val buf = ByteArray(1024)
        var len: Int
        while ((src.read(buf).also { len = it }) > 0) {
            dst.write(buf, 0, len)
        }
    }
}
