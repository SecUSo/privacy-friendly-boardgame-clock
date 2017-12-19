package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.PlayerManagementActivity;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

public class PlayerManagementEditPlayerFragment extends DialogFragment {

    private static final int CAMERA_REQUEST = 1888;
    private Activity activity;
    private View rootView;
    private PlayersDataSourceSingleton pds;
    private Bitmap playerIcon;
    private EditText playerName;
    private ImageView pictureIMGView;
    private Player p;
    private Button confirmButtonGrey;
    private Button confirmButtonBlue;
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

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_player_management_newplayer, container, false);
        activity = getActivity();

        p = ((PlayerManagementActivity)activity).getPlayerToEdit();
        confirmButtonGrey = rootView.findViewById(R.id.confirmNewPlayerButtonGrey);
        confirmButtonBlue = rootView.findViewById(R.id.confirmNewPlayerButtonBlue);
        confirmButtonBlue.setOnClickListener(confirmButtonListener);

        pds = PlayersDataSourceSingleton.getInstance(activity);

        playerName = rootView.findViewById(R.id.editName);
        playerName.setInputType(InputType.TYPE_CLASS_TEXT);
        playerName.setText(p.getName());

        playerIcon = p.getIcon();

        playerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                if (playerName.getText().toString().length() > 0) {
                    confirmButtonGrey.setVisibility(View.GONE);
                    confirmButtonBlue.setVisibility(View.VISIBLE);
                } else {
                    confirmButtonGrey.setVisibility(View.VISIBLE);
                    confirmButtonBlue.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        pictureIMGView = (ImageView) rootView.findViewById(R.id.picture);
        pictureIMGView.setImageBitmap(playerIcon);

        final Button colorButton = (Button) rootView.findViewById(R.id.colorButton);
        colorButton.setOnClickListener(colorWheelDialog());

        final Button photoButton = (Button) rootView.findViewById(R.id.photoButton);

        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            photoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST);
                }
            });
        } else
            photoButton.setVisibility(View.GONE);


        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity) {
            a = (Activity) context;
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
                                pictureIMGView.setColorFilter(selectedColor, PorterDuff.Mode.OVERLAY);
                                confirmButtonGrey.setVisibility(View.GONE);
                                confirmButtonBlue.setVisibility(View.VISIBLE);
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
