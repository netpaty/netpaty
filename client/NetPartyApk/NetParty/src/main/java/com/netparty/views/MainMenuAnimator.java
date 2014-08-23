package com.netparty.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.netparty.R;

/**
 * Created by Valentin on 18.08.2014.
 */
public class MainMenuAnimator extends LinearLayout {

    private Context context;
    private Animation inAnimation, outAnimation;

    public MainMenuAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        inAnimation = AnimationUtils.loadAnimation(context, R.anim.show_left);
        outAnimation = AnimationUtils.loadAnimation(context, R.anim.hide_left);
        if(inAnimation != null){
            inAnimation.setFillAfter(true);

        }
        if(outAnimation != null){
            outAnimation.setFillAfter(true);
            outAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        }

    }



    public void hide(){
        if(outAnimation != null) startAnimation(outAnimation);
    }

    public void show(){
        setVisibility(VISIBLE);
        if(inAnimation != null) startAnimation(inAnimation);
    }


}
