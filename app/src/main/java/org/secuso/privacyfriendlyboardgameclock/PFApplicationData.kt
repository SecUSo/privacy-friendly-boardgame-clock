package org.secuso.privacyfriendlyboardgameclock

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.map
import org.secuso.pfacore.application.PFData
import org.secuso.pfacore.model.Theme
import org.secuso.pfacore.model.about.About
import org.secuso.pfacore.model.preferences.Preferable
import org.secuso.pfacore.ui.help.Help
import org.secuso.pfacore.ui.preferences.appPreferences
import org.secuso.pfacore.ui.preferences.settings.appearance
import org.secuso.pfacore.ui.preferences.settings.general
import org.secuso.pfacore.ui.preferences.settings.preferenceFirstTimeLaunch
import org.secuso.pfacore.ui.preferences.settings.settingDeviceInformationOnErrorReport
import org.secuso.pfacore.ui.preferences.settings.settingThemeSelector
import org.secuso.pfacore.ui.preferences.settings.switch
import org.secuso.pfacore.ui.tutorial.buildTutorial

class PFApplicationData private constructor(context: Context) {

    // Preferences
    lateinit var theme: Preferable<String>
        private set
    lateinit var firstTimeLaunch: Preferable<Boolean>
        private set
    lateinit var includeDeviceDataInReport: Preferable<Boolean>
        private set
    lateinit var showSwipeDialog: Preferable<Boolean>
        private set
    lateinit var gameNumber: Preferable<Int>
        private set


    private val preferences = appPreferences(context) {
        preferences {
            firstTimeLaunch = preferenceFirstTimeLaunch
            showSwipeDialog = preference {
                key = "showSwipeDialog"
                default = true
                backup = true
            }
            gameNumber = preference {
                key = "gameNumber"
                default = 1
                backup = true
            }
        }
        settings {
            appearance {
                theme = settingThemeSelector
            }
            general {
                includeDeviceDataInReport = settingDeviceInformationOnErrorReport
            }
        }
    }

    private val help = Help.build(context) {
        listOf(
            context.resources.getText(R.string.help_whatis) to context.resources.getText(R.string.help_whatis_answer),
            context.resources.getText(R.string.help_privacy) to context.resources.getText(R.string.help_privacy_answer),
            context.resources.getText(R.string.help_new_game) to context.resources.getText(R.string.help_new_game_answer),
            context.resources.getText(R.string.help_game_mode) to context.resources.getText(R.string.help_game_mode_answer),
            context.resources.getText(R.string.help_next_player) to context.resources.getText(R.string.help_next_player_answer),
            context.resources.getText(R.string.help_permissions) to context.resources.getText(R.string.help_permissions_answer),
            context.resources.getText(R.string.help_add_new_player_feature) to context.resources.getText(R.string.help_add_new_player_feature_answer)
        ).forEach {
            item {
                title { literal(it.first) }
                description { literal(it.second) }
            }
        }
    }

    private val about = About(
        name = context.resources.getString(R.string.app_name),
        version = BuildConfig.VERSION_NAME,
        authors = context.resources.getString(R.string.about_author_names),
        repo = context.resources.getString(org.secuso.pfacore.R.string.about_github)
    )

    private val tutorial = buildTutorial {
        stage {
            title = ContextCompat.getString(context, R.string.slide1_heading)
            description = ContextCompat.getString(context, R.string.slide1_text)
            images = single(R.mipmap.splash)
        }
        stage {
            title = ContextCompat.getString(context, R.string.slide2_heading)
            description = ContextCompat.getString(context, R.string.slide2_text)
            images = single(R.mipmap.splash)
        }
        stage {
            title = ContextCompat.getString(context, R.string.slide3_heading)
            description = ContextCompat.getString(context, R.string.slide3_text)
            images = single(R.mipmap.splash)
        }
    }

    val data = PFData(
        about = about,
        help = help,
        preferences = preferences,
        tutorial = tutorial,
        theme = theme.state.map { Theme.valueOf(it) },
        firstLaunch = firstTimeLaunch,
        includeDeviceDataInReport = includeDeviceDataInReport,
    )

    companion object {
        private var _instance: PFApplicationData? = null
        fun instance(context: Context): PFApplicationData {
            if (_instance == null) {
                _instance = PFApplicationData(context)
            }
            return _instance!!
        }
    }
}