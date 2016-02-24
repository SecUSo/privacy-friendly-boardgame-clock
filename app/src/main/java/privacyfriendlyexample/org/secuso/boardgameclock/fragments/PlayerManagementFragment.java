package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.db.PlayersDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;
import privacyfriendlyexample.org.secuso.boardgameclock.view.PFAListAdapter;

public class PlayerManagementFragment extends Fragment {

    Activity activity;
    List<Player> list;
    PlayersDataSource playersDataSource;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_player_management, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.action_player_management);
        container.removeAllViews();

        final Button b = (Button) rootView.findViewById(R.id.createNewPlayerButton);
        b.setText("Create New Player");
        b.setBackgroundColor(getResources().getColor(R.color.darkblue));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewPlayer();
            }
        });

        playersDataSource = new PlayersDataSource(this.getActivity());
        playersDataSource.open();
        list = playersDataSource.getAllPlayers();
        playersDataSource.close();

        final ListView myListView = (ListView) rootView.findViewById(R.id.current_players_list);
        final PFAListAdapter listAdapter = new PFAListAdapter(this.getActivity(), R.id.current_players_list, list);

        myListView.setAdapter(listAdapter);
        myListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (myListView.getCheckedItemCount() > 0) {
                    b.setText("Delete Player (" + myListView.getCheckedItemCount() + ")");
                    b.setBackgroundColor(Color.RED);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deletePlayer(myListView);
                        }
                    });

                } else {
                    b.setText("Create New Player");
                    b.setBackgroundColor(getResources().getColor(R.color.darkblue));
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createNewPlayer();
                        }
                    });
                }

            }
        });

        final Button contactsButton = (Button) rootView.findViewById(R.id.addPlayerContactsButton);
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayerFromContacts();
            }
        });

        return rootView;
    }

    private void refreshFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    private void createNewPlayer() {
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(activity)
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Create New Player")
                .setMessage("Name:")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playersDataSource.open();
                        playersDataSource.createPlayer(input.getText().toString(), resourceToUri(activity, R.mipmap.ic_launcher));
                        playersDataSource.close();

                        refreshFragment();
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    private static String resourceToUri (Context context,int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID)).toString();
    }

    private void addPlayerFromContacts() {
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new ContactListFragment());
        fragmentTransaction.addToBackStack("ContactListFragment");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();

    }

    private void deletePlayer(final ListView lv) {
        new AlertDialog.Builder(activity)
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete Player (" + lv.getCheckedItemCount() + ")")
                .setMessage("Are you sure you want to delete the selected players? This step cannot be undone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SparseBooleanArray checked = lv.getCheckedItemPositions();
                        int size = checked.size();
                        playersDataSource.open();

                        for (int i = 0; i < size; i++) {
                            int key = checked.keyAt(i);
                            boolean value = checked.get(key);
                            if (value) {
                                playersDataSource.deletePlayer(list.get(key));
                                lv.setItemChecked(key, false);
                            }
                        }
                        list = playersDataSource.getAllPlayers();
                        playersDataSource.close();
                        refreshFragment();
                    }

                })
                .setNegativeButton("No", null)
                .show();

    }



}
