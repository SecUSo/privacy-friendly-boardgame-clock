package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerListAdapter;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

import java.util.List;

/**
 * Created by Quang Anh Dang on 01.12.2017.
 * TODO DOC
 */

public class PlayerManagementMainFragment extends Fragment {
    private View view;
    private RecyclerView playersRecycleView;
    private List<Player> listPlayers;
    private PlayersDataSourceSingleton pds;
    private LinearLayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player_management, container, false);
        pds = PlayersDataSourceSingleton.getInstance(null);
        listPlayers = pds.getAllPlayers();
        listPlayers.add(new Player(112,2011457,"Player1", BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.privacyfriendlyappslogo)));
        listPlayers.add(new Player(113,2011453,"Player2", BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.privacyfriendlyappslogo)));
        listPlayers.add(new Player(114,2011454,"Player3", BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.privacyfriendlyappslogo)));
        listPlayers.add(new Player(115,2011456,"Player4", BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.privacyfriendlyappslogo)));

        layoutManager = new LinearLayoutManager(this.getActivity());

        // Lookup the recyclerview in fragment layout
        playersRecycleView = rootView.findViewById(R.id.player_list);
        playersRecycleView.setHasFixedSize(true);
        PlayerListAdapter playerListAdapter = new PlayerListAdapter(this.getActivity(),listPlayers,null);
        playersRecycleView.setAdapter(playerListAdapter);
        playersRecycleView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        /* TODO: Set onclicklistener
        listViewPlayers.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listViewPlayers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String tmp = "Clicked on Player";
                Toast toast = Toast.makeText(getActivity(),tmp,Toast.LENGTH_SHORT);
                toast.show();
            }
        });*/
        return rootView;
    }
}
