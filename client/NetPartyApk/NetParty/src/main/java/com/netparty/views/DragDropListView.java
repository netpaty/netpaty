package com.netparty.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.widget.BaseAdapter;
import android.widget.ListView;

import com.netparty.data.MetaContactRec;
import com.netparty.interfaces.Account;
import com.netparty.interfaces.RemoveItemListener;


/**
 * Created by Valentin on 13.08.2014.
 */
public class DragDropListView extends ListView {

    private Context context;
    private OnTouchListener listener;
    private OnTouchListener outUpListener;
    private RemoveItemListener removeItemListener;


    public void setDragListener(OnTouchListener listener) {
        this.listener = listener;
    }



    public void setOutUpListener(OnTouchListener outUpListener) {
        this.outUpListener = outUpListener;
    }



    public DragDropListView(Context context, AttributeSet set) {
        super(context, set);
        this.context = context;

    }

    public void setRemoveItemListener(RemoveItemListener removeItemListener) {
        this.removeItemListener = removeItemListener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev){
        super.onTouchEvent(ev);
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                Rect listRect = new Rect();
                listRect.set(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
                if (!listRect.contains((int) ev.getX(), (int) ev.getY())) {
                    if (outUpListener != null) outUpListener.onTouch(this, ev);
                }
            }
            if (listener != null) listener.onTouch(this, ev);

        return true;
    }

    public void receiveItem(int x, int y, MetaContactRec receivedItem){
        if(receivedItem != null) {
            Rect listRect = new Rect();
            listRect.set(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
            if (listRect.contains(x, y)) {
                int position = getPositionOnPoint(x, y);
                if (position > -1 && !this.getAdapter().getItem(position).equals(receivedItem)) {
                    for (Account account : receivedItem.getAccounts()) {
                        ((MetaContactRec) this.getAdapter().getItem(position)).addAccount(account);
                    }
                    ((BaseAdapter) this.getAdapter()).notifyDataSetChanged();
                    removeItemListener.removeItem(receivedItem);
                }
            }
        }
    }


    private int getPositionOnPoint(int x, int y){
        int position = -1;
        for (int i = 0; i < getChildCount(); i++) {

            Rect viewRect = new Rect();
            View child = getChildAt(i);
            int left = child.getLeft() + this.getLeft();
            int right = child.getRight() + this.getLeft();
            int top = child.getTop() + this.getTop();
            int bottom = child.getBottom() + this.getTop();
            viewRect.set(left, top, right, bottom);
            if (viewRect.contains(x, y)) {
                position = i + this.getFirstVisiblePosition();
            }
        }
        return position;
    }




}
