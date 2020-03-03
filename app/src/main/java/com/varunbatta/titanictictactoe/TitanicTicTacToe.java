package com.varunbatta.titanictictactoe;

import android.content.Context;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class TitanicTicTacToe extends MultiDexApplication{
	
	public static Context context;
	
	public void onCreate(){
        super.onCreate();
        this.context = getApplicationContext();
        MultiDex.install(this);
    }

    public static Context getAppContext() {
        return TitanicTicTacToe.context;
    }
}
