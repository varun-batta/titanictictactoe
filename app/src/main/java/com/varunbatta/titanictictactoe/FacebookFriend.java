package com.varunbatta.titanictictactoe;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * TODO: document your custom view class.
 */
public class FacebookFriend extends LinearLayout {

    public FacebookFriend(Player player, Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.facebook_friend, this, true);
        init(player, context);
    }

    public FacebookFriend(Player player, Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.facebook_friend, this, true);
        init(player, context);
    }

    public FacebookFriend(Player player, Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.facebook_friend, this, true);
        init(player, context);
    }

    private void init(Player player, Context context) {
        ProfilePictureView profilePictureView = (ProfilePictureView) findViewById(R.id.profile_picture);
        profilePictureView.setProfileId("" + player.playerFBID);
        profilePictureView.setCropped(true);

        TextView friendName = (TextView) findViewById(R.id.friend_name);
        friendName.setText(player.playerName);
    }
}
