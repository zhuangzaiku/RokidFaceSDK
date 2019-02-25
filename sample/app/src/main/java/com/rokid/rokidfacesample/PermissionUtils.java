package com.rokid.rokidfacesample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class PermissionUtils {

    private static String[] PERMISSIONS_FACE = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.RECORD_AUDIO
    };

    public static void permissionCheck(Activity activity) {

//        if (!shouldShowRequestPermissionRationale(activity)) {
//            return;
//        }
        if (checkSelfPermission(activity)) {
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_FACE, 123);

//            if (shouldShowRequestPermissionRationale(activity)) {
//                ActivityCompat.requestPermissions(activity, PERMISSIONS_FACE, 123);
//            } else {
//                ActivityCompat.requestPermissions(activity, PERMISSIONS_FACE, 123);
//            }
        }
    }

    private static boolean checkSelfPermission(Context context) {
        boolean ret = true;
        for (String permission : PERMISSIONS_FACE) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    private static boolean shouldShowRequestPermissionRationale(Activity activity) {
        boolean ret = false;
        for (String permission : PERMISSIONS_FACE) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                ret = true;
                break;
            }
        }
        return ret;
    }
}
