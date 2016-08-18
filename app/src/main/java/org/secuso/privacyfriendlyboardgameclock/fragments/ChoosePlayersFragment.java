package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.db.GamesDataSource;
import org.secuso.privacyfriendlyboardgameclock.db.PlayersDataSource;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;
import org.secuso.privacyfriendlyboardgameclock.view.SelectPlayerListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class ChoosePlayersFragment extends Fragment {

    private Activity activity;
    private ListView myListView;
    private List<Player> list;

    private List<Player> selectedPlayers;

    private GamesDataSource gds;
    private PlayersDataSource pds;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = this.getActivity();
        gds = ((MainActivity) activity).getGamesDataSource();
        pds = ((MainActivity) activity).getPlayersDataSource();

        View rootView = inflater.inflate(R.layout.fragment_choose_players, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.action_new_game);
        container.removeAllViews();

        list = pds.getAllPlayers();
        selectedPlayers = new ArrayList<>();


        final Button startNewGameButton = (Button) rootView.findViewById(R.id.startNewGameButton);
        startNewGameButton.setOnClickListener(new View.OnClickListener() {
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
        SelectPlayerListAdapter listAdapter = new SelectPlayerListAdapter(this.getActivity(), R.id.choose_players_list, list);

        myListView.setAdapter(listAdapter);
        myListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                TextView tv = (TextView) v.findViewById(R.id.textViewNumber);
                if (tv.getText() == "" && myListView.getCheckedItemCount() > 0) {
                    selectedPlayers.add((Player) adapter.getItemAtPosition(position));
                    tv.setText(selectedPlayers.indexOf((Player) adapter.getItemAtPosition(position)) + 1 + ".");
                } else {
                    int deletedNumber = selectedPlayers.indexOf(adapter.getItemAtPosition(position)) + 1;
                    selectedPlayers.remove(adapter.getItemAtPosition(position));
                    tv.setText("");

                    ListView playersList = (ListView) v.getParent();
                    SparseBooleanArray checked = playersList.getCheckedItemPositions();
                    int size = checked.size();
                    for (int i = 0; i < size; i++) {
                        int key = checked.keyAt(i);
                        boolean checkedValue = checked.get(key);
                        if (checkedValue) {
                            TextView number = (TextView) playersList.getChildAt(key).findViewById(R.id.textViewNumber);
                            String numberText = number.getText().toString();
                            int indexDot = numberText.indexOf(".");
                            if (indexDot != -1) {
                                int value = Integer.valueOf(numberText.substring(0, indexDot));
                                if (value > deletedNumber) {
                                    value--;
                                    number.setText(value + ".");
                                }
                            }
                        }
                    }
                }

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

        HashMap<Long, Long> player_round_times = new HashMap<>();
        for (Player p : selectedPlayers) {
            player_round_times.put(p.getId(), Long.valueOf(game.getRound_time()));
        }

        HashMap<Long, Long> players_rounds = new HashMap<>();
        for (Player p : selectedPlayers) {
            players_rounds.put(p.getId(), Long.valueOf(1));
        }

        long dateMs = System.currentTimeMillis();

        if (selectedPlayers.size() < 2) new AlertDialog.Builder(activity)
                .setTitle(R.string.error)
                .setMessage(R.string.errorAtLeastTwoPlayers)
                .setIcon(android.R.drawable.ic_menu_info_details)
                .setPositiveButton(R.string.ok, null)
                .show();
        else {
            game = gds.createGame(dateMs, selectedPlayers, player_round_times, players_rounds, game.getName(), game.getRound_time(),
                    game.getGame_time(), game.getReset_round_time(), game.getGame_mode(), game.getRound_time_delta(), game.getGame_time(), 0, 0, game.getSaved(), 0, game.getGame_time_infinite(),
                    game.getChess_mode(), 0);

            //start player index
            if (game.getGame_mode() == 0 || game.getGame_mode() == 3) {
                game.setStartPlayerIndex(0);
                game.setNextPlayerIndex(1);
            } else if (game.getGame_mode() == 1) {
                game.setStartPlayerIndex(0);
                game.setNextPlayerIndex(selectedPlayers.size() - 1);
            } else if (game.getGame_mode() == 2) {
                game.setStartPlayerIndex(0);

                int randomPlayerIndex = new Random().nextInt(selectedPlayers.size());
                while (randomPlayerIndex == game.getStartPlayerIndex())
                    randomPlayerIndex = new Random().nextInt(selectedPlayers.size());
                game.setNextPlayerIndex(randomPlayerIndex);
            }

            game.setPlayers(selectedPlayers);
            game.setPlayer_round_times(player_round_times);
            game.setPlayer_rounds(players_rounds);
            ((MainActivity) activity).setGame(game);

            // if game is finally created and game time is infinite, set game time to zero
            if (game.getGame_time_infinite() == 1) {
                game.setGame_time(0);
                game.setCurrentGameTime(0);
            }

            // store game number
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
            int gameNumber = settings.getInt("gameNumber", 1);
            gameNumber++;
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("gameNumber", gameNumber);
            editor.commit();

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

    private void clearListSelections() {
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
