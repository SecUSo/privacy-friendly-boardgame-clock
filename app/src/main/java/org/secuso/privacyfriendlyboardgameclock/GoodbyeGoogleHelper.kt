package org.secuso.privacyfriendlyboardgameclock

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.secuso.privacyfriendlyboardgameclock.databinding.DialogGoodbyeGogleBinding

fun checkGoodbyeGoogle(context: Context, layoutInflater: LayoutInflater) {
    val installSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.packageManager.getInstallSourceInfo(BuildConfig.APPLICATION_ID).installingPackageName
    } else {
        context.packageManager.getInstallerPackageName(BuildConfig.APPLICATION_ID)
    }

    val showNotice = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_goodbye_google_notice", true);

    if (installSource == "com.android.vending" && showNotice) {
        val binding = DialogGoodbyeGogleBinding.inflate(layoutInflater)
        binding.showNoticeCheckbox.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("show_goodbye_google_notice", !binding.showNoticeCheckbox.isChecked).apply()
        }
        val dialog = MaterialAlertDialogBuilder(context)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setTitle(R.string.dialog_goodbye_google_title)
            .setMessage(R.string.dialog_goodbye_google_desc)
            .setView(binding.root)
            .setNeutralButton(android.R.string.ok) { _,_ -> }
            .create()

        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
    }
}