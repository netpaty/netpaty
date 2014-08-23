package com.netparty.viewers;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.netparty.R;
import com.netparty.interfaces.RemoveItemListener;

/**
 * Created by Valentin on 22.08.2014.
 */
public class FullScreenTransparentDialog extends Dialog {

    private View contentView;
    private ImageView item;
    private Rect listRect;

    public void setListener(RemoveItemListener listener) {
        this.listener = listener;
    }

    private RemoveItemListener listener;

    public FullScreenTransparentDialog(Context context, Bitmap bitmap, Rect listRect) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        LayoutInflater inflater = getLayoutInflater();
        contentView = inflater.inflate(R.layout.drag_dialog_layout, null);
        setContentView(contentView);
        item = (ImageView)contentView.findViewById(R.id.drag_view);
        item.setImageBitmap(bitmap);
        this.listRect = listRect;
    }

    public void moveItem(MotionEvent event){
        switch (event.getAction()) {
            case (MotionEvent.ACTION_MOVE):
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) item.getLayoutParams();
                params.topMargin = (int) (event.getY() + contentView.getHeight() / 2 - item.getHeight());
                params.leftMargin = 16;
                item.setLayoutParams(params);
                break;
            case (MotionEvent.ACTION_UP):
                int rectHeight = listRect.height();
                listRect.top = (contentView.getHeight() - rectHeight)/2;
                listRect.bottom = (contentView.getHeight() - rectHeight)/2 + rectHeight;
                if(!listRect.contains((int)event.getX(),
                        (int)(event.getY() + contentView.getHeight() / 2 - item.getHeight()))){
                    if(listener != null){
                        listener.removeItem(null);
                    }
                }
                cancel();
                break;
        }
    }
}
