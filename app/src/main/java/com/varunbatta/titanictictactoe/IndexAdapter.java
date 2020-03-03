package com.varunbatta.titanictictactoe;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class IndexAdapter extends BaseAdapter{
	private Context context;
	private int screenWidth;
	
	public IndexAdapter(Context context, int screenWidth){
		this.context = context;
		this.screenWidth = screenWidth;
	}
	
	@Override
	public int getCount() {
		return 9;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv = new TextView(context);
		
		switch(position){
			case 0: tv.setText(R.string.tic);
					tv.setTextSize(40);
			break;
			case 1: tv.setText(R.string.tac);
					tv.setTextSize(40);
			break;
			case 2: tv.setText(R.string.toe);
					tv.setTextSize(40);
			break;
			case 4: tv.setText(R.string.guide);
			break;
		}
		
		tv.setWidth((screenWidth - 40)/3);
		tv.setHeight((screenWidth - 40)/3);
		tv.setGravity(Gravity.CENTER);
		tv.setTextColor(context.getResources().getColor(R.color.colorBlack));
		tv.setBackgroundColor(context.getResources().getColor(R.color.colorGreen));
		
		return tv;
	}


}
