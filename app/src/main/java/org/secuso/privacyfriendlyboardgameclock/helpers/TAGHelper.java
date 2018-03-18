package org.secuso.privacyfriendlyboardgameclock.helpers;

/**
 * Created by Quang Anh Dang on 15.12.2017.
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 * This class contains all the TAGs
 */

public class TAGHelper {
    public static final String DIALOG_FRAGMENT = "dialog";
    // CountdownTimerService
    public static final int COUNT_DOWN_TIMER_NOTIFICATION_ID = 456721;
    public static final String GAME_COUNT_DOWN_TAG = "game count down";
    public static final String GAME_COUNT_IN_NEGATIVE_TAG = "game count in negative";
    public static final String ROUND_COUNT_DOWN_TAG = "round count down";
    public static final String ROUND_COUNT_IN_NEGATIVE_TAG = "round count in negative";
    public static final String COUNTDOWN_SERVICE_BROADCAST_TAG = "org.secuso.privacyfriendlyboardgameclock.services.CountdownTimerServiceBroadcast";
    public static final long DEFAULT_VALUE_LONG = -1;
    public static final String GAME_FINISHED_SIGNAL = "game finished";
    public static final String ROUND_FINISHED_SIGNAL = "round finished";
    public static final long COUNTDOWN_INTERVAL = 100;
    // Intent Tag
    public static final String GAME_INDEX_FROM_LIST = "game index from list";
    // Permissions TAGS
    public static final int REQUEST_READ_CONTACT_CODE = 7;
    // Game Mode Index
    public static final int CLOCKWISE = 0;
    public static final int COUNTER_CLOCKWISE = 1;
    public static final int RANDOM = 2;
    public static final int MANUAL_SEQUENCE= 3;
    public static final int TIME_TRACKING = 4;
    // TimeTrackingService
    public static final String GAME_TIME_TRACKING = "game time tracking";
}
