package com.netparty.views;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.netparty.R;
import com.netparty.interfaces.MenuPanelShowListener;

/**
 * Created by Valentin on 19.08.2014.
 */
public class MenuPanel extends LinearLayout {

    boolean init = false;
    boolean ownMoving = false;

    public void setShowListener(MenuPanelShowListener showListener) {
        this.showListener = showListener;
    }

    MenuPanelShowListener showListener;


    public MenuPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ownMoving = true;
                move(event.getX(), event.getAction());
                return true;
            }
        });

    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(!init){
            init = true;
            setX(-getWidth());
        }
    }



    public void move(float eX, int action){
        float X = 0;
        switch(action){
            case(MotionEvent.ACTION_DOWN):
                X = getX();

                break;
            case(MotionEvent.ACTION_MOVE):
                if(showListener!=null) showListener.menuHide();
                if(eX <= getWidth()) {
                    if(!ownMoving) {
                        setX(eX - getWidth());
                    }
                    else {
                        float x = eX - (X - getX());
                        setX(x - getWidth());
                    }
                }
                break;
            case(MotionEvent.ACTION_UP):
                ownMoving = false;
                if(getX() > - getWidth()/2){
                    setX(0);
                    if(showListener!=null) showListener.menuShown();
                }
                else{
                    setX(-getWidth());
                    if(showListener!=null) showListener.menuHide();
                }
                break;
        }

    }
}
