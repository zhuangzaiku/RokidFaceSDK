package com.rokid.rokidfacesample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.view.KeyEvent;

import com.rokid.rokidfacesample.activity.CameraAutoRecogActivity;
import com.rokid.rokidfacesample.activity.CameraRecogActivity;
import com.rokid.rokidfacesample.activity.DbControlActivity;
import com.rokid.rokidfacesample.activity.ImageActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setEnterTransition(new Explode());
        setContentView(R.layout.activity_main);

        PermissionUtils.permissionCheck(this);


        findViewById(R.id.camera_auto_recog).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, CameraAutoRecogActivity.class));
        });

        findViewById(R.id.camera_recog).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, CameraRecogActivity.class));
        });

        findViewById(R.id.img_face).setOnClickListener(view ->{
            startActivity(new Intent(MainActivity.this,ImageActivity.class));
        });

        findViewById(R.id.db_op).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, DbControlActivity.class));
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