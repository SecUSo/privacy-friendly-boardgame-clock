package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.helpers.ContactListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener;

import java.io.IOException;

/**
 * Created by Quang Anh Dang on 03.12.2017.
 */

public class PlayerManagementContactListFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>,ItemClickListener {
    // columns requested from the database
    private Activity activity;
    private static final String[] PROJECTION = {
            ContactsContract.Contacts._ID, // _ID is always required
            ContactsContract.Contacts.DISPLAY_NAME, // that's what we want to display
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
    };
    private RecyclerView contactListRecycleView;
    private Button confirmContactButton;
    // and name should be displayed in the text1 textview in item layout
    private static final String[] FROM = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
    private static final int[] TO = {R.id.player_text, R.id.player_image};
    private ContactListAdapter contactListAdapter;
    private Loader<Cursor> contacts;
    private PlayersDataSourceSingleton pds;
    DialogInterface.OnClickListener confirmButtonOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            SparseBooleanArray checked = contactListAdapter.getSelectedItemsAsSparseBooleanArray();
            int size = checked.size();

            for (int i = 0; i < size; i++) {
                int key = checked.keyAt(i);
                boolean value = checked.get(key);
                if (value) {
                    try {
                        Cursor c = (Cursor) contactListAdapter.getCursorAdapter().getItem(0);
                        c.move(key);
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        String photoThumbnailUri = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                        Bitmap androidIcon = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.ic_android);

                        if (photoThumbnailUri != null) {
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

            // reload the activity starting this
            Intent intent = getActivity().getIntent();
            getActivity().finish();
            startActivity(intent);
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
        activity = getActivity();
        int layout = R.layout.player_management_custom_row;
        Cursor c = null; // there is no cursor yet
        int flags = 0; // no auto-requery! Loader requeries.
        contactListAdapter = new ContactListAdapter(activity,this, new SimpleCursorAdapter(context, layout, c, FROM, TO, flags));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.chooseContacts))
                .setPositiveButton(R.string.confirm, confirmButtonOnClickListener)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().onBackPressed();
                    }
                });

        View v = activity.getLayoutInflater().inflate(R.layout.fragment_contact_list, null);
        pds = PlayersDataSourceSingleton.getInstance(null);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        contactListRecycleView = v.findViewById(R.id.contactList);
        contactListRecycleView.setAdapter(contactListAdapter);
        contactListRecycleView.setLayoutManager(layoutManager);

        // and tell loader manager to start loading
        getLoaderManager().initLoader(0, null, this);

        builder.setView(v);
        return builder.create();
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
        contactListAdapter.getCursorAdapter().swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // on reset take any old cursor away
        contactListAdapter.getCursorAdapter().swapCursor(null);
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

    @Override
    public void onItemClick(View view, int position) {
        if(!contactListAdapter.isLongClickedSelected() && !contactListAdapter.isSimpleClickedSelected()){
            contactListAdapter.setSimpleClickedSelected(true);
            contactListAdapter.setLongClickedSelected(false);
        }
        toggleSelection(position);
    }

    @Override
    public boolean onItemLongClicked(View view, int position) {
        return false;
    }

    private void toggleSelection(int position) {
        contactListAdapter.toggleSelection(position);
        int count = contactListAdapter.getSelectedItemCount();
        if(count == 0){
            contactListAdapter.setSimpleClickedSelected(false);
            contactListAdapter.setLongClickedSelected(false);
            contactListAdapter.notifyDataSetChanged();
        }
        else if(count == 1) contactListAdapter.notifyDataSetChanged();
    }
}
