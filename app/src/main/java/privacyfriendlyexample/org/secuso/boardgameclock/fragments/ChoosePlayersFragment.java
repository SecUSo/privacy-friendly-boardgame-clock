package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.db.GamesDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.db.PlayersDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;
import privacyfriendlyexample.org.secuso.boardgameclock.view.PFAListAdapter;


public class ChoosePlayersFragment extends Fragment {

    Activity activity;
    ListView myListView;
    List<Player> list;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = this.getActivity();

        View rootView = inflater.inflate(R.layout.fragment_choose_players, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.action_new_game);
        container.removeAllViews();

        PlayersDataSource playersDataSource = new PlayersDataSource(this.getActivity());

        playersDataSource.open();
        list = playersDataSource.getAllPlayers();
        playersDataSource.close();

        myListView = (ListView) rootView.findViewById(R.id.choose_players_list);
        PFAListAdapter listAdapter = new PFAListAdapter(this.getActivity(), R.id.choose_players_list, list);

        myListView.setAdapter(listAdapter);
        myListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        /*myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            // here i overide the onitemclick method in onitemclick listener
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SparseBooleanArray checkedItems = myListView.getCheckedItemPositions();


                // remove every color
                for (int i = 0; i < myListView.getAdapter().getCount(); i++) {
                //    myListView.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                }

                // get key values of SparseBooleanArray if checked=true
                Vector<Integer> keys = new Vector<>();
                for (int i = 0; i < checkedItems.size(); i++){
                    if(checkedItems.valueAt(i))
                        keys.add(checkedItems.keyAt(i));
                }

                // set color of checked elements (contained in keys vector)
                for (int i : keys) {
                    parent.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.darkblue));
                }

                System.err.println(checkedItems);
            }
        });*/

        Button b = (Button) rootView.findViewById(R.id.startNewGameButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGame();
            }
        });

        return rootView;
    }

    private void createNewGame() {
        Game game = ((MainActivity) activity).getGame();

        List<Player> players = new ArrayList<>();

        SparseBooleanArray checked = myListView.getCheckedItemPositions();
        int size = checked.size();
        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value) {
                players.add(list.get(key));
            }
        }

        HashMap<Long, Long> player_round_times = new HashMap<>();
        for (Player p : players){
            player_round_times.put(p.getId(), Long.valueOf(game.getRound_time()));
        }

        if (players.size() < 2) new AlertDialog.Builder(activity)
                .setTitle("Error")
                .setMessage("Please choose at least two players to continue.")
                .setPositiveButton("OK", null)
                .show();
        else {

            GamesDataSource gds = new GamesDataSource(activity);
            gds.open();
            gds.createGame(players, player_round_times, game.getName(), game.getRound_time(), game.getGame_time(), game.getReset_round_time(), game.getGame_mode(), game.getRound_time_delta());

            gds.getAllGames();
            gds.close();

            game.setPlayers(players);
            game.setPlayer_round_times(player_round_times);
            ((MainActivity) activity).setGame(game);

            startNewGame();
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    public void startNewGame() {

            final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new GameFragment());
            fragmentTransaction.addToBackStack("GameFragment");
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.commit();

    }


}
