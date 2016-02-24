package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.db.PlayersDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.view.PFAListAdapter;

public class ContactListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private CursorAdapter mAdapter;

    private Loader<Cursor> contacts;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // create adapter once
        Context context = getActivity();
        int layout = R.layout.listview_item_row;
        Cursor c = null; // there is no cursor yet
        int flags = 0; // no auto-requery! Loader requeries.
        mAdapter = new SimpleCursorAdapter(context, layout, c, FROM, TO, flags);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_list, null);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // each time we are started use our listadapter
        setListAdapter(mAdapter);
        // and tell loader manager to start loading
        getLoaderManager().initLoader(0, null, this);

        final Button b = (Button) getView().findViewById(R.id.addContactSelectionButton);
        final ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (lv.getCheckedItemCount() > 0) {
                    b.setText("Add Selected Contacts (" + lv.getCheckedItemCount() + ")");
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SparseBooleanArray checked = lv.getCheckedItemPositions();
                            int size = checked.size();
                            PlayersDataSource pds = new PlayersDataSource(getActivity());
                            pds.open();

                            for (int i = 0; i < size; i++) {
                                int key = checked.keyAt(i);
                                boolean value = checked.get(key);
                                if (value) {
                                    Cursor c = (Cursor) getListAdapter().getItem(0);
                                    c.move(key);
                                    String name = c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME));
                                    String photoThumbnailUri = c.getString(c.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI));
                                    pds.createPlayer(name, photoThumbnailUri);
                                }
                            }
                            pds.getAllPlayers();
                            pds.close();

                            getActivity().onBackPressed();
                        }
                    });

                } else
                    b.setText("Add Selected Contacts (0)");

            }
        });

    }

    // columns requested from the database
    private static final String[] PROJECTION = {
            Contacts._ID, // _ID is always required
            Contacts.DISPLAY_NAME, // that's what we want to display
            Contacts.PHOTO_THUMBNAIL_URI
    };

    // and name should be displayed in the text1 textview in item layout
    private static final String[] FROM = { Contacts.DISPLAY_NAME, Contacts.PHOTO_THUMBNAIL_URI };

    private static final int[] TO = { R.id.textViewName, R.id.imageViewIcon };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // load from the "Contacts table"
        Uri contentUri = Contacts.CONTENT_URI;

        // no sub-selection, no sort order, simply every row
        // projection says we want just the _id and the name column
        this.contacts = new CursorLoader(getActivity(),
                contentUri,
                PROJECTION,
                null,
                null,
                Contacts.DISPLAY_NAME + " ASC");

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
}