package com.rokid.rokidfacesample.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.rokid.facelib.ImageRokidFace;
import com.rokid.facelib.api.IImageRokidFace;
import com.rokid.facelib.api.ImageFaceCallback;
import com.rokid.facelib.conf.ImageDFaceConf;
import com.rokid.facelib.input.BitmapInput;
import com.rokid.facelib.model.FaceDO;
import com.rokid.facelib.model.FaceModel;
import com.rokid.facelib.utils.FaceBitmapUtils;
import com.rokid.facelib.utils.FaceMainThread;
import com.rokid.facelib.view.InjectBitmapDraw;
import com.rokid.rokidfacesample.R;

import java.util.ArrayList;
import java.util.List;

public class TakePhotoPreviweActivity extends Activity {

    private ImageView imageView;
    private IImageRokidFace imageFace;
    private Rect roiRect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_preview);

        initImageFace();
        init();
    }

    private void init() {
        String filePath = getIntent().getStringExtra("path");
        roiRect = JSON.parseObject(getIntent().getStringExtra("rect"), Rect.class);

        imageView = findViewById(R.id.photo_image);

        Bitmap fileBitmap = FaceBitmapUtils.loadBitmap(filePath);
        if (fileBitmap == null) {
            Toast.makeText(this, "bitmap null", Toast.LENGTH_SHORT).show();
            return;
        }

//        if (roiRect != null) {
//            Bitmap copyBp = InjectBitmapDraw.copyBitmap(fileBitmap);
//            InjectBitmapDraw.drawRect(copyBp, roiRect);
//            fileBitmap = copyBp;
//        }

        final Bitmap bitmap = fileBitmap;

        imageFace.setImageFaceCallback(new BitmapInput(bitmap), new ImageFaceCallback() {
            @Override
            public void onFaceModel(FaceModel model) {
                List<Rect> list = new ArrayList<>();
                for (FaceDO face : model.getFaceList()) {
                    list.add(face.toRect(bitmap.getWidth(), bitmap.getHeight()));
                }
                final Bitmap copyBp = InjectBitmapDraw.copyBitmap(bitmap);
                InjectBitmapDraw.drawRectList(copyBp, list);
                FaceMainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(copyBp);
                    }
                });
            }
        });
    }

    private void initImageFace() {
        if (imageFace == null) {
            imageFace = ImageRokidFace.create(this);
            imageFace.init(new ImageDFaceConf(), null);
        }
    }
}
