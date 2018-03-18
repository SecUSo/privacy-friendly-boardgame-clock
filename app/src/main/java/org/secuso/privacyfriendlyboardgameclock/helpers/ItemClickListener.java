/*
 This file is part of Privacy Friendly Board Game Clock.

 Privacy Friendly Board Game Clock is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly App Example is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlyboardgameclock.helpers;

import android.view.View;

/**
 * Created by Quang Anh Dang on 03.12.2017.
 * Tutorial: https://www.youtube.com/watch?v=puyiZKvxBa0
 * Last changed on 18.03.18
 * @author Quang Anh Dang
 */

public interface ItemClickListener {
    void onItemClick(View view, int position);
    boolean onItemLongClicked(View view, int position);
}
