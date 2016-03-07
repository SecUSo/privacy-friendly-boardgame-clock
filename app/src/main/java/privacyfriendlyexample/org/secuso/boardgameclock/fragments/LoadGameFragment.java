package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.db.GamesDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;
import privacyfriendlyexample.org.secuso.boardgameclock.view.GamesListAdapter;
import privacyfriendlyexample.org.secuso.boardgameclock.view.PlayerListAdapter;

public class LoadGameFragment extends ListFragment {

    Button b;
    String selectedGameId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_load_game, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("Load Game");
        container.removeAllViews();

        b = (Button) rootView.findViewById(R.id.loadGameButton);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final GamesDataSource gds = new GamesDataSource(getActivity());
        gds.open();
        final ListView myListView = getListView();
        List<Game> gamesList = gds.getAllGames();
        gds.close();

        final GamesListAdapter listAdapter = new GamesListAdapter(this.getActivity(), this.getId(), gamesList);

        myListView.setAdapter(listAdapter);
        myListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                selectedGameId = String.valueOf(((Game) adapter.getItemAtPosition(position)).getId());
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gds.open();
                        ((MainActivity) getActivity()).setGame(gds.getGameWithId(selectedGameId));
                        gds.close();

                        startNewGame();
                    }
                });
            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                            .setTitle("Error")
                            .setMessage("Please choose a game from the list.")
                            .setPositiveButton("OK", null)
                            .show();
            }
        });
    }

    public void startNewGame() {

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new GameFragment());
        fragmentTransaction.addToBackStack("GameFragment");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();

    }
}