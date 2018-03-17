package org.secuso.privacyfriendlyboardgameclock.helpers;

import android.view.View;

/**
 * Created by Quang Anh Dang on 03.12.2017.
 * Tutorial: https://www.youtube.com/watch?v=puyiZKvxBa0
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 */

public interface ItemClickListener {
    void onItemClick(View view, int position);
    boolean onItemLongClicked(View view, int position);
}
