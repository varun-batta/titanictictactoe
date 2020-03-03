package com.varunbatta.titanictictactoe;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * FacebookFriend - A view to showcase your Facebook friends as possible opponents
 */
public class FacebookFriend extends LinearLayout {

    public FacebookFriend(Player player, Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.facebook_friend, this, true);
        init(player);
    }

    public FacebookFriend(Player player, Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.facebook_friend, this, true);
        init(player);
    }

    public FacebookFriend(Player player, Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.facebook_friend, this, true);
        init(player);
    }

    private void init(Player player) {
        ProfilePictureView profilePictureView = findViewById(R.id.profile_picture);
        profilePictureView.setProfileId("" + player.playerFBID);
        profilePictureView.setCropped(true);

        TextView friendName = findViewById(R.id.friend_name);
        friendName.setText(player.playerName);
    }
}
