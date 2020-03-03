package com.varunbatta.titanictictactoe;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FacebookGameSelected extends Fragment implements FacebookAPIListener {

    CallbackManager loginCallbackManager;
    public static View layout;

    Player player1 = new Player();
    ArrayList<Player> player1Friends = new ArrayList<>();

    FacebookAPIManager facebookAPIManager;

    LinearLayout friendSelector;
    // TODO: Use Recycler view to draw this instead

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initiate facebookAPIManager
        facebookAPIManager = FacebookAPIManager.getInstance(this.getContext(), this);

        // Inflate the layout for this fragment
        layout =  inflater.inflate(R.layout.facebook_game_selected, container, false);
        friendSelector = layout.findViewById(R.id.facebook_friend_selector_layout);

        // Check to make sure FacebookSdk is initialized
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(getContext(), 2809);
        }

        // Handle login callback as necessary
        // TODO: See if this is even necessary since we aren't actually doing anything with it
        loginCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(loginCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("LoginSuccess", loginResult.toString());
                    }

                    @Override
                    public void onCancel() {
                        Log.d("LoginCancel", "FB Login Cancelled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d("LoginError", exception.toString());
                    }
                });

        // Login as necessary
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"));

        // If logged in, populate facebook friends to play against
        // TODO: What about the else?
        boolean loggedIn = AccessToken.getCurrentAccessToken() != null;
        if (loggedIn) {
            // Initiate the playerParameters that will be required for all requests
            Map<String, String> playerParameters = new HashMap<>();
            playerParameters.put("fields", "id,name");
            playerParameters.put("access_token", AccessToken.getCurrentAccessToken().getToken());

            // Get Player 1 Information
            GameRequest player1Request = new GameRequest();
            player1Request.createNewGameRequest("/me", playerParameters, null);
            facebookAPIManager.placeMeRequest(player1Request);

            // Get Player 1 Friends
            GameRequest player1FriendsRequest = new GameRequest();
            player1FriendsRequest.createNewGameRequest("/me/friends", playerParameters, null);
            facebookAPIManager.placeMyFriendsRequest(player1FriendsRequest);
        }

        return layout;
    }

    // TODO: See if this is required
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccessMeRequest() {
        player1 = facebookAPIManager.me;
    }

    @Override
    public void onSuccessMyFriendsRequest() {
        player1Friends = facebookAPIManager.myFriends;

        // Show all Facebook Friends as possible opponents
        for (final Player player2 : player1Friends) {
            FacebookFriend friendView = new FacebookFriend(player2, getContext());
            friendSelector.addView(friendView);
            friendView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent board = new Intent(getContext(), Board.class);
                    // TODO: Cleanup all these params that need to be passed in and see if it can be simplified
                    board.putExtra("Level", PlayerSelector.level);
                    board.putExtra("Player 1 FBID", player1.playerFBID);
                    board.putExtra("Player 1 Name", player1.playerName);
                    board.putExtra("Player 2 FBID", player2.playerFBID);
                    board.putExtra("Player 2 Name", player2.playerName);
                    board.putExtra("Caller", "MainMenu");
                    board.putExtra("My Turn", true);
                    board.putExtra("Finished", false);
                    board.putExtra("Multiplayer", true);
                    startActivity(board);
                }
            });
        }

        friendSelector.invalidate();
    }
}
