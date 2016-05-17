package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.List;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.db.PlayersDataSource;

public class CreateNewPlayerFragment extends Fragment {

    Activity activity;
    View rootView;
    private PlayersDataSource pds;
    private static final int CAMERA_REQUEST = 1888;
    Bitmap playerIcon;
    EditText playerName;
    ImageView picture;
    Button confirmNewPlayerButtonBlue, confirmNewPlayerButtonGrey;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_new_player, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.createNewPlayer);
        container.removeAllViews();
        activity = getActivity();

        playerIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_launcher);

        confirmNewPlayerButtonBlue = (Button) rootView.findViewById(R.id.confirmNewPlayerButtonBlue);
        confirmNewPlayerButtonGrey = (Button) rootView.findViewById(R.id.confirmNewPlayerButtonGrey);

        confirmNewPlayerButtonBlue.setOnClickListener(confirmButtonListener);
        confirmNewPlayerButtonGrey.setOnClickListener(confirmButtonListener);

        pds = ((MainActivity) getActivity()).getPlayersDataSource();

        playerName = (EditText) rootView.findViewById(R.id.editName);
        playerName.setInputType(InputType.TYPE_CLASS_TEXT);
        playerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                if (playerName.getText().toString().length() > 0) {
                    confirmNewPlayerButtonBlue.setVisibility(View.VISIBLE);
                    confirmNewPlayerButtonGrey.setVisibility(View.GONE);
                } else {
                    confirmNewPlayerButtonBlue.setVisibility(View.GONE);
                    confirmNewPlayerButtonGrey.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        picture = (ImageView) rootView.findViewById(R.id.picture);
        picture.setImageBitmap(playerIcon);

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

    View.OnClickListener confirmButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

                            picture.buildDrawingCache();
                            playerIcon = picture.getDrawingCache();

                            pds.createPlayer(playerName.getText().toString(), playerIcon);

                            activity.onBackPressed();
        }
    };

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
                        .setTitle(activity.getString(R.string.pickColor))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                            }
                        })
                        .setPositiveButton(activity.getString(R.string.ok), new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                picture.setImageBitmap(playerIcon);
                                picture.setColorFilter(selectedColor, PorterDuff.Mode.OVERLAY);
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.cancel), null)
                        .build()
                        .show();
            }
        };
    }


    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        if (requestCode == CAMERA_REQUEST && resultCode == activity.RESULT_OK) {
            Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get("data");
            ImageView picture = (ImageView) rootView.findViewById(R.id.picture);

            playerIcon = Bitmap.createScaledBitmap(cutSquareBitmap(photo), 288, 288, false);
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
