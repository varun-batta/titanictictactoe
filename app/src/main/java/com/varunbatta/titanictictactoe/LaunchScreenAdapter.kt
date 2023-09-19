package com.varunbatta.titanictictactoe

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

internal class LaunchScreenAdapter(
    private val context: Context,
    private val screenWidth: Int,
) : BaseAdapter() {
    override fun getCount(): Int {
        return 9
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val tv : TextView = TextView(context)

        when(position) {
            0 -> {
                tv.setText(R.string.tic)
                tv.textSize = 40F
            }
            1 -> {
                tv.setText(R.string.tac)
                tv.textSize = 40F
            }
            2 -> {
                tv.setText(R.string.toe)
                tv.textSize = 40F
            }
            4 -> {
                tv.setText(R.string.guide)
            }
        }

        tv.width = (screenWidth - 40)/3
        tv.height = (screenWidth - 40)/3
        tv.gravity = Gravity.CENTER
        tv.setTextColor(context.resources.getColor(R.color.colorBlack))
        tv.setBackgroundColor(context.resources.getColor(R.color.colorGreen))

        return tv
    }
}