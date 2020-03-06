package com.varunbatta.titanictictactoe;

/**
 * Created by varun on 2/25/2018.
 */

public class Player {
    String playerName;
    long playerFBID;
    String turn;

    public Player() {
        this.playerName = "";
        this.playerFBID = -1;
        this.turn = "";
    }

    public void initForSavedGameName(String playerName, String turn) {
        this.playerName = playerName;
        this.turn = turn;
    }

    public void initWithPlayerData(String name, long id, String turn) {
        this.playerName = name;
        this.playerFBID = id;
        this.turn = turn;
    }
}
