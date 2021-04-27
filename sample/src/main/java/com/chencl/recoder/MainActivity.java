package com.chencl.recoder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


import com.tools.recoderlib.DeviceUtils;
import com.tools.recoderlib.RLCamera;
import com.tools.recoderlib.MediaRecorderActivity;
import com.tools.recoderlib.model.AutoVBRMode;
import com.tools.recoderlib.model.BaseMediaBitrateConfig;
import com.tools.recoderlib.model.MediaRecorderConfig;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_REQUEST_CODE = 0x001;
    private static final String[] permissionManifest = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSmallVideo();
        permissionCheck();

    }

    private void setSupportCameraSize() {
        Camera back = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        List<Camera.Size> backSizeList = back.getParameters().getSupportedPreviewSizes();
        StringBuilder str = new StringBuilder();
        str.append("经过检查您的摄像头，如使用后置摄像头您可以输入的高度有：");
        for (Camera.Size bSize : backSizeList) {
            str.append(bSize.height + "、");
        }
        back.release();
        Camera front = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        List<Camera.Size> frontSizeList = front.getParameters().getSupportedPreviewSizes();
        str.append("如使用前置摄像头您可以输入的高度有：");
        for (Camera.Size fSize : frontSizeList) {
            str.append(fSize.height + "、");
        }
        front.release();
    }


    /**
     * 开始录制按钮点击事件
     *
     * @param c
     */
    public void go(View c) {

        BaseMediaBitrateConfig recordMode;

        recordMode = new AutoVBRMode();
        recordMode.setVelocity("none");

        MediaRecorderConfig config = new MediaRecorderConfig.Buidler()
                .fullScreen(true)
                .smallVideoWidth(1920)
                .smallVideoHeight(1080)
                .recordTimeMax(15000)
                .recordTimeMin(1500)
                .maxFrameRate(60)
                .videoBitrate(6000000)
                .captureThumbnailsTime(1)
                .build();
        MediaRecorderActivity.goSmallVideoRecorder(this, VideoPlayerActivity.class.getName(), config);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (Manifest.permission.CAMERA.equals(permissions[i])) {
                        setSupportCameraSize();
                    } else if (Manifest.permission.RECORD_AUDIO.equals(permissions[i])) {

                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void permissionCheck() {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean permissionState = true;
            for (String permission : permissionManifest) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionState = false;
                }
            }
            if (!permissionState) {
                ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE);
            } else {
                setSupportCameraSize();
            }
        } else {
            setSupportCameraSize();
        }
    }

    public static void initSmallVideo() {

        // 设置拍摄视频缓存路径
        File dcim = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                RLCamera.setVideoCachePath(dcim + "/pic/");
            } else {
                RLCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/",
                        "/sdcard-ext/")
                        + "/pic/");
            }
        } else {
            RLCamera.setVideoCachePath(dcim + "/pic/");
        }
        // 初始化拍摄
        RLCamera.initialize(false, null);
    }
}
