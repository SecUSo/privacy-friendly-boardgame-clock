package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
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
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.view.GamesListAdapter;

import java.util.List;

public class LoadGameFragment extends ListFragment {

    Button loadButton, deleteButton;
    String selectedGameId = "-1";
    Activity activity;
    GamesDataSource gds;
    List<Game> gamesList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity();
        gds = ((MainActivity) activity).getGamesDataSource();

        final View rootView = inflater.inflate(R.layout.fragment_load_game, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getString(R.string.loadGame));
        container.removeAllViews();

        loadButton = (Button) rootView.findViewById(R.id.loadGameButton);
        deleteButton = (Button) rootView.findViewById(R.id.deleteGameButton);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ListView myListView = getListView();
        gamesList = gds.getSavedGames();

        final GamesListAdapter listAdapter = new GamesListAdapter(this.getActivity(), this.getId(), gamesList);

        myListView.setAdapter(listAdapter);
        myListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

                String lastGameId = selectedGameId;
                selectedGameId = String.valueOf(((Game) adapter.getItemAtPosition(position)).getId());

                if (lastGameId.equals(selectedGameId)) {
                    myListView.setItemChecked(-1, true);
                    selectedGameId = "-1";
                    refreshFragment();
                }

                if (getListView().getCheckedItemCount() > 0) {
                    deleteButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_red));

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((MainActivity) getActivity()).setGame(gds.getGameWithId(selectedGameId));
                            deleteGame(getListView());
                        }
                    });

                    loadButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));

                    loadButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((MainActivity) getActivity()).setGame(gds.getGameWithId(selectedGameId));
                            startNewGame();
                        }
                    });
                } else {
                    deleteButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));

                    deleteButton.setOnClickListener(null);

                    loadButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));

                    loadButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.error)
                                    .setMessage(R.string.pleaseChooseAGame)
                                    .setPositiveButton(R.string.ok, null)
                                    .setIcon(android.R.drawable.ic_menu_info_details)

                                    .show();
                        }
                    });
                }
            }
        });

        deleteButton.setClickable(false);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.error)
                        .setMessage(R.string.pleaseChooseAGame)
                        .setPositiveButton(R.string.ok, null)
                        .setIcon(android.R.drawable.ic_menu_info_details)

                        .show();
            }
        });

    }

    public void startNewGame() {

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new GameFragment());
        fragmentTransaction.addToBackStack(getString(R.string.gameFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);


        fragmentTransaction.commit();

    }

    private void refreshFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    public void deleteGame(final ListView lv) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.deleteGame)
                .setMessage(R.string.deleteGameQuestion)
                .setIcon(android.R.drawable.ic_menu_help)

                .setPositiveButton(activity.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SparseBooleanArray checked = lv.getCheckedItemPositions();
                        int size = checked.size();

                        for (int i = 0; i < size; i++) {
                            int key = checked.keyAt(i);
                            boolean value = checked.get(key);
                            if (value) {
                                gds.deleteGame(gamesList.get(key));
                                lv.setItemChecked(key, false);
                            }
                        }
                        gamesList = gds.getSavedGames();
                        refreshFragment();
                    }

                })
                .setNegativeButton(activity.getString(R.string.no), null)
                .show();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity) {
            a = (Activity) context;
        }

    }
}