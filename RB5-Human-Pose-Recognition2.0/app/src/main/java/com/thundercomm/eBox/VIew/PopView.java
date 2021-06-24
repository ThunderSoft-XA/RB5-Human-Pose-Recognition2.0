package com.thundercomm.eBox.VIew;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thundercomm.eBox.R;

/**
 * @Describe 弹出显示行人信息View
 */
public class PopView extends LinearLayout {
    private TextView tv_message;

    private String name;
    private String local;
    private String direction;
    private String time;
    private Context mContext;

    /**
     * @Describe 初始化View
     */
    public PopView(Context context) {
        super(context);
        mContext = context;
        addView(inflate(context, R.layout.pedestrian_popview, null));
        initView();
    }

    private void initView() {
        tv_message = findViewById(R.id.tv_popView);
    }

    public void setTv_message(String message) {
        tv_message.setText(message);
    }

    public void setTv_message_SpannableString(SpannableStringBuilder s) {
        tv_message.setText(s);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "{" + this.getTime() + " " + this.getLocal() + "}";
    }
}
