package com.rokid.rokidfacesample.activity;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.transition.Explode;
import android.widget.ImageView;
import android.widget.TextView;

import com.rokid.camerakit.cameralibrary.view.DefaultCameraView;
import com.rokid.facelib.VideoRokidFace;
import com.rokid.facelib.api.IVideoRokidFace;
import com.rokid.facelib.conf.SFaceConf;
import com.rokid.facelib.conf.VideoDFaceConf;
import com.rokid.facelib.input.VideoInput;
import com.rokid.facelib.model.FaceDO;
import com.rokid.facelib.model.FaceModel;
import com.rokid.facelib.model.FaceSize;
import com.rokid.facelib.utils.FaceRectUtils;
import com.rokid.facelib.view.InjectFaceView;
import com.rokid.rokidfacesample.R;

import java.util.List;

/**
 * 相机人脸跟踪+检测+识别
 * 识别过程需要手动调用
 */
public class CameraRecogActivity extends Activity {

    private static final String TAG = CameraRecogActivity.class.getSimpleName();
    private int REQUEST_CODE_PICK_IMAGE = 123;

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


    private HandlerThread handlerThread;
    private Handler recogHandler;

    ImageView faceImage;
    String faceInfo;
    TextView tv_text;
    private Handler mH = new Handler(Looper.getMainLooper());
    private FaceModel faceModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setEnterTransition(new Explode());

        setContentView(R.layout.activity_camera);
        initView();

        viewPost(() -> {
            init();
        });
    }

    private void init() {
        contentRect = new Rect(0, 0, cameraView.getWidth(), cameraView.getHeight());
        Rect rect = new Rect();
        findViewById(R.id.focus_rect_layout).getGlobalVisibleRect(rect);

        roiRect = FaceRectUtils.toRect(rect, new FaceSize(contentRect.width(), contentRect.height()),
                new FaceSize(PREVIEW_WIDTH, PREVIEW_HEIGHT));

        //将roi区域扩大1.25倍，用户体验会好点
        roiRect = FaceRectUtils.scaleRect(roiRect, PREVIEW_WIDTH, PREVIEW_HEIGHT, 1.25f);
        handlerThread = new HandlerThread("recog");
        handlerThread.setPriority(Thread.MAX_PRIORITY);
        handlerThread.start();
        recogHandler = new Handler(handlerThread.getLooper());
        faceTrackRecog();
        recogHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doRecog();
                recogHandler.postDelayed(this,1000);
            }
        },1000);
    }

    private void doRecog() {
        if(faceModel==null){
            return;
        }
        List<FaceDO> faceDOS = faceModel.getFaceList();
        for(FaceDO faceDO:faceDOS){
            FaceDO face = videoFace.recog(faceDO.trackId);
            if(face==null){
                return;
            }
            StringBuilder sb = new StringBuilder();
            if(face.pose!=null) {
                sb.append("pose:");
                for (float f : face.pose) {
                    sb.append(f).append(",");
                }
                sb.append("\n");
            }
            if(face.userInfo!=null){
                sb.append("name:"+face.userInfo.name+"\n");
            }
            if(face.sharpness!=0){
                sb.append("sharpness:"+face.sharpness+"\n");
            }
            faceInfo = sb.toString();
            mH.post(new Runnable() {
                @Override
                public void run() {
                    tv_text.setText(faceInfo);
                }
            });
        }
    }

    private void initView() {
        faceImage = findViewById(R.id.faceImage);
        cameraView = findViewById(R.id.cameraview);
        tv_text = findViewById(R.id.tv_text);
        injectFaceView = findViewById(R.id.injectView);
        findViewById(R.id.face_toggle).setOnClickListener(view -> {
            cameraView.setFace();
            roiRect = FaceRectUtils.toMirror(roiRect, cameraView.getWidth());
        });
    }


    private void viewPost(Runnable runnable) {
        findViewById(android.R.id.content).post(() -> {
            runnable.run();
        });
    }


    private void faceTrackRecog() {
        videoFace = VideoRokidFace.create(getBaseContext(),new VideoDFaceConf().setSize(1280, 720).setRoi(roiRect));
        //关闭自动识别
        videoFace.sconfig(new SFaceConf().setRecog(true, "/sdcard/facesdk/user.db").setAutoRecog(false));
        videoFace.dconfig(new VideoDFaceConf().setSize(1280, 720).setRoi(roiRect));//.setRoi(roiRect));

        videoFace.startTrack(model -> {
//            faceModel = model;
//
//            List<Rect> list = new ArrayList<>();
//            List<String> textList = new ArrayList<>();
//            for (FaceDO face : model.getFaceList()) {
//                Rect rect;
//                if (cameraView.isMirror()) {
//                    rect = face.toMirroRect(cameraView.getWidth(), cameraView.getHeight());
//                } else {
//                    rect = face.toRect(cameraView.getWidth(), cameraView.getHeight());
//                }
//                list.add(rect);
//                StringBuilder sb = new StringBuilder();
//                if(face.pose!=null) {
//                    sb.append("pose:");
//                    for (float f : face.pose) {
//                        sb.append(f).append(",");
//                    }
//                    sb.append("\n");
//                }
//
//                if(face.sharpness!=0){
//                    sb.append("sharpness:"+face.sharpness+"\n");
//                }
//                faceInfo = sb.toString();
//
//                textList.add("unkonw");
//                mH.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        tv_text.setText(faceInfo);
//                    }
//                });
//            }
//            injectFaceView.drawRects(list, textList,null);
        });

        initCam();

    }

    private void initCam() {

        cameraView.addPreviewCallBack((bytes, camera) -> {
            if (videoFace != null) {
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
