package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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

import java.util.List;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.db.GamesDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.view.GamesListAdapter;

public class GameHistoryFragment extends ListFragment {

    Button loadButton, deleteButton;
    String selectedGameId = "-1";
    Activity activity;
    GamesDataSource gds;
    List<Game> gamesList;
    ListView myListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity();

        final View rootView = inflater.inflate(R.layout.fragment_game_history, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(activity.getString(R.string.gameHistory));
        container.removeAllViews();

        loadButton = (Button) rootView.findViewById(R.id.showResultsButton);
        deleteButton = (Button) rootView.findViewById(R.id.removeEntryButton);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gds = new GamesDataSource(activity);
        gds.open();
        myListView = getListView();
        gamesList = gds.getFinishedGames();
        gds.close();

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
                            gds.open();
                            ((MainActivity) getActivity()).setGame(gds.getGameWithId(selectedGameId));
                            gds.close();

                            removeEntry(getListView());
                        }
                    });

                    loadButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));

                    loadButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gds.open();
                            ((MainActivity) getActivity()).setGame(gds.getGameWithId(selectedGameId));
                            gds.close();

                            showResults();
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
                                    .setIcon(android.R.drawable.ic_menu_help)

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
                        .setIcon(android.R.drawable.ic_menu_help)

                        .show();
            }
        });

    }

    public void showResults() {

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new GameResultsFragment());
        fragmentTransaction.addToBackStack(getString(R.string.gameResultsFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();

    }

    private void refreshFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    public void removeEntry(final ListView lv) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.removeEntry)
                .setMessage(R.string.removeEntryQuestion)
                .setIcon(android.R.drawable.ic_menu_help)

                .setPositiveButton(activity.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SparseBooleanArray checked = lv.getCheckedItemPositions();
                        int size = checked.size();
                        gds.open();

                        for (int i = 0; i < size; i++) {
                            int key = checked.keyAt(i);
                            boolean value = checked.get(key);
                            if (value) {
                                gds.deleteGame(gamesList.get(key));
                                lv.setItemChecked(key, false);
                            }
                        }

                        gamesList = gds.getFinishedGames();
                        gds.close();
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