package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
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

public class LoadGameFragment extends ListFragment {

    Button loadButton, deleteButton;
    String selectedGameId;
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

        final View rootView = inflater.inflate(R.layout.fragment_load_game, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getString(R.string.loadGame));
        container.removeAllViews();

        loadButton = (Button) rootView.findViewById(R.id.loadGameButton);
        deleteButton = (Button) rootView.findViewById(R.id.deleteGameButton);
        deleteButton.setBackgroundColor(Color.RED);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gds = new GamesDataSource(activity);
        gds.open();
        final ListView myListView = getListView();
        gamesList = gds.getSavedGames();
        gds.close();

        final GamesListAdapter listAdapter = new GamesListAdapter(this.getActivity(), this.getId(), gamesList);

        myListView.setAdapter(listAdapter);
        myListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                selectedGameId = String.valueOf(((Game) adapter.getItemAtPosition(position)).getId());

                loadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gds.open();
                        ((MainActivity) getActivity()).setGame(gds.getGameWithId(selectedGameId));
                        gds.close();

                        startNewGame();
                    }
                });

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gds.open();
                        ((MainActivity) getActivity()).setGame(gds.getGameWithId(selectedGameId));
                        gds.close();

                        deleteGame(getListView());
                    }
                });

                if (getListView().getCheckedItemCount() > 0)
                    deleteButton.setVisibility(View.VISIBLE);
                else
                    deleteButton.setVisibility(View.GONE);
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.error)
                        .setMessage(R.string.pleaseChooseAGame)
                        .setPositiveButton(R.string.ok, null)
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

    public void deleteGame(final ListView lv){
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.deleteGame)
                    .setMessage(R.string.deleteGameQuestion)
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
                            gamesList = gds.getSavedGames();
                            gds.close();
                            refreshFragment();
                        }

                    })
                    .setNegativeButton(activity.getString(R.string.no), null)
                    .show();

    }
}