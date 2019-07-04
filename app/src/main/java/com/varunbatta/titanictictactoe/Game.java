package com.varunbatta.titanictictactoe;

import com.facebook.GraphResponse;

/**
 * Created by varun on 3/1/2018.
 */

public class Game {
    Player player1;
    Player player2;
    String [][] data;
    String lastMove;
    int lastMoveRow;
    int lastMoveColumn;
    int level;
    long requestID;

    Game() {
        player1 = new Player();
        player2 = new Player();
        data = new String[10][9];
        lastMove = "";
        lastMoveRow = -1;
        lastMoveColumn = -1;
        level = 0;
        requestID = 0;
    }

    public Game createNewGame(Player player1, Player player2, String[][] data, long requestID) {
        this.player1 = player1;
        this.player2 = player2;
        this.data = data;
        this.lastMove = data[9][2];
        this.lastMoveRow = Integer.parseInt(data[9][0]);
        this.lastMoveColumn = Integer.parseInt(data[9][1]);
        this.level = Integer.parseInt(data[9][3]);
        this.requestID = requestID;
        return this;
    }
}
