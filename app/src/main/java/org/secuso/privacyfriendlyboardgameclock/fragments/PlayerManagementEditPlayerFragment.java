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

package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.PlayerManagementActivity;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

/**
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This Fragment opens up a dialog from which user can edit existing players
 */
public class PlayerManagementEditPlayerFragment extends DialogFragment {
    private static final int CAMERA_REQUEST = 1888;
    private Activity activity;
    private View rootView;
    private PlayersDataSourceSingleton pds;
    private Bitmap playerIcon;
    private EditText playerName;
    private ImageView pictureIMGView;
    private ImageView colorIMGView;
    private Player p;
    View.OnClickListener confirmButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            p.setName(playerName.getText().toString());
            pictureIMGView.buildDrawingCache();
            playerIcon = pictureIMGView.getDrawingCache();
            p.setIcon(playerIcon);
            pds.updatePlayer(p);

            activity.onBackPressed();

        }
    };

    public PlayerManagementEditPlayerFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static PlayerManagementEditPlayerFragment newInstance(String title){
        PlayerManagementEditPlayerFragment frag = new PlayerManagementEditPlayerFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.editPlayer)
                .setPositiveButton(R.string.confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                p.setName(playerName.getText().toString());
                                pictureIMGView.buildDrawingCache();
                                playerIcon = pictureIMGView.getDrawingCache();
                                p.setIcon(playerIcon);
                                pds.updatePlayer(p);
                                // reload the activity starting this
                                Intent intent = getActivity().getIntent();
                                getActivity().finish();
                                startActivity(intent);
                            }
                        }
                )
                .setNeutralButton(R.string.playerStatistic, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft  = fm.beginTransaction();
                        Fragment prev = fm.findFragmentByTag(TAGHelper.DIALOG_FRAGMENT);
                        if(prev != null) ft.remove(prev);
                        ft.addToBackStack(null);

                        // Create and show the dialog
                        PlayerManagementStatisticsFragment playerStatisticFragment = PlayerManagementStatisticsFragment.newInstance("Player Statistic");
                        playerStatisticFragment.show(ft,TAGHelper.DIALOG_FRAGMENT);
                    }
                });
        LayoutInflater inflater = activity.getLayoutInflater();
        rootView = inflater.inflate(R.layout.fragment_player_management_newplayer, null);

        p = ((PlayerManagementActivity)activity).getPlayerToEdit();

        pds = PlayersDataSourceSingleton.getInstance(activity);

        playerName = rootView.findViewById(R.id.editName);
        playerName.setInputType(InputType.TYPE_CLASS_TEXT);
        playerName.setText(p.getName());

        playerIcon = p.getIcon();
        pictureIMGView = (ImageView) rootView.findViewById(R.id.picture);
        pictureIMGView.setImageBitmap(playerIcon);

        colorIMGView = rootView.findViewById(R.id.color);
        colorIMGView.setOnClickListener(colorWheelDialog());

        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            pictureIMGView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST);
                }
            });
        }

        builder.setView(rootView);
        return builder.create();
    }

    private View.OnClickListener colorWheelDialog() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(activity)
                        .setTitle("Choose color")
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                            }
                        })
                        .setPositiveButton("OK", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                pictureIMGView.setImageBitmap(playerIcon);
                                pictureIMGView.setColorFilter(selectedColor, PorterDuff.Mode.OVERLAY);
                                colorIMGView.setBackgroundColor(selectedColor);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .build()
                        .show();
            }
        };
    }


    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get("data");
            ImageView picture = (ImageView) rootView.findViewById(R.id.picture);

            playerIcon = Bitmap.createScaledBitmap(cutSquareBitmap(photo), 288, 288, false);
            picture.setImageBitmap(playerIcon);
        }
    }

    private Bitmap cutSquareBitmap(Bitmap b) {
        int bHeight = b.getHeight();
        int bWidth = b.getWidth();
        int longEdge = bHeight;
        int shortEdge = bWidth;

        if (bWidth > bHeight) {
            longEdge = bWidth;
            shortEdge = bHeight;
        }

        int diff = longEdge - shortEdge;

        return Bitmap.createBitmap(b, 0, diff / 2, shortEdge, shortEdge);
    }
}
