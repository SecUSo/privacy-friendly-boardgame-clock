package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;

import java.io.IOException;

/**
 * Created by Quang Anh Dang on 03.12.2017.
 */

public class PlayerManagementContactListFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    // columns requested from the database
    private static final String[] PROJECTION = {
            ContactsContract.Contacts._ID, // _ID is always required
            ContactsContract.Contacts.DISPLAY_NAME, // that's what we want to display
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
    };
    private ListView contactListView;
    private Button confirmContactButton;
    // and name should be displayed in the text1 textview in item layout
    private static final String[] FROM = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
    private static final int[] TO = {R.id.player_text, R.id.player_image};
    private CursorAdapter mAdapter;
    private Loader<Cursor> contacts;
    private PlayersDataSourceSingleton pds;
    View.OnClickListener confirmButtonOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            SparseBooleanArray checked = contactListView.getCheckedItemPositions();
            int size = checked.size();

            for (int i = 0; i < size; i++) {
                int key = checked.keyAt(i);
                boolean value = checked.get(key);
                if (value) {
                    try {
                        Cursor c = (Cursor) contactListView.getAdapter().getItem(0);
                        c.move(key);
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        String photoThumbnailUri = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                        Bitmap androidIcon = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.ic_android);

                        if (photoThumbnailUri != null) {
                            // TODO Exception not found
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(photoThumbnailUri));
                            pds.createPlayer(name, Bitmap.createScaledBitmap(cutSquareBitmap(bitmap), androidIcon.getWidth(), androidIcon.getHeight(), false));
                        } else {
                            pds.createPlayer(name, androidIcon);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            getActivity().onBackPressed();
        }
    };

    public PlayerManagementContactListFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static PlayerManagementContactListFragment newInstance(String title){
        PlayerManagementContactListFragment frag = new PlayerManagementContactListFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // create adapter once
        Context context = getActivity();
        int layout = R.layout.player_management_contactlist_row; // Maybe we have to create new custom thing
        Cursor c = null; // there is no cursor yet
        int flags = 0; // no auto-requery! Loader requeries.
        mAdapter = new SimpleCursorAdapter(context, layout, c, FROM, TO, flags);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_list, null);
        pds = PlayersDataSourceSingleton.getInstance(null);
        contactListView = v.findViewById(R.id.contactListView);

        // each time we are started use our listadapter
        contactListView.setAdapter(mAdapter);
        // and tell loader manager to start loading
        getLoaderManager().initLoader(0, null, this);

        confirmContactButton = v.findViewById(R.id.addContactSelectionButton);
        contactListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (contactListView.getCheckedItemCount() > 0) {
                    confirmContactButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_fullwidth));
                    confirmContactButton.setOnClickListener(confirmButtonOnClickListener);

                    if (contactListView.getCheckedItemCount() == 1)
                        confirmContactButton.setText(R.string.addContact);
                    if (contactListView.getCheckedItemCount() > 1)
                        confirmContactButton.setText(getString(R.string.addContacts));


                } else {
                    confirmContactButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_disabled));
                    confirmContactButton.setText(getString(R.string.addContact));
                    confirmContactButton.setOnClickListener(null);
                }

            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // load from the "Contacts table"
        Uri contentUri = ContactsContract.Contacts.CONTENT_URI;

        // no sub-selection, no sort order, simply every row
        // projection says we want just the _id and the name column
        this.contacts = new CursorLoader(getActivity(),
                contentUri,
                PROJECTION,
                ContactsContract.Contacts.DISPLAY_NAME + " IS NOT NULL ",
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        return contacts;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Once cursor is loaded, give it to adapter
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // on reset take any old cursor away
        mAdapter.swapCursor(null);
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
