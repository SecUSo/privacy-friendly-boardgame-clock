package org.secuso.privacyfriendlyboardgameclock.helpers;

import android.view.View;

/**
 * Created by Quang Anh Dang on 03.12.2017.
 * https://www.youtube.com/watch?v=puyiZKvxBa0
 * TODO JAVA DOC
 */

public interface ItemClickListener {
    void onItemClick(View view, int position);
    boolean onItemLongClicked(View view, int position);
}
