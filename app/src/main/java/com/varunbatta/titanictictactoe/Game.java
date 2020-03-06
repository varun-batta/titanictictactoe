package com.varunbatta.titanictictactoe;

/**
 * Game - The model containing the game data including data to recreate the game
 */

public class Game {

    public Player player1;
    public Player player2;
    public String [][] data;
    public String lastMove;
    public int lastMoveRow;
    public int lastMoveColumn;
    public int level;
    public long requestID;

    public Game() {
        player1 = new Player();
        player2 = new Player();
        data = new String[10][9];
        lastMove = "";
        lastMoveRow = -1;
        lastMoveColumn = -1;
        level = 0;
        requestID = 0;
    }

    // TODO: Deprecate if necessary
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
