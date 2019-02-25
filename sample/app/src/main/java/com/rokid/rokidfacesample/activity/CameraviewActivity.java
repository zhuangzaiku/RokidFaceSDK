package com.rokid.rokidfacesample.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.rokid.camerakit.cameralibrary.view.DefultCameraView;
import com.rokid.facelib.ImageRokidFace;
import com.rokid.facelib.VideoRokidFace;
import com.rokid.facelib.api.IImageRokidFace;
import com.rokid.facelib.api.IVideoRokidFace;
import com.rokid.facelib.api.ImageFaceCallback;
import com.rokid.facelib.api.RokidFaceCallback;
import com.rokid.facelib.conf.DFaceConf;
import com.rokid.facelib.conf.ImageDFaceConf;
import com.rokid.facelib.conf.SFaceConf;
import com.rokid.facelib.conf.VideoDFaceConf;
import com.rokid.facelib.engine.FaceDbEngine;
import com.rokid.facelib.input.BitmapInput;
import com.rokid.facelib.input.VideoInput;
import com.rokid.facelib.model.FaceDO;
import com.rokid.facelib.model.FaceModel;
import com.rokid.facelib.model.FaceSize;
import com.rokid.facelib.utils.FaceFileUtils;
import com.rokid.facelib.utils.FaceLog;
import com.rokid.facelib.utils.FaceMainThread;
import com.rokid.facelib.utils.FaceRectUtils;
import com.rokid.facelib.utils.StructUtils;
import com.rokid.facelib.view.InjectFaceView;
import com.rokid.rokidfacesample.MainActivity;
import com.rokid.rokidfacesample.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CameraviewActivity extends Activity {

    private static final String TAG = CameraviewActivity.class.getSimpleName();
    private int REQUEST_CODE_PICK_IMAGE = 123;

    private static final int WIDTH_720P = 1280;
    private static final int HEIGHT_720P = 720;

    private static final int WIDTH_1080P = 1920;
    private static final int HEIGHT_1080P = 1080;

    private int PREVIEW_WIDTH = WIDTH_720P;
    private int PREVIEW_HEIGHT = HEIGHT_720P;

    DefultCameraView cameraView;

    IVideoRokidFace videoFace;
    IImageRokidFace imageFace;
    InjectFaceView injectFaceView;

    private Rect roiRect;
    private Rect contentRect;


    ImageView faceImage;

    private static final String DB_NAME = "/sdcard/output2/face.face";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setEnterTransition(new Explode());

        setContentView(R.layout.activity_camera);
        initView();

        FaceLog.setLogLevel(FaceLog.LOG_ROKID_ENGING, true);

        viewPost(new Runnable() {
            @Override
            public void run() {
                contentRect = new Rect(0, 0, cameraView.getWidth(), cameraView.getHeight());
                Rect rect = new Rect();
                findViewById(R.id.focus_rect_layout).getGlobalVisibleRect(rect);

                roiRect = FaceRectUtils.toRect(rect, new FaceSize(contentRect.width(), contentRect.height()),
                        new FaceSize(PREVIEW_WIDTH, PREVIEW_HEIGHT));

                roiRect = FaceRectUtils.scaleRect(roiRect, PREVIEW_WIDTH, PREVIEW_HEIGHT, 1.25f);

                init();
            }
        });


    }

    private void init() {

        initCase();
    }

    private void initView() {

        faceImage = findViewById(R.id.faceImage);

        cameraView = findViewById(R.id.cameraview);
        injectFaceView = findViewById(R.id.injectView);
        findViewById(R.id.face_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.setFace();
                roiRect = FaceRectUtils.toMirror(roiRect, cameraView.getWidth());
            }
        });

        findViewById(R.id.sel_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });

        findViewById(R.id.take_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initImageFace();
                cameraView.setPictureSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                cameraView.takePicture(new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        FaceLog.d(TAG, "pic " + bitmap.getWidth() + " " + bitmap.getHeight());

                        FaceFileUtils.saveBitmap(bitmap, "/sdcard/tmp.png");


                        FaceMainThread.post(new Runnable() {
                            @Override
                            public void run() {
                                Intent it = new Intent();
                                it.putExtra("path", "/sdcard/tmp.png");
                                it.putExtra("rect", JSON.toJSONString(roiRect));
                                it.setClass(CameraviewActivity.this, TakePhotoPreviweActivity.class);
                                startActivity(it);
                            }
                        });
                    }
                });
            }
        });
    }

    private void initCase() {
        faceTrackRecog();
        mH.sendEmptyMessageDelayed(1, 2000);
    }

    private void initImageFace() {
        if (imageFace == null) {
            imageFace = ImageRokidFace.create(this);
            imageFace.dconfig(new ImageDFaceConf());
        }
    }

    void choosePhoto() {
        /**
         * 打开选择图片的界面
         */
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);

    }

    private void viewPost(final Runnable runnable) {
        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    private Handler mH = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

//            videoFace.destroy();
//            faceTrackRecog();
//            mH.sendEmptyMessageDelayed(1, 2200);
        }
    };

    int index = 0;
    private void faceTrackRecog() {
        new File("/sdcard/output2/").mkdirs();

        long dbId = FaceDbEngine.create(getApplicationContext()).config(new FaceDbEngine.DbEngineConfig(DB_NAME)).getDbId();


        videoFace = VideoRokidFace.create(getBaseContext());
        videoFace.sconfig(new SFaceConf().setRecog(true, dbId).setTimingRecog(false));
        videoFace.dconfig(new VideoDFaceConf().setSize(1280, 720).setRoi(roiRect));//.setRoi(roiRect));

        videoFace.startTrack(new RokidFaceCallback() {
            @Override
            public void onFaceCallback(FaceModel faceModel) {
                Log.d(MainActivity.class.getSimpleName(), "" + faceModel.toString());


                if (index++ % 15 == 0) {
                    FaceDO face = faceModel.getFace(0);
                    if (face != null) {
                        FaceDO faceDO = videoFace.recog(face.trackId);
                        Log.e(TAG, "faceDo " + faceDO.getUUIDStr());
                    }
                }

                List<Rect> list = new ArrayList<>();
                List<String> textList = new ArrayList<>();
                for (FaceDO face : faceModel.getFaceList()) {
                    Rect rect;
                    if (cameraView.isMirror()) {
                        rect = face.toMirroRect(cameraView.getWidth(), cameraView.getHeight());
                    } else {
                        rect = face.toRect(cameraView.getWidth(), cameraView.getHeight());
                    }
                    FaceLog.d(TAG, "model " + faceModel.toString());
                    list.add(rect);
                    if (!StructUtils.isEmpty(face.UUID)) {
                        FaceLog.d(TAG, "name " + new String(face.UUID));
                        textList.add(new String(face.UUID));
                    } else {
                        textList.add("unknow");
                        FaceLog.d(TAG, "name " + "unknown");
                    }
                }
                injectFaceView.drawRects(list, textList);
            }
        });

        initCam();

    }

    private void initCam() {

        cameraView.addPreviewCallBack(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (videoFace != null) {
                    videoFace.setData(new VideoInput(data));
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageFace != null) {
            imageFace.destroy();
        }
        if (videoFace != null) {
            videoFace.destroy();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));

            imageFace = ImageRokidFace.create(getBaseContext());
            DFaceConf ImgConf = new ImageDFaceConf();
            imageFace.dconfig(ImgConf);

            FaceLog.d(TAG, "pic " + bitmap.getWidth() + " " + bitmap.getHeight());

            imageFace.setImageFaceCallback(new BitmapInput(bitmap), new ImageFaceCallback() {
                @Override
                public void onFaceModel(FaceModel model) {
                    FaceLog.e(TAG, "imageFace" + model.toString());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
