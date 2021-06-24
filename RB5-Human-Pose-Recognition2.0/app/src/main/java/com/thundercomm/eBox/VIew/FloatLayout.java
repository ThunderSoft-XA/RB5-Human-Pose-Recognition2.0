package com.thundercomm.eBox.VIew;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.thundercomm.eBox.Utils.ToastUtils;

import java.lang.reflect.Parameter;

public class FloatLayout extends View {
    private Context mContext;
    public boolean isclick = true;
    private long startTime;
    private float mTouchStartY;
    private float mTouchStartX;
    private ViewGroup mWindowManager;
    private Parameter mWmParams;

    public FloatLayout(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 获取相对屏幕的坐标，即以屏幕左上角为原点
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        //下面的这些事件，跟图标的移动无关，为了区分开拖动和点击事件
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startTime = System.currentTimeMillis();
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //图标移动的逻辑在这里
                float mMoveStartX = event.getX();
                float mMoveStartY = event.getY();
                // 如果移动量大于3才移动
                if (Math.abs(mTouchStartX - mMoveStartX) > 3
                        && Math.abs(mTouchStartY - mMoveStartY) > 3) {
                    return false;
                }
                break;
        }
        //响应点击事件
        if (isclick) {
            ToastUtils.show(mContext,"hhh");
        }
        return true;
    }
}
