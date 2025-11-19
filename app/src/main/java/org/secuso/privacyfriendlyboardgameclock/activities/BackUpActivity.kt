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

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.secuso.pfacore.model.DrawerElement
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.database.DbHelper
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
    private val REQUEST_READ_EXTERNAL_STORAGE = 1
    private val REQUEST_WRITE_EXTERNAL_STORAGE = 2

    override fun isActiveDrawerElement(element: DrawerElement) = element.icon == R.drawable.ic_menu_backup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        findViewById<Button>(R.id.importBackupButton).setOnClickListener { importBackupButton() }
        findViewById<Button>(R.id.exportBackupButton).setOnClickListener { exportBackupButton() }
    }

    fun importBackupButton() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE
            )
        }  else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setTitle(R.string.importDatabaseBackup)
                .setMessage(R.string.importDatabaseBackupInfoMessage)
                .setPositiveButton(R.string.yes
                ) { dialog, whichButton -> importBackup() }
                .setNegativeButton(R.string.no, null)
                .setIcon(android.R.drawable.ic_menu_help)
                .show()
        }
    }

    fun exportBackupButton() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL_STORAGE
            )
        } else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setTitle(R.string.exportDatabaseBackup)
                .setMessage(R.string.exportDatabaseBackupInfoMessage)
                .setPositiveButton(R.string.yes
                ) { dialog, whichButton -> exportBackup() }
                .setNegativeButton(R.string.no, null)
                .setIcon(android.R.drawable.ic_menu_help)
                .show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.importDatabaseBackup)
                        .setMessage(R.string.importDatabaseBackupInfoMessage)
                        .setPositiveButton(R.string.yes
                        ) { dialog, whichButton -> importBackup() }
                        .setNegativeButton(R.string.no, null)
                        .setIcon(android.R.drawable.ic_menu_help)

                        .show()
                }
                return
            }

            REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.exportDatabaseBackup)
                        .setMessage(R.string.exportDatabaseBackupInfoMessage)
                        .setPositiveButton(R.string.yes, object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, whichButton: Int) {
                                exportBackup()
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .setIcon(android.R.drawable.ic_menu_help)

                        .show()
                }
                return
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun importBackup() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) try {
            val sd = Environment.getExternalStorageDirectory()

            if (sd.canWrite()) {
                val currentDBPath = this.getDatabasePath(DbHelper.DB_NAME).getPath()
                val backupDBPath = "pfa-boardgameclock-database.db"
                val currentDB = File(currentDBPath)
                val backupDB = File(sd, backupDBPath)

                if (backupDB.exists()) {
                    copy(backupDB, currentDB)
                }

                if (currentDB.exists() && backupDB.length() == currentDB.length()) Toast.makeText(
                    this,
                    R.string.importDatabaseBackupSuccess,
                    Toast.LENGTH_LONG
                ).show()
                else Toast.makeText(this, R.string.noBackupFound, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
        }
    }

    private fun exportBackup() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) try {
            val sd = Environment.getExternalStorageDirectory()

            if (sd.canWrite()) {
                val currentDBPath = this.getDatabasePath(DbHelper.DB_NAME).getPath()
                val backupDBPath = "pfa-boardgameclock-database.db"
                val currentDB = File(currentDBPath)
                val backupDB = File(sd, backupDBPath)

                if (currentDB.exists()) {
                    copy(currentDB, backupDB)
                }

                if (backupDB.exists() && backupDB.length() == currentDB.length()) Toast.makeText(
                    this, getString(
                        R.string.exportDatabaseBackupSuccess
                    ) + " " + backupDB.getPath(), Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, R.string.backupExportError, Toast.LENGTH_LONG).show()
        }
    }

    @Throws(IOException::class)
    private fun copy(src: File?, dst: File?) {
        val `in`: InputStream = FileInputStream(src)
        val out: OutputStream = FileOutputStream(dst)

        // Transfer bytes from in to out
        val buf = ByteArray(1024)
        var len: Int
        while ((`in`.read(buf).also { len = it }) > 0) {
            out.write(buf, 0, len)
        }
        `in`.close()
        out.close()
    }
}
