package com.varunbatta.titanictictactoe.facebookAPI;

import android.content.Context;
import android.util.Log;
import android.util.LongSparseArray;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.varunbatta.titanictictactoe.Game;
import com.varunbatta.titanictictactoe.GameRequest;
import com.varunbatta.titanictictactoe.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FacebookAPIManager {
    private static FacebookAPIManager instance;
    private static Context context;
    private RequestQueue requestQueue;

    private String baseURL = "https://graph.facebook.com";

    public Player me = new Player();
    public ArrayList<Player> myFriends = new ArrayList<>();
    public Game game = new Game();
    public LongSparseArray<Game> availableGames = new LongSparseArray<>();

    public int numOpponentRequestsMade = 0;

    private FacebookAPIManager(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized FacebookAPIManager getInstance(Context context) {
        if (instance == null) {
            instance = new FacebookAPIManager(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

    // TODO: Might want to make listeners not passed in but one global listener that each class has access to
    public void placeMeRequest(GameRequest gameRequest, final FacebookAPIListener listener) {
        // Build URL String with associated params
        // TODO: See if GameRequest still needs to exist or can it be simplified, in which case change this
        StringBuilder requestURLBuilder = new StringBuilder(baseURL + gameRequest.graphPath);
        for (int i = 0; i < gameRequest.parameters.size(); i++) {
            requestURLBuilder.append(i == 0 ? "?" : "&");
            String curKey = (String) gameRequest.parameters.keySet().toArray()[i];
            requestURLBuilder.append(curKey + "=" + gameRequest.parameters.get(curKey));
        }
        String requestURL = requestURLBuilder.toString();

        // Create request for the above URL
        JsonObjectRequest request = new JsonObjectRequest(
                requestURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    me.initWithPlayerData(response.getString("name"), response.getLong("id"), "");
                    listener.onSuccessMeRequest();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MeRequestError", error.toString());
            }
        });

        addToRequestQueue(request);
    }

    public void placeMyFriendsRequest(GameRequest gameRequest, final FacebookAPIListener listener) {
        // Build URL String with associated params
        // TODO: See if GameRequest still needs to exist or can it be simplified, in which case change this
        StringBuilder requestURLBuilder = new StringBuilder(baseURL + gameRequest.graphPath);
        for (int i = 0; i < gameRequest.parameters.size(); i++) {
            requestURLBuilder.append(i == 0 ? "?" : "&");
            String curKey = (String) gameRequest.parameters.keySet().toArray()[i];
            requestURLBuilder.append(curKey + "=" + gameRequest.parameters.get(curKey));
        }
        String requestURL = requestURLBuilder.toString();

        // Create request for the above URL
        JsonObjectRequest request = new JsonObjectRequest(
                requestURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray friendsData = response.getJSONArray("data");
                    for (int i = 0; i < friendsData.length(); i++) {
                        // Read the JSONData of the friend object to create a friend player
                        JSONObject friendJSON = friendsData.getJSONObject(i);
                        Player friend = new Player();
                        friend.initWithPlayerData(friendJSON.getString("name"), friendJSON.getLong("id"), "");
                        myFriends.add(friend);
                    }
                    listener.onSuccessMyFriendsRequest();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MyFriendsRequestError", error.toString());
            }
        });

        addToRequestQueue(request);
    }

    public void placeOpponentRequests(final String [] requestIDs, final FacebookAPIListener listener) {
        // This is just a way to make sure all the requests are done
        // TODO: Might wanna change this to use something other than requestQueue
        for (final String requestID : requestIDs) {
            // Build URL String with associated params
            // TODO: Might wanna make this just one string with using string formatting
            StringBuilder requestURLBuilder = new StringBuilder(baseURL + "/" + requestID);
            requestURLBuilder.append("?fields=ids,action_type,application,created_time,date,from,messages,object,to");
            requestURLBuilder.append("&access_token=" + AccessToken.getCurrentAccessToken().getToken());
            String requestURL = requestURLBuilder.toString();

            // Create request for the above URL
            JsonObjectRequest request = new JsonObjectRequest(
                    requestURL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getString("message").equals("New Match") || response.getString("message").contains("forfeit")) {
                            placeDeleteGameRequest(requestID);
                        } else {
                            // TODO: Clean up this parsing task
                            game.data = parseData(response.getString("data"));
                            game.lastMoveRow = Integer.parseInt(game.data[9][0]);
                            game.lastMoveColumn = Integer.parseInt(game.data[9][1]);
                            game.lastMove = game.data[9][2];
                            game.level = Integer.parseInt(game.data[9][3]);
                            game.requestID = response.getLong("id");

                            Player opponent = new Player();
                            JSONObject opponentData = response.getJSONObject("from");
                            opponent.initWithPlayerData(opponentData.getString("name"), opponentData.getLong("id"), "");

                            Player me = new Player();
                            JSONObject meData = response.getJSONObject("to");
                            me.initWithPlayerData(meData.getString("name"), meData.getLong("id"), "");

                            // TODO: Clean up this logic
                            if (game.lastMove.equals("X")) {
                                game.player1 = opponent;
                                game.player2 = me;
                            } else {
                                game.player1 = me;
                                game.player2 = opponent;
                            }

                            // Appending the game object to the list of available games
                            if (!game.lastMove.equals("")) {
                                availableGames.append(Long.parseLong(requestID), game);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // Once all requests are finished, make the callback
                    numOpponentRequestsMade++;
                    if (numOpponentRequestsMade == requestIDs.length) {
                        listener.onSuccessOpponentRequest(availableGames);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("MyFriendsRequestError", error.toString());
                }
            });

            addToRequestQueue(request);
        }
    }

    public void placeDeleteGameRequest(String requestID) {
        // Build URL String with associated params
        // TODO: See if GameRequest still needs to exist or can it be simplified, in which case change this
        StringBuilder requestURLBuilder = new StringBuilder(baseURL + "/" + requestID);
        requestURLBuilder.append("?access_token=" + AccessToken.getCurrentAccessToken().getToken());
        String requestURL = requestURLBuilder.toString();

        // Create request for the above URL
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE, requestURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("DeleteRequestResponse", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MyFriendsRequestError", error.toString());
            }
        });

        addToRequestQueue(request);
    }

    // Helpers
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
}
