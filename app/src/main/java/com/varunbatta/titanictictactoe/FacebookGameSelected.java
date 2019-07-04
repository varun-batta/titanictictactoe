package com.varunbatta.titanictictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.widget.GameRequestDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FacebookGameSelected extends Fragment {

    CallbackManager playerSelectCallbackManager;
    CallbackManager loginCallbackManager;
    GameRequestDialog requestDialog;
    public static View layout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout =  inflater.inflate(R.layout.facebook_game_selected, container, false);
        LinearLayout friendSelector = layout.findViewById(R.id.facebook_friend_selector_layout);

        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(getContext(), 2809);
        }

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

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"));

//        playerSelectCallbackManager = CallbackManager.Factory.create();
//        requestDialog = new GameRequestDialog(this);
//        requestDialog.registerCallback(playerSelectCallbackManager,
//                new FacebookCallback<GameRequestDialog.Result>() {
//                    public void onSuccess(GameRequestDialog.Result result) {
//                        if (result.getRequestRecipients().size() == 0) {
//                            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
//                            alert.setTitle("No friends selected!");
//                            alert.setMessage("You haven't selected any friends to play against. Please select just ONE friend.");
//                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                            alert.create().show();
//                        } else if (result.getRequestRecipients().size() > 1) {
//                            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
//                            alert.setTitle("Too many friends selected!");
//                            alert.setMessage("You have selected too many friends to play against. Please select just ONE friend.");
//                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                            alert.create().show();
//                        } else {
//                            Player player1 = null;
//                            Player player2 = null;
//                            try {
//
//                                Map<String, String> playerParameters = new HashMap<String, String>();
//                                playerParameters.put("fields", "id,name");
//                                GameRequest player1Request = new GameRequest();
//                                player1Request.createNewGameRequest("/me", playerParameters, null);
//                                player1 = (Player) new GraphRequests().execute(player1Request).get();
//
//                                GameRequest player2Request = new GameRequest();
//                                player2Request.createNewGameRequest("/?id=" + result.getRequestRecipients().get(0), playerParameters, null);
//                                player2 = (Player) new GraphRequests().execute(player2Request).get();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            } catch (ExecutionException e) {
//                                e.printStackTrace();
//                            }
//
//                            Board.player1Player = player1;
//                            Board.player2Player = player2;
//                            Intent board = new Intent(getContext(), Board.class);
//                            board.putExtra("Level", 1);
//                            board.putExtra("Player 1 Name", player1.playerName);
//                            board.putExtra("Player 2 Name", player2.playerName);
//                            board.putExtra("Pending Player", player2.playerFBID);
//                            board.putExtra("Current Player", player1.playerFBID);
//                            board.putExtra("Caller", "MainMenu");
//                            board.putExtra("My Turn", true);
//                            board.putExtra("Finished", false);
//                            board.putExtra("Multiplayer", true);
//                            startActivity(board);
//                        }
//                    }
//                    public void onCancel() {
//                        Log.d("Cancelled", "Facebook Invite Cancelled");
//                    }
//                    public void onError(FacebookException error) {
//                        Log.d("Error", error.toString());
//                    }
//                }
//        );

        boolean loggedIn = AccessToken.getCurrentAccessToken() != null;
        if (loggedIn) {
            Player player1 = null;
            try {

                Map<String, String> playerParameters = new HashMap<String, String>();
                playerParameters.put("fields", "id,name");
                GameRequest player1Request = new GameRequest();
                player1Request.createNewGameRequest("/me", playerParameters, null);
                player1 = (Player) new GraphRequests().execute(player1Request).get();

                GameRequest player1FriendsRequest = new GameRequest();
                player1FriendsRequest.createNewGameRequest("/" + player1.playerFBID + "/friends", null, null);
                ArrayList<Long> friendsFBIDs = (ArrayList<Long>) new GraphRequests().execute(player1FriendsRequest).get();

                for (long friendFBID : friendsFBIDs) {
                    GameRequest player2Request = new GameRequest();
                    player2Request.createNewGameRequest("/?id=" + friendFBID, playerParameters, null);
                    final Player player2 = (Player) new GraphRequests().execute(player2Request).get();

                    FacebookFriend friendView = new FacebookFriend(player2, getContext());
                    friendSelector.addView(friendView);
                    final Player finalPlayer = player1;
                    friendView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            Board.player1Player = finalPlayer;
//                            Board.player2Player = player2;
                            Intent board = new Intent(getContext(), Board.class);
                            board.putExtra("Level", PlayerSelector.level);
                            board.putExtra("Player 1 Name", finalPlayer.playerName);
                            board.putExtra("Player 2 Name", player2.playerName);
                            board.putExtra("Player 2 FBID", player2.playerFBID);
                            board.putExtra("Player 1 FBID", finalPlayer.playerFBID);
                            board.putExtra("Caller", "MainMenu");
                            board.putExtra("My Turn", true);
                            board.putExtra("Finished", false);
                            board.putExtra("Multiplayer", true);
                            startActivity(board);
//                            PlayerSelector playerSelector = (PlayerSelector) getActivity();
//                            playerSelector.finish();
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
//            EditText facebookName = (EditText) layout.findViewById(R.id.theirFacebookNameToFill);
//            facebookName.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    GameRequestContent content = new GameRequestContent.Builder()
//                            .setActionType(GameRequestContent.ActionType.TURN)
//                            .setFilters(GameRequestContent.Filters.APP_USERS)
//                            .setMessage("Come play this level with me")
//                            .build();
//                    requestDialog.show(content);
//                }
//            });
        }

        return layout;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        playerSelectCallbackManager.onActivityResult(requestCode, resultCode, data);
        loginCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
