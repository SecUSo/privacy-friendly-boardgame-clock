package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
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

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.db.PlayersDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;

public class EditPlayerFragment extends Fragment {

    Activity activity;
    View rootView;
    PlayersDataSource playersDataSource;
    private static final int CAMERA_REQUEST = 1888;
    Bitmap playerIcon;
    EditText playerName;
    ImageView picture;

    Player p;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_edit_player, container, false);
        activity = getActivity();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(activity.getString(R.string.editPlayer));
        container.removeAllViews();


        p = ((MainActivity) activity).getPlayerForEditing();

        final Button confirmEditPlayerButtonBlue = (Button) rootView.findViewById(R.id.confirmEditPlayerButtonBlue);
        final Button confirmEditPlayerButtonGrey = (Button) rootView.findViewById(R.id.confirmEditPlayerButtonGrey);

        confirmEditPlayerButtonBlue.setOnClickListener(confirmButtonListener);
        confirmEditPlayerButtonGrey.setOnClickListener(confirmButtonListener);

        playersDataSource = new PlayersDataSource(this.getActivity());

        playerName = (EditText) rootView.findViewById(R.id.editName);
        playerName.setInputType(InputType.TYPE_CLASS_TEXT);
        playerName.setText(p.getName());

        playerIcon = p.getIcon();

        playerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                if (playerName.getText().toString().length() > 0) {
                    confirmEditPlayerButtonBlue.setVisibility(View.VISIBLE);
                    confirmEditPlayerButtonGrey.setVisibility(View.GONE);
                } else {
                    confirmEditPlayerButtonBlue.setVisibility(View.GONE);
                    confirmEditPlayerButtonGrey.setVisibility(View.VISIBLE);
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
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.editPlayer))
                    .setMessage(activity.getString(R.string.confirmEditPart1) + playerName.getText() + activity.getString(R.string.confirmEditPart2))
                    .setPositiveButton(activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            playersDataSource.open();
                            p.setName(playerName.getText().toString());
                            picture.buildDrawingCache();
                            playerIcon = picture.getDrawingCache();
                            p.setIcon(playerIcon);
                            playersDataSource.updatePlayer(p);
                            playersDataSource.close();

                            activity.onBackPressed();
                        }

                    })
                    .setNegativeButton(activity.getString(R.string.cancel), null)
                    .show();
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
