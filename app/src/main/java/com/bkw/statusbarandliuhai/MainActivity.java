package com.bkw.statusbarandliuhai;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bkw.statusbarandliuhai.utils.StatusBarUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //設置透明狀態欄
        StatusBarUtils.setColor(this, R.color.colorPrimaryDark, true);
//        StatusBarUtils.setTransparent(this);

        StatusBarUtils.setContentViewPading(this);
    }
}
