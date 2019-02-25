package com.rokid.rokidfacesample;

import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;


import com.rokid.rokidfacesample.activity.CameraviewActivity;
import com.rokid.rokidfacesample.activity.PhotoActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String DB_NAME = "db_e85b72faeab24ce8982f4769fdbb7e7f";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setEnterTransition(new Explode());
        setContentView(R.layout.activity_main);

        PermissionUtils.permissionCheck(this);


        findViewById(R.id.camera_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setEnterTransition(new Explode());
                startActivity(new Intent(MainActivity.this, CameraviewActivity.class));
            }
        });

        findViewById(R.id.db_op).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PhotoActivity.class),
                        ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            }
        });


        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Process.killProcess(Process.myPid());
        }
        return super.onKeyUp(keyCode, event);
    }
}