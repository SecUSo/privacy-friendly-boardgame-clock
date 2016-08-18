package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.db.GamesDataSource;
import org.secuso.privacyfriendlyboardgameclock.db.PlayersDataSource;
import org.secuso.privacyfriendlyboardgameclock.model.Player;
import org.secuso.privacyfriendlyboardgameclock.view.PlayerListAdapter;

import java.util.List;

public class PlayerManagementFragment extends Fragment {

    Activity activity;
    List<Player> list;
    PlayersDataSource pds;
    String selectedPlayerId = "-1";
    ListView myListView;
    PlayerListAdapter listAdapter;
    Button contactsButton;
    View.OnClickListener contactButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= 23)
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 7);
            else if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                contactsButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                addPlayerFromContacts();
            } else {
                contactsButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
            }
        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_player_management, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.action_player_management);
        container.removeAllViews();

        activity = getActivity();

        final Button showPlayerStatsButton = (Button) rootView.findViewById(R.id.showPlayerStatsButton);
        showPlayerStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlayerStatistics();
            }
        });

        final Button editPlayerButton = (Button) rootView.findViewById(R.id.editPlayerButton);
        editPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPlayer();
            }
        });

        final Button createNewPlayerButton = (Button) rootView.findViewById(R.id.createNewPlayerButton);
        createNewPlayerButton.setText(R.string.createNewPlayer);
        createNewPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewPlayer();
            }
        });

        contactsButton = (Button) rootView.findViewById(R.id.addPlayerContactsButton);
        contactsButton.setOnClickListener(contactButtonListener);


        pds = ((MainActivity) getActivity()).getPlayersDataSource();

        list = pds.getAllPlayers();

        myListView = (ListView) rootView.findViewById(R.id.current_players_list);

        listAdapter = new PlayerListAdapter(this.getActivity(), R.id.current_players_list, list);

        myListView.setAdapter(listAdapter);
        myListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String lastPlayerId = selectedPlayerId;
                selectedPlayerId = String.valueOf(((Player) listAdapter.getItem(position)).getId());

                if (lastPlayerId.equals(selectedPlayerId)) {
                    myListView.setItemChecked(-1, true);
                    selectedPlayerId = "-1";
                    refreshFragment();
                }

                if (myListView.getCheckedItemCount() > 0) {
                    createNewPlayerButton.setText(activity.getString(R.string.deletePlayer));
                    createNewPlayerButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_red));
                    createNewPlayerButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deletePlayer(myListView);
                        }
                    });

                    ((MainActivity) activity).setPlayerForEditing((Player) listAdapter.getItem(position));

                    contactsButton.setVisibility(View.GONE);
                    editPlayerButton.setVisibility(View.VISIBLE);
                    showPlayerStatsButton.setVisibility(View.VISIBLE);

                } else {
                    createNewPlayerButton.setText(activity.getString(R.string.createNewPlayer));
                    createNewPlayerButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                    createNewPlayerButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createNewPlayer();
                        }
                    });

                    editPlayerButton.setVisibility(View.GONE);
                    showPlayerStatsButton.setVisibility(View.GONE);
                }

            }
        });

        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 7: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addPlayerFromContacts();
                } else {
                    contactsButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
                }

                return;
            }
        }
    }

    private void refreshFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity) {
            a = (Activity) context;
        }

    }

    private void createNewPlayer() {
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new CreateNewPlayerFragment());
        fragmentTransaction.addToBackStack(activity.getString(R.string.createNewPlayerFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        myListView.setItemChecked(-1, true);
        selectedPlayerId = "-1";

        fragmentTransaction.commit();
    }

    private void editPlayer() {
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new EditPlayerFragment());
        fragmentTransaction.addToBackStack(activity.getString(R.string.editPlayerFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        myListView.setItemChecked(-1, true);
        selectedPlayerId = "-1";

        fragmentTransaction.commit();
    }

    private void showPlayerStatistics() {
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new PlayerStatisticsFragment());
        fragmentTransaction.addToBackStack(activity.getString(R.string.playerStatisticsFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        myListView.setItemChecked(-1, true);
        selectedPlayerId = "-1";

        fragmentTransaction.commit();
    }


    private void addPlayerFromContacts() {
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new ContactListFragment());
        fragmentTransaction.addToBackStack(activity.getString(R.string.contactListFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();

    }

    private void deletePlayer(final ListView lv) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.deletePlayer))
                .setMessage(R.string.deletePlayerQuestion)
                .setIcon(android.R.drawable.ic_menu_help)
                .setPositiveButton(activity.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SparseBooleanArray checked = lv.getCheckedItemPositions();
                        int size = checked.size();

                        long deletedPlayerId = -1;

                        for (int i = 0; i < size; i++) {
                            int key = checked.keyAt(i);
                            boolean value = checked.get(key);
                            if (value) {
                                deletedPlayerId = list.get(key).getId();

                                GamesDataSource gds = new GamesDataSource(activity);
                                gds.open();
                                gds.deleteGamesWithPlayer(deletedPlayerId);
                                gds.close();

                                pds.deletePlayer(list.get(key));
                                lv.setItemChecked(key, false);
                            }
                        }
                        list = pds.getAllPlayers();

                        refreshFragment();
                    }

                })
                .setNegativeButton(activity.getString(R.string.no), null)
                .show();

    }

}
