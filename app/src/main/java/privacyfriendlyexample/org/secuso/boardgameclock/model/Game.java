package privacyfriendlyexample.org.secuso.boardgameclock.model;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Marco on 24.02.2016.
 */
public class Game {

    private long id;
    private List<Player> players;
    private HashMap<Long, Long> player_round_times;
    private String name;
    private long round_time;
    private long game_time;
    private int reset_round_time; //0 = false, 1 = true
    private int game_mode; //0 = clockwise, 1= counter_clockwise, 2=random
    private long round_time_delta = -1;

    public Game() {
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRound_time() {
        return round_time;
    }

    public void setRound_time(long round_time) {
        this.round_time = round_time;
    }

    public int getReset_round_time() {
        return reset_round_time;
    }

    public void setReset_round_time(int reset_round_time) {
        this.reset_round_time = reset_round_time;
    }

    public int getGame_mode() {
        return game_mode;
    }

    public void setGame_mode(int game_mode) {
        this.game_mode = game_mode;
    }

    public long getRound_time_delta() {
        return round_time_delta;
    }

    public void setRound_time_delta(long round_time_delta) {
        this.round_time_delta = round_time_delta;
    }


    public HashMap<Long, Long> getPlayer_round_times() {
        return player_round_times;
    }

    public void setPlayer_round_times(HashMap<Long, Long> player_round_times) {
        this.player_round_times = player_round_times;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getGame_time() {
        return game_time;
    }

    public void setGame_time(long game_time) {
        this.game_time = game_time;
    }

}
