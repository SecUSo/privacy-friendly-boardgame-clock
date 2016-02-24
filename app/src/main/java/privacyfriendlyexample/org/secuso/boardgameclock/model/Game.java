package privacyfriendlyexample.org.secuso.boardgameclock.model;

import java.util.List;

/**
 * Created by Marco on 24.02.2016.
 */
public class Game {

    private long id;
    private List<Player> players;
    private String name;
    private int round_time;
    private int reset_round_time; //0 = false, 1 = true
    private int game_mode; //0 = clockwise, 1= counter_clockwise, 2=random
    private int round_time_delta = -1;

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

    public int getRound_time() {
        return round_time;
    }

    public void setRound_time(int round_time) {
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

    public int getRound_time_delta() {
        return round_time_delta;
    }

    public void setRound_time_delta(int round_time_delta) {
        this.round_time_delta = round_time_delta;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
