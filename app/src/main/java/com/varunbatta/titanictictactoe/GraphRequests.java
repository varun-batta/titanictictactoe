package com.varunbatta.titanictictactoe;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by varun on 2/25/2018.
 */

public class GraphRequests extends AsyncTask<GameRequest, Void, Object> {

    // TODO: Use a better networking tool like OKHTTP to do this
    AccessToken accessToken = AccessToken.getCurrentAccessToken();
    GraphRequest request;
    String path;
    @Override
    protected Object doInBackground(GameRequest[] objects) {
        path = objects[0].graphPath;
        if (objects[0].httpMethod == HttpMethod.DELETE) {
            request = GraphRequest.newDeleteObjectRequest(accessToken, objects[0].graphPath.substring(1), new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    Log.d("DeleteRequestResponse", response.toString());
                }
            });
            request.executeAsync();
            return null;
        }
        if (path == "/me") {
            final Player me = new Player();
            request = GraphRequest.newMeRequest(accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                me.playerName = object.getString("name");
                                me.playerFBID = Long.parseLong(object.getString("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            for (String parameterKey : objects[0].parameters.keySet()) {
                parameters.putString(parameterKey, objects[0].parameters.get(parameterKey));
            }
            request.setParameters(parameters);
            request.executeAndWait();
            return me;
        } else if(path.contains("/friends")) {
            final ArrayList<Player> friends = new ArrayList<>();
            request = GraphRequest.newMyFriendsRequest(accessToken,
                    new GraphRequest.GraphJSONArrayCallback() {
                        @Override
                        public void onCompleted(JSONArray objects, GraphResponse response) {
                            try {
                                for (int i = 0; i < objects.length(); i++) {
                                    JSONObject friendJSON = objects.getJSONObject(i);
                                    Player friend = new Player();
                                    friend.initWithPlayerData(friendJSON.getString("name"), friendJSON.getLong("id"), "");
                                    friends.add(friend);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            request.executeAndWait();
            return friends;
        } else if (path.contains("/?id=")) {
            final Player opponent = new Player();
            request = GraphRequest.newGraphPathRequest(accessToken, path, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    JSONObject responseJSON = response.getJSONObject();
                    try {
                        opponent.playerName = responseJSON.getString("name");
                        opponent.playerFBID = Long.parseLong(responseJSON.getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            Bundle parameters = new Bundle();
            for (String parameterKey : objects[0].parameters.keySet()) {
                parameters.putString(parameterKey, objects[0].parameters.get(parameterKey));
            }
            request.setParameters(parameters);
            request.executeAndWait();
            return opponent;
        } else if (path.contains("apprequests")) {
            final ArrayList<Long> activeGames = new ArrayList<Long>();
            request = GraphRequest.newGraphPathRequest(accessToken, path, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    JSONObject responseJSON = response.getJSONObject();
                    try {
                        JSONArray currentGames = responseJSON.getJSONArray("data");
                        for(int i = 0; i < currentGames.length(); i++) {
                            JSONObject currentGame = currentGames.getJSONObject(i);
                            String [][] gameData = parseData(currentGame.getString("data"));
                            if (gameData !=  null) {
                                long gameRequestID = Long.parseLong(currentGame.getString("id").split("_")[0]);
                                activeGames.add(gameRequestID);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            Bundle parameters = new Bundle();
            for (String parameterKey : objects[0].parameters.keySet()) {
                parameters.putString(parameterKey, objects[0].parameters.get(parameterKey));
            }
            request.setParameters(parameters);
            request.executeAndWait();
            return activeGames;
        } else {
            final com.varunbatta.titanictictactoe.Game game = new com.varunbatta.titanictictactoe.Game();
            request = GraphRequest.newGraphPathRequest(accessToken, path, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    JSONObject responseJSON = response.getJSONObject();
                    try {
                        if (responseJSON.getString("message").equals("New Match") || responseJSON.getString("message").contains("forfeit")) {
                            GameRequest deleteRequest = new GameRequest();
                            deleteRequest.createNewGameRequest(path, null, HttpMethod.DELETE);
                            new GraphRequests().execute(deleteRequest);
                        } else {
                            game.data = parseData(responseJSON.getString("data"));
                            game.lastMoveRow = Integer.parseInt(game.data[9][0]);
                            game.lastMoveColumn = Integer.parseInt(game.data[9][1]);
                            game.lastMove = game.data[9][2];
                            game.level = Integer.parseInt(game.data[9][3]);
                            game.requestID = responseJSON.getLong("id");

                            Player opponent = new Player();
                            JSONObject opponentData = responseJSON.getJSONObject("from");
                            opponent.playerName = opponentData.getString("name");
                            opponent.playerFBID = opponentData.getLong("id");

                            Player me = new Player();
                            JSONObject meData = responseJSON.getJSONObject("to");
                            me.playerName = meData.getString("name");
                            me.playerFBID = meData.getLong("id");

                            if (game.lastMove.equals("X")) {
                                game.player1 = opponent;
                                game.player2 = me;
                            } else {
                                game.player1 = me;
                                game.player2 = opponent;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            request.executeAndWait();
            return game;
        }
    }

    private String[][] parseData(String data) {
        String [][] gameData = new String[10][9];
        int row = 0;
        int column = 0;
        for(int i = 0; i < data.length(); i++) {
            char gameDataChar = data.charAt(i);
            if (gameDataChar == ';') {
                row++;
                column = 0;
            } else if (gameDataChar == ',') {
                column += 1;
            } else {
                gameData[row][column] = "" + gameDataChar;
            }
        }
        return gameData;
    }

    @Override
    protected void onPostExecute(Object player) {
        super.onPostExecute(player);
        if (player != null) {
            if (path == "/me") {
                EditText yourNameToFill = PlayerSelector.viewGroup.findViewById(R.id.yourNameToFill);
                yourNameToFill.setText(((Player) player).playerName);
            }
        }
    }
}
