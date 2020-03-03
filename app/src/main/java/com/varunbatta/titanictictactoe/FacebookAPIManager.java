package com.varunbatta.titanictictactoe;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FacebookAPIManager {
    private static FacebookAPIManager instance;
    private static Context context;
    private static FacebookAPIListener listener;
    private RequestQueue requestQueue;

    private String baseURL = "https://graph.facebook.com";

    public Player me = new Player();
    public ArrayList<Player> myFriends = new ArrayList<>();

    private FacebookAPIManager(Context context, FacebookAPIListener listener) {
        this.context = context;
        this.listener = listener;
        requestQueue = getRequestQueue();
    }

    public static synchronized FacebookAPIManager getInstance(Context context, FacebookAPIListener listener) {
        if (instance == null) {
            instance = new FacebookAPIManager(context, listener);
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

    public void placeMeRequest(GameRequest gameRequest) {
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
                    me.playerName = response.getString("name");
                    me.playerFBID = Long.parseLong(response.getString("id"));
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

    public void placeMyFriendsRequest(GameRequest gameRequest) {
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
}
