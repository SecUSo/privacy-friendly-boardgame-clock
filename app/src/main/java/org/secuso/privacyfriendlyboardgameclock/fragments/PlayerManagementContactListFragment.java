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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * Adding a new player from contact list. This Fragment open a RecycleView with a list of all contacts
 * from which user can choose.
 */
public class PlayerManagementContactListFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>,ItemClickListener {
    // columns requested from the database
    private Activity activity;
    private View emptyListLayout;
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
        emptyListLayout = v.findViewById(R.id.emptyListLayout);
        pds = PlayersDataSourceSingleton.getInstance(null);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        contactListRecycleView = v.findViewById(R.id.contactList);
        contactListRecycleView.setAdapter(contactListAdapter);
        contactListRecycleView.setLayoutManager(layoutManager);
        contactListRecycleView.setItemAnimator(null);

        // and tell loader manager to start loading
        getLoaderManager().initLoader(0, null, this);

        builder.setView(v);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
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

        final AlertDialog dialog = (AlertDialog) getDialog();
        int countContacts = 1;
        if(contactListAdapter.getCursorAdapter().getCursor() != null){
            countContacts = contactListAdapter.getCursorAdapter().getCursor().getCount();
        }
        if (dialog != null) {
            // If no contacts found, change title of dialog
            if(countContacts == 0)
                emptyListLayout.setVisibility(View.VISIBLE);
            else
                emptyListLayout.setVisibility(View.GONE);
        }
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
