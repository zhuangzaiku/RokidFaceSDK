package com.rokid.rokidfacesample.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rokid.facelib.db.UserInfo;
import com.rokid.facelib.face.FaceDbHelper;
import com.rokid.rokidfacesample.R;

public class DbControlActivity extends Activity {
    private static final String TAG = "DbControlActivity";
    private Button db_add, db_remove, db_save, db_clear, db_create;
    private Handler mH;
    private HandlerThread mT;
    private FaceDbHelper dbCreator;
    private String uuid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_control);
        mT = new HandlerThread("dbThread");
        mT.start();
        mH = new Handler(mT.getLooper());
        initView();
        setListener();

    }

    private void initView() {
        db_add = findViewById(R.id.db_add);
        db_remove = findViewById(R.id.db_remove);
        db_save = findViewById(R.id.db_save);
        db_clear = findViewById(R.id.db_clear);
        db_create = findViewById(R.id.db_create);
    }

    private void setListener() {
        db_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        dbCreator = new FaceDbHelper(getApplicationContext());
                        dbCreator.setModel(FaceDbHelper.MODEL_DB);
                        dbCreator.clearDb();
                        dbCreator.configDb("user.db");
                        Toast.makeText(DbControlActivity.this, "dbCreate", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        db_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        UserInfo info = new UserInfo("安慰", "3522031989");
                        Bitmap bm = BitmapFactory.decodeFile("sdcard/test.jpg");
                        uuid = dbCreator.add(bm, info).uuid;
                        Log.i(TAG, "uuid:" + uuid);
                        UserInfo info1 = new UserInfo("迟丽颖", "3522031989");
                        Bitmap bm1 = BitmapFactory.decodeFile("sdcard/test_2.jpg");
                        String uuid1 = dbCreator.add(bm1, info1).uuid;
                        Log.i(TAG, "uuid:" + uuid1);
                    }
                });
            }
        });
        db_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        dbCreator.remove(uuid);
                    }
                });
            }
        });
        db_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbCreator.save();
            }
        });
        db_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbCreator.clearDb();
            }
        });
    }
}
