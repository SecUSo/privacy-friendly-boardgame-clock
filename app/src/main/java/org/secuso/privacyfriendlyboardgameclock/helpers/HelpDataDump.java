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

import android.content.Context;
import android.text.Html;

import org.secuso.privacyfriendlyboardgameclock.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Karola Marky
 * @version 20171016
 * Class structure taken from tutorial at http://www.journaldev.com/9942/android-expandablelistview-example-tutorial
 * last access 27th October 2016
 */

public class HelpDataDump {

    private Context context;
    private LinkedHashMap<CharSequence, List<CharSequence>> expandableListDetail;

    public HelpDataDump(Context context) {
        this.context = context;
    }

    public LinkedHashMap<CharSequence, List<CharSequence>> getDataGeneral() {
        expandableListDetail = new LinkedHashMap<>();
        addTextToList(context.getResources().getText(R.string.help_whatis),context.getResources().getText(R.string.help_whatis_answer));
        addTextToList(context.getResources().getText(R.string.help_privacy),context.getResources().getText(R.string.help_privacy_answer));
        addTextToList(context.getResources().getText(R.string.help_new_game),context.getResources().getText(R.string.help_new_game_answer));
        addTextToList(context.getResources().getText(R.string.help_game_mode),context.getResources().getText(R.string.help_game_mode_answer));
        addTextToList(context.getResources().getText(R.string.help_next_player),context.getResources().getText(R.string.help_next_player_answer));
        addTextToList(context.getResources().getText(R.string.help_permissions),context.getResources().getText(R.string.help_permissions_answer));

        return expandableListDetail;
    }

    private void addTextToList(CharSequence title, CharSequence answer){
        List<CharSequence> tmp = new ArrayList<>();
        tmp.add(answer);
        expandableListDetail.put(title, tmp);
    }

}
