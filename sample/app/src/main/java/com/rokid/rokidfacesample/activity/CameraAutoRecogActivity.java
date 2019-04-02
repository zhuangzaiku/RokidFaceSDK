package com.rokid.rokidfacesample.activity;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rokid.camerakit.cameralibrary.view.DefaultCameraView;
import com.rokid.facelib.VideoRokidFace;
import com.rokid.facelib.api.IVideoRokidFace;
import com.rokid.facelib.conf.SFaceConf;
import com.rokid.facelib.conf.VideoDFaceConf;
import com.rokid.facelib.input.VideoInput;
import com.rokid.facelib.model.FaceSize;
import com.rokid.facelib.utils.FaceLog;
import com.rokid.facelib.utils.FaceRectUtils;
import com.rokid.facelib.view.InjectFaceView;
import com.rokid.rokidfacesample.R;

/**
 * 相机人脸跟踪+检测+识别
 * 识别过程自动集成在跟踪和检测的流程中
 */
public class CameraAutoRecogActivity extends Activity {

    private static final String TAG = CameraAutoRecogActivity.class.getSimpleName();

    private static final int WIDTH_720P = 1280;
    private static final int HEIGHT_720P = 720;

    private static final int WIDTH_1080P = 1920;
    private static final int HEIGHT_1080P = 1080;

    private int PREVIEW_WIDTH = WIDTH_720P;
    private int PREVIEW_HEIGHT = HEIGHT_720P;

    DefaultCameraView cameraView;
    IVideoRokidFace videoFace;
    InjectFaceView injectFaceView;

    private Rect roiRect;
    private Rect contentRect;


    ImageView faceImage;
    String faceInfo;
    TextView tv_text;
    Button btn_reload;
    boolean stop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setEnterTransition(new Explode());

        setContentView(R.layout.activity_camera);
        initView();

        FaceLog.setLogLevel(FaceLog.LOG_ROKID_ENGING, true);

        viewPost(() -> {
            contentRect = new Rect(0, 0, cameraView.getWidth(), cameraView.getHeight());
            Rect rect = new Rect();
            findViewById(R.id.focus_rect_layout).getGlobalVisibleRect(rect);

            roiRect = FaceRectUtils.toRect(rect, new FaceSize(contentRect.width(), contentRect.height()),
                    new FaceSize(PREVIEW_WIDTH, PREVIEW_HEIGHT));

            //将roi区域扩大1.25倍，用户体验会好点
            roiRect = FaceRectUtils.scaleRect(roiRect, PREVIEW_WIDTH, PREVIEW_HEIGHT, 1.25f);

            init();
        });
    }

    private void init() {

        initCase();
    }

    private void initView() {

        faceImage = findViewById(R.id.faceImage);

        cameraView = findViewById(R.id.cameraview);
        tv_text = findViewById(R.id.tv_text);
        btn_reload = findViewById(R.id.reload);
        injectFaceView = findViewById(R.id.injectView);
        findViewById(R.id.face_toggle).setOnClickListener(view -> {
            cameraView.setFace();
            roiRect = FaceRectUtils.toMirror(roiRect, cameraView.getWidth());
        });
        btn_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop = true;
                videoFace.destroy();
                faceTrackRecog();
                stop = false;
            }
        });
    }

    private void initCase() {
        faceTrackRecog();
        mH.sendEmptyMessageDelayed(1, 2000);
    }

    private void viewPost(Runnable runnable) {
        findViewById(android.R.id.content).post(() -> {
            runnable.run();
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
    private void faceTrackRecog() {
        videoFace = VideoRokidFace.create(getBaseContext(),new VideoDFaceConf().setSize(1280, 720));
        videoFace.sconfig(new SFaceConf().setRecog(true, "/sdcard/facesdk/").setAutoRecog(true));

        videoFace.startTrack(model -> {
            if(model!=null){
                Log.i(TAG,"model:"+model.toString());
            }
            injectFaceView.drawRects(model.getFaceList(),cameraView.getWidth(),cameraView.getHeight(),false);
        });

        initCam();

    }

    private void initCam() {

        cameraView.addPreviewCallBack((bytes, camera) -> {
            if (videoFace != null&&!stop) {
                videoFace.setData(new VideoInput(bytes));
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
        if (videoFace != null) {
            videoFace.destroy();
        }
    }
}
