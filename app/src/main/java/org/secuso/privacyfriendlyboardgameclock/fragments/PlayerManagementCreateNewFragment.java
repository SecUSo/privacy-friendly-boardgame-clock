package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.PlayerManagementActivity;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;

/**
 * Diaglog Fragment responsible for creating new Player
 * Step 2 in creating new player process
 * Created by Quang Anh Dang on 03.12.2017.
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 */

public class PlayerManagementCreateNewFragment extends DialogFragment {
    private static final int CAMERA_REQUEST = 1888;
    Activity activity;
    View rootView;
    Bitmap playerIcon;
    EditText playerNameEditText;
    ImageView pictureIMGView;
    ImageView colorIMGView;
    boolean confirmReady = false;
    private PlayersDataSourceSingleton pds;
    View.OnClickListener confirmButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(confirmReady){
                pictureIMGView.buildDrawingCache();
                playerIcon = pictureIMGView.getDrawingCache();

                pds.createPlayer(playerNameEditText.getText().toString(), playerIcon);

                // reload the activity starting this
                Intent intent = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent);
            }
            else{
                Toast toast = Toast.makeText(activity, getString(R.string.enterAName), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.editPlayer)
                .setPositiveButton(R.string.confirm,null)
                .setNegativeButton(R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        rootView = inflater.inflate(R.layout.fragment_player_management_newplayer, null);

        playerIcon = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_android);
        pds = PlayersDataSourceSingleton.getInstance(null);

        playerNameEditText = rootView.findViewById(R.id.editName);
        playerNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        playerNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                if (playerNameEditText.getText().toString().length() > 0) {
                    confirmReady = true;
                } else {
                    confirmReady = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        pictureIMGView = rootView.findViewById(R.id.picture);
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

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(confirmButtonListener);
        }
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
                                pictureIMGView.setColorFilter(selectedColor, PorterDuff.Mode.DST_OVER);
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

            playerIcon = Bitmap.createScaledBitmap(cutSquareBitmap(photo), picture.getWidth(), picture.getHeight(), false);
            picture.setImageBitmap(playerIcon);
            picture.setColorFilter(null);
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

        if (bWidth <= bHeight)
            return Bitmap.createBitmap(b, 0, diff / 2, shortEdge, shortEdge);
        else
            return Bitmap.createBitmap(b, diff / 2, 0, shortEdge, shortEdge);
    }
}
