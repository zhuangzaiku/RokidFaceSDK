package com.rokid.rokidfacesample.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.rokid.facelib.ImageRokidFace;
import com.rokid.facelib.RokidFaceEnv;
import com.rokid.facelib.api.IImageRokidFace;
import com.rokid.facelib.api.ImageFaceCallback;
import com.rokid.facelib.conf.SFaceConf;
import com.rokid.facelib.engine.FaceDbEngine;
import com.rokid.facelib.input.BitmapInput;
import com.rokid.facelib.model.FaceDO;
import com.rokid.facelib.model.FaceModel;
import com.rokid.facelib.utils.DigestUtils;
import com.rokid.facelib.utils.FaceRectUtils;
import com.rokid.facelib.utils.StructUtils;
import com.rokid.facelib.view.InjectBitmapDraw;
import com.rokid.rokidfacesample.R;

public class PhotoActivity extends Activity {
    private static final String TAG = PhotoActivity.class.getSimpleName();

    private int DB_PREVIEW = 11;
    private int DB_ADD = 12;
    private int DB_REMOVE = 13;
    private int DB_QUERY = 14;
    private int DB_LIST = 15;


    private ImageView picPreview;
    private EditText editText;
    private Button commitBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RokidFaceEnv.init();

        getWindow().setEnterTransition(new Explode());


        setContentView(R.layout.activity_photo);

        long dbId = FaceDbEngine.create(getApplicationContext()).
                config(new FaceDbEngine.DbEngineConfig("/sdcard/output2/face.face")).getDbId();

        final FaceDbEngine dbEngine = FaceDbEngine.create(getApplicationContext());

        findViewById(R.id.db_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto(DB_PREVIEW);
            }
        });
        findViewById(R.id.db_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto(DB_ADD);
            }
        });


        findViewById(R.id.db_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setHint("db_remove");
                commitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(editText.getText())) {
                            boolean exist = dbEngine.contain(DigestUtils.UUID(editText.getText().toString()));
                            Toast.makeText(getApplicationContext(), "uuid " + editText.getText().toString() + " " + exist, Toast.LENGTH_LONG).show();
                            if (exist) {
                                boolean ret = dbEngine.remove(DigestUtils.UUID(editText.getText().toString()));
                                Toast.makeText(getApplicationContext(), "uuid " + editText.getText().toString() + " remvoe " + ret, Toast.LENGTH_LONG).show();
                            }
                            picPreview.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        findViewById(R.id.db_query).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setHint("db_query");
                commitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(editText.getText())) {
                            boolean exist = dbEngine.contain(DigestUtils.UUID(editText.getText().toString()));
                            Toast.makeText(getApplicationContext(), "db query " + exist, Toast.LENGTH_LONG).show();
                            picPreview.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
        findViewById(R.id.db_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "db size " + dbEngine.dbSize(), Toast.LENGTH_SHORT).show();
                StringBuilder uuidList = new StringBuilder();
                for (int i = 0; i < dbEngine.dbSize(); i++) {
                    uuidList.append(new String(dbEngine.getUUID(i)).replace(" ", ""));
                    uuidList.append(" ");
                }
                Toast.makeText(getApplicationContext(), "db uuid list " + uuidList.toString(), Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.db_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbEngine.delDb();
            }
        });

        picPreview = findViewById(R.id.pic_preview);

        editText = findViewById(R.id.edit_text);

        commitBtn = findViewById(R.id.commit);

    }

    void choosePhoto(int id) {
        /**
         * 打开选择图片的界面
         */
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, id);
    }


    private void previewFace(final Bitmap bitmapFinal, long dbId) {
        if (bitmapFinal == null || dbId == 0) {
            return;
        }
        IImageRokidFace imageFace = ImageRokidFace.create(getApplicationContext());
        imageFace.sconfig(new SFaceConf().setRecog(true, dbId));

        imageFace.setImageFaceCallback(new BitmapInput(bitmapFinal), new ImageFaceCallback() {
            @Override
            public void onFaceModel(FaceModel model) {
                picPreview.setVisibility(View.VISIBLE);
                Bitmap bp = InjectBitmapDraw.copyBitmap(bitmapFinal);
                for (FaceDO face : model.getFaceList()) {
                    Log.e(TAG, "uuid " + face.getUUIDStr());
                    if (!StructUtils.isEmpty(face.UUID)) {
                        editText.setText(face.getUUIDStr());
                    }
                    Toast.makeText(getApplicationContext(), face.getUUIDStr(), Toast.LENGTH_LONG).show();
                    InjectBitmapDraw.drawRect(bp, FaceRectUtils.toRect(
                            face.toRect(model.width, model.height), 1.5f, model.width, model.height));
                }
                picPreview.setImageBitmap(bp);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            Bitmap bitmap = null;
            if (data != null) {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
            }

//            if (requestCode < 10) {
//                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
//                FaceLog.d(TAG, "pic " + bitmap.getWidth() + " " + bitmap.getHeight());
//            } else if (requestCode < 100) {
//                FaceFileUtils.loadBytes(getContentResolver().openInputStream(data.getData()));
//            }

            long dbId = FaceDbEngine.create(getApplicationContext()).
                    config(new FaceDbEngine.DbEngineConfig("/sdcard/output2/face.face")).getDbId();

            final FaceDbEngine dbEngine = FaceDbEngine.create(getApplicationContext());

            final Bitmap bitmapFinal = bitmap;
            if (requestCode == DB_PREVIEW) {
                previewFace(bitmapFinal, dbId);
            } else if (requestCode == DB_ADD) {
                previewFace(bitmapFinal, dbId);
                editText.setHint("db_add");
                commitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(editText.getText())) {
//                        editText.getText().toString()
                            dbEngine.setData(new BitmapInput(bitmapFinal));
                            int ret = dbEngine.add(DigestUtils.UUID(editText.getText().toString()));
                            if (ret < 0) {
                                Toast.makeText(getApplicationContext(), "db add fail", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "db add suc " + editText.getText().toString(), Toast.LENGTH_LONG).show();

                            }
                            picPreview.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            } else if (requestCode == DB_QUERY) {

            } else if (requestCode == DB_REMOVE) {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
