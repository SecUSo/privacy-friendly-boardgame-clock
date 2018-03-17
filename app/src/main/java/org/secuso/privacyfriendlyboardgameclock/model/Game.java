package org.secuso.privacyfriendlyboardgameclock.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 */
public class Game {

    private long id;
    private List<Player> players;
    private HashMap<Long, Long> player_round_times;
    private HashMap<Long, Long> player_rounds;
    private String name;
    private long round_time;
    private long game_time;
    private int reset_round_time; //0 = false, 1 = true
    private int game_time_infinite;
    private int game_mode; //0 = clockwise, 1= counter_clockwise, 2=random, 3  = chess
    private long round_time_delta = -1;
    private int saved = 0;
    private int isLastRound;
    private long date;
    private String dateString;
    private int chess_mode;
    private int finished = 0;
    private int startPlayerIndex;
    private int nextPlayerIndex;
    private long currentGameTime;

    public Game() {
    }

    public int getChess_mode() {
        return chess_mode;
    }

    public void setChess_mode(int chess_mode) {
        this.chess_mode = chess_mode;
    }

    public int getStartPlayerIndex() {
        return startPlayerIndex;
    }

    public void setStartPlayerIndex(int startPlayerIndex) {
        this.startPlayerIndex = startPlayerIndex;
    }

    public long getCurrentGameTime() {
        return currentGameTime;
    }

    public void setCurrentGameTime(long currentGameTime) {
        this.currentGameTime = currentGameTime;
    }

    public int getNextPlayerIndex() {
        return nextPlayerIndex;
    }

    public void setNextPlayerIndex(int nextPlayerIndex) {
        this.nextPlayerIndex = nextPlayerIndex;
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

    public HashMap<Long, Long> getPlayer_rounds() {
        return player_rounds;
    }

    public void setPlayer_rounds(HashMap<Long, Long> player_rounds) {
        this.player_rounds = player_rounds;
    }


    public int getSaved() {
        return saved;
    }

    public void setSaved(int saved) {
        this.saved = saved;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy kk:mm");
        dateString = formatter.format(new Date(date));
    }

    public String getDateString() {
        return dateString;
    }


    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }


    public int getGame_time_infinite() {
        return game_time_infinite;
    }

    public void setGame_time_infinite(int game_time_infinite) {
        this.game_time_infinite = game_time_infinite;
    }

    public int getIsLastRound() {
        return isLastRound;
    }

    public void setIsLastRound(int isLastRound) {
        this.isLastRound = isLastRound;
    }
}
