package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.db.GamesDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.db.PlayersDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;
import privacyfriendlyexample.org.secuso.boardgameclock.view.PlayerListAdapter;


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


        final Button startNewGameButton = (Button) rootView.findViewById(R.id.startNewGameButton);
        startNewGameButton .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGame();
            }
        });

        final Button b = (Button) rootView.findViewById(R.id.newGamePlayerManagementButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGamePlayerManagementButton();
            }
        });

        myListView = (ListView) rootView.findViewById(R.id.choose_players_list);
        PlayerListAdapter listAdapter = new PlayerListAdapter(this.getActivity(), R.id.choose_players_list, list);

        myListView.setAdapter(listAdapter);
        myListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                if (myListView.getCheckedItemCount() >= 2)
                    startNewGameButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                else
                    startNewGameButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
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
        for (Player p : players) {
            player_round_times.put(p.getId(), Long.valueOf(game.getRound_time()));
        }

        HashMap<Long, Long> players_rounds = new HashMap<>();
        for (Player p : players) {
            players_rounds.put(p.getId(), Long.valueOf(1));
        }

        long dateMs = System.currentTimeMillis();

        if (players.size() < 2) new AlertDialog.Builder(activity)
                .setTitle(R.string.error)
                .setMessage(R.string.errorAtLeastTwoPlayers)
                .setIcon(android.R.drawable.ic_menu_info_details)
                .setPositiveButton(R.string.ok, null)
                .show();
        else {
            GamesDataSource gds = new GamesDataSource(activity);
            gds.open();
            game = gds.createGame(dateMs, players, player_round_times, players_rounds, game.getName(), game.getRound_time(),
                    game.getGame_time(), game.getReset_round_time(), game.getGame_mode(), game.getRound_time_delta(), game.getGame_time(), 0, 0, game.getSaved(), 0);
            gds.getAllGames();
            gds.close();


            //start player index
            if (game.getGame_mode() == 0) {
                game.setStartPlayerIndex(0);
                game.setNextPlayerIndex(1);
            } else if (game.getGame_mode() == 1) {
                game.setStartPlayerIndex(0);
                game.setNextPlayerIndex(players.size() - 1);
            } else if (game.getGame_mode() == 2) {
                int randomPlayerIndex = new Random().nextInt(players.size());
                game.setStartPlayerIndex(randomPlayerIndex);

                randomPlayerIndex = new Random().nextInt(players.size());
                while (randomPlayerIndex == game.getStartPlayerIndex())
                    randomPlayerIndex = new Random().nextInt(players.size());

                game.setNextPlayerIndex(randomPlayerIndex);
            }

            game.setPlayers(players);
            game.setPlayer_round_times(player_round_times);
            game.setPlayer_rounds(players_rounds);
            ((MainActivity) activity).setGame(game);

            startNewGame();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity) {
            a = (Activity) context;
        }

    }

    private void clearListSelections(){
        myListView.clearChoices();

        SparseBooleanArray checked = myListView.getCheckedItemPositions();
        int size = checked.size();
        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value) {
                myListView.setItemChecked(key, false);
            }
        }
    }

    public void startNewGame() {

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new GameFragment());
        fragmentTransaction.addToBackStack(getString(R.string.gameFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        myListView.setItemChecked(-1, true);

        fragmentTransaction.commit();

    }

    public void newGamePlayerManagementButton() {
        clearListSelections();

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new PlayerManagementFragment());
        fragmentTransaction.addToBackStack(getString(R.string.playerManagementFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();
    }


}
