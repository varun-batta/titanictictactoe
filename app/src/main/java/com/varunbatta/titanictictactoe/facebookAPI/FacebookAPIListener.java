package com.varunbatta.titanictictactoe.facebookAPI;

import android.util.LongSparseArray;

import com.varunbatta.titanictictactoe.Game;

public interface FacebookAPIListener {
    void onSuccessMeRequest();
    void onSuccessMyFriendsRequest();
    void onSuccessOpponentRequest(LongSparseArray<Game> availableGames);
}
