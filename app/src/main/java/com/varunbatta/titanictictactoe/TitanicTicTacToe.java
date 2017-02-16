package com.varunbatta.titanictictactoe;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

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
