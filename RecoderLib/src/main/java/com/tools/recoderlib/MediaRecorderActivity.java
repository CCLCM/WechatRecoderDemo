package com.tools.recoderlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.tools.recoderlib.model.MediaObject;
import com.tools.recoderlib.model.MediaRecorderConfig;

import java.io.File;



/**
 * 视频录制
 */
public class MediaRecorderActivity extends Activity implements
        MediaRecorderBase.OnErrorListener, OnClickListener, MediaRecorderBase.OnPreparedListener,
        MediaRecorderBase.OnEncodeListener {

    private int RECORD_TIME_MIN = (int) (1.5f * 1000);
    /**
     * 录制最长时间
     */
    private int RECORD_TIME_MAX = 6 * 1000;
    /**
     * 刷新进度条
     */
    private static final int HANDLE_INVALIDATE_PROGRESS = 0;
    /**
     * 延迟拍摄停止
     */
    private static final int HANDLE_STOP_RECORD = 1;

    /**
     * 前后摄像头切换
     */
    private CheckBox mCameraSwitch;
    /**
     * 回删按钮、延时按钮、滤镜按钮
     */
    private CheckedTextView mRecordDelete;
    /**
     * 拍摄按钮
     */
    private TextView mRecordController;

    /**
     * 底部条
     */
    private RelativeLayout mBottomLayout;
    /**
     * 摄像头数据显示画布
     */
    private SurfaceView mSurfaceView;
    /**
     * 录制进度
     */
    private CircleProgressBar mCircleProgressBar;

    /**
     * SDK视频录制对象
     */
    private MediaRecorderBase mMediaRecorder;
    /**
     * 视频信息
     */
    private MediaObject mMediaObject;

    /**
     * 是否是点击状态
     */
    private volatile boolean mPressedStatus;
    /**
     * 录制完成后需要跳转的activity
     */
    public final static String OVER_ACTIVITY_NAME = "over_activity_name";
    /**
     * 录制配置key
     */
    public final static String MEDIA_RECORDER_CONFIG_KEY = "media_recorder_config_key";

    private boolean GO_HOME;
    private boolean startState;
    private boolean NEED_FULL_SCREEN = false;
    private RelativeLayout title_layout;

    /**
     * @param context
     * @param overGOActivityName 录制结束后需要跳转的Activity全类名
     */
    public static void goSmallVideoRecorder(Activity context, String overGOActivityName, MediaRecorderConfig mediaRecorderConfig) {
        context.startActivity(new Intent(context, MediaRecorderActivity.class).putExtra(OVER_ACTIVITY_NAME, overGOActivityName).putExtra(MEDIA_RECORDER_CONFIG_KEY, mediaRecorderConfig));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
        initData();
        loadViews();
    }

    private void initData() {
        Intent intent = getIntent();
        MediaRecorderConfig mediaRecorderConfig = intent.getParcelableExtra(MEDIA_RECORDER_CONFIG_KEY);
        if (mediaRecorderConfig == null) {
            return;
        }
        NEED_FULL_SCREEN = mediaRecorderConfig.getFullScreen();
        RECORD_TIME_MAX = mediaRecorderConfig.getRecordTimeMax();
        RECORD_TIME_MIN = mediaRecorderConfig.getRecordTimeMin();
        MediaRecorderBase.MAX_FRAME_RATE = mediaRecorderConfig.getMaxFrameRate();
        MediaRecorderBase.NEED_FULL_SCREEN = NEED_FULL_SCREEN;
        MediaRecorderBase.MIN_FRAME_RATE = mediaRecorderConfig.getMinFrameRate();
        MediaRecorderBase.SMALL_VIDEO_HEIGHT = mediaRecorderConfig.getSmallVideoHeight();
        MediaRecorderBase.SMALL_VIDEO_WIDTH = mediaRecorderConfig.getSmallVideoWidth();
        MediaRecorderBase.mVideoBitrate = mediaRecorderConfig.getVideoBitrate();
        MediaRecorderBase.CAPTURE_THUMBNAILS_TIME = mediaRecorderConfig.getCaptureThumbnailsTime();
        GO_HOME = mediaRecorderConfig.isGO_HOME();
    }

    /**
     * 加载视图
     */
    private void loadViews() {
        setContentView(R.layout.activity_media_recorder);
        // ~~~ 绑定控件
        mSurfaceView = (SurfaceView) findViewById(R.id.record_preview);
        title_layout = (RelativeLayout) findViewById(R.id.title_layout);
        mCameraSwitch = (CheckBox) findViewById(R.id.record_camera_switcher);
        mCircleProgressBar = (CircleProgressBar) findViewById(R.id.circle_record_progress);
        mRecordDelete = (CheckedTextView) findViewById(R.id.record_delete);
        mRecordController = (TextView) findViewById(R.id.record_controller);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);

        findViewById(R.id.title_back).setOnClickListener(this);
        mRecordController.setOnTouchListener(mOnVideoControllerTouchListener);

        // ~~~ 设置数据

        // 是否支持前置摄像头
        if (MediaRecorderBase.isSupportFrontCamera()) {
            mCameraSwitch.setOnClickListener(this);
        } else {
            mCameraSwitch.setVisibility(View.GONE);
        }
        mCircleProgressBar.setMaxDuration(RECORD_TIME_MAX);
        mCircleProgressBar.setMinTime(RECORD_TIME_MIN);


    }

    /**
     * 初始化画布
     */
    private void initSurfaceView() {
        if (NEED_FULL_SCREEN) {
            mBottomLayout.setBackgroundColor(0);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSurfaceView
                    .getLayoutParams();
            lp.setMargins(0,0,0,0);
            mSurfaceView.setLayoutParams(lp);
        } else {
            final int w = DeviceUtils.getScreenWidth(this);
            ((RelativeLayout.LayoutParams) mBottomLayout.getLayoutParams()).topMargin = (int) (w / (MediaRecorderBase.SMALL_VIDEO_HEIGHT / (MediaRecorderBase.SMALL_VIDEO_WIDTH * 1.0f)));
            int width = w;
            int height = (int) (w * ((MediaRecorderBase.mSupportedPreviewWidth * 1.0f) / MediaRecorderBase.SMALL_VIDEO_HEIGHT));
            //
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSurfaceView
                    .getLayoutParams();
            lp.width = width;
            lp.height = height;
            mSurfaceView.setLayoutParams(lp);
        }
    }

    /**
     * 初始化拍摄SDK
     */
    private void initMediaRecorder() {
        mMediaRecorder = new MediaRecorderNative();

        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOnEncodeListener(this);
        mMediaRecorder.setOnPreparedListener(this);

        File f = new File(RLCamera.getVideoCachePath());
        if (!FileUtils.checkFile(f)) {
            f.mkdirs();
        }
        String key = String.valueOf(System.currentTimeMillis());
        mMediaObject = mMediaRecorder.setOutputDirectory(key,
                RLCamera.getVideoCachePath() + key);
        mMediaRecorder.setSurfaceHolder(mSurfaceView.getHolder());
        mMediaRecorder.prepare();
    }


    /**
     * 点击屏幕录制
     */
    private View.OnTouchListener mOnVideoControllerTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mMediaRecorder == null) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 检测是否手动对焦
                    // 判断是否已经超时
                    if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
                        return true;
                    }

                    // 取消回删
                    if (cancelDelete())
                        return true;
                    if (!startState) {
                        startState = true;
                        startRecord();
                    } else {
                        mMediaObject.buildMediaPart(mMediaRecorder.mCameraId);
                        mCircleProgressBar.setData(mMediaObject);
                        setStartUI();
                        mMediaRecorder.setRecordState(true);
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    stopRecord();
                    break;
            }
            return true;
        }

    };

    @Override
    public void onResume() {
        super.onResume();

        if (mMediaRecorder == null) {
            initMediaRecorder();
        } else {
            mMediaRecorder.prepare();
            mCircleProgressBar.setData(mMediaObject);
        }
    }


    /**
     * 开始录制
     */
    private void startRecord() {
        if (mMediaRecorder != null) {

            MediaObject.MediaPart part = mMediaRecorder.startRecord();
            if (part == null) {
                return;
            }

            mCircleProgressBar.setData(mMediaObject);
        }

        setStartUI();
    }

    private void setStartUI() {
        mPressedStatus = true;
//		TODO 开始录制的图标
        mRecordController.animate().scaleX(0.8f).scaleY(0.8f).setDuration(500).start();


        if (mHandler != null) {
            mHandler.removeMessages(HANDLE_INVALIDATE_PROGRESS);
            mHandler.sendEmptyMessage(HANDLE_INVALIDATE_PROGRESS);

            mHandler.removeMessages(HANDLE_STOP_RECORD);
            mHandler.sendEmptyMessageDelayed(HANDLE_STOP_RECORD,
                    RECORD_TIME_MAX - mMediaObject.getDuration());
        }
//        mRecordDelete.setVisibility(View.GONE);
        mCameraSwitch.setEnabled(false);
    }

    @Override
    public void onBackPressed() {
        /*if (mRecordDelete != null && mRecordDelete.isChecked()) {
            cancelDelete();
            return;
        }*/

        if (mMediaObject != null && mMediaObject.getDuration() > 1) {
            // 未转码
            new AlertDialog.Builder(this)
                    .setTitle(R.string.hint)
                    .setMessage(R.string.record_camera_exit_dialog_message)
                    .setNegativeButton(
                            R.string.record_camera_cancel_dialog_yes,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    mMediaObject.delete();
                                    finish();
                                }

                            })
                    .setPositiveButton(R.string.record_camera_cancel_dialog_no,
                            null).setCancelable(false).show();
            return;
        }

        if (mMediaObject != null)
            mMediaObject.delete();
        finish();
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stopRecord();
        }
        setStopUI();
    }

    private void setStopUI() {
        mPressedStatus = false;
        mRecordController.animate().scaleX(1).scaleY(1).setDuration(500).start();


//        mRecordDelete.setVisibility(View.VISIBLE);
        mCameraSwitch.setEnabled(true);

        mHandler.removeMessages(HANDLE_STOP_RECORD);
        checkStatus();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (mHandler.hasMessages(HANDLE_STOP_RECORD)) {
            mHandler.removeMessages(HANDLE_STOP_RECORD);
        }

        // 处理开启回删后其他点击操作
        if (id != R.id.record_delete) {
            if (mMediaObject != null) {
                MediaObject.MediaPart part = mMediaObject.getCurrentPart();
                if (part != null) {
                    if (part.remove) {
                        part.remove = false;
//                        mRecordDelete.setChecked(false);
                        if (mCircleProgressBar != null)
                            mCircleProgressBar.invalidate();
                    }
                }
            }
        }

        if (id == R.id.title_back) {
            onBackPressed();
        } else if (id == R.id.record_camera_switcher) {// 前后摄像头切换


            if (mMediaRecorder != null) {
                mMediaRecorder.switchCamera();
            }


        }else if (id == R.id.record_delete) {
            // 取消回删
            if (mMediaObject != null) {
                MediaObject.MediaPart part = mMediaObject.getCurrentPart();
                if (part != null) {
                    if (part.remove) {
                        part.remove = false;
                        mMediaObject.removePart(part, true);
//                        mRecordDelete.setChecked(false);
                    } else {
                        part.remove = true;
//                        mRecordDelete.setChecked(true);
                    }
                }
                if (mCircleProgressBar != null)
                    mCircleProgressBar.invalidate();

                // 检测按钮状态
                checkStatus();
            }
        }
    }


    /**
     * 取消回删
     */
    private boolean cancelDelete() {
        if (mMediaObject != null) {
            MediaObject.MediaPart part = mMediaObject.getCurrentPart();
            if (part != null && part.remove) {
                part.remove = false;
//                mRecordDelete.setChecked(false);

                if (mCircleProgressBar != null)
                    mCircleProgressBar.invalidate();

                return true;
            }
        }
        return false;
    }

    /**
     * 检查录制时间，显示/隐藏下一步按钮
     */
    private int checkStatus() {
        int duration = 0;
        if (!isFinishing() && mMediaObject != null) {
            duration = mMediaObject.getDuration();
            if (duration < RECORD_TIME_MIN) {
                if (duration == 0) {
                    mCameraSwitch.setVisibility(View.VISIBLE);
                } else {
                    mCameraSwitch.setVisibility(View.GONE);
                }
            } else {
            }
        }
        return 0;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_INVALIDATE_PROGRESS:
                    if (mMediaRecorder != null && !isFinishing()) {
                        if (mMediaObject != null && mMediaObject.getMedaParts() != null && mMediaObject.getDuration() >= RECORD_TIME_MAX) {
                            return;
                        }
                        if (mCircleProgressBar != null)
                            mCircleProgressBar.invalidate();
                        if (mPressedStatus)
                            sendEmptyMessageDelayed(0, 30);
                    }
                    break;
            }
        }
    };

    @Override
    public void onEncodeStart() {
        showProgress("", getString(R.string.record_camera_progress_message));
    }

    @Override
    public void onEncodeProgress(int progress) {
    }

    /**
     * 转码完成
     */
    @Override
    public void onEncodeComplete() {
        hideProgress();
        Intent intent = null;
        try {
            intent = new Intent(this, Class.forName(getIntent().getStringExtra(OVER_ACTIVITY_NAME)));
            intent.putExtra("path", mMediaObject.getOutputTempTranscodingVideoPath());
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("需要传入录制完成后跳转的Activity的全类名");
        }

        finish();
    }

    /**
     * 转码失败 检查sdcard是否可用，检查分块是否存在
     */
    @Override
    public void onEncodeError() {
        hideProgress();
        Toast.makeText(this, R.string.record_video_transcoding_faild,
                Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onVideoError(int what, int extra) {

    }

    @Override
    public void onAudioError(int what, String message) {

    }

    @Override
    public void onPrepared() {
        initSurfaceView();
    }

    public void onFinished() {
        finish();
    }

    protected ProgressDialog mProgressDialog;

    public ProgressDialog showProgress(String title, String message) {
        return showProgress(title, message, -1);
    }

    public ProgressDialog showProgress(String title, String message, int theme) {
        if (mProgressDialog == null) {
            if (theme > 0)
                mProgressDialog = new ProgressDialog(this, theme);
            else
                mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setCanceledOnTouchOutside(false);// 不能取消
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);// 设置进度条是否不明确
        }

        if (!StringUtils.isEmpty(title))
            mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
        return mProgressDialog;
    }

    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaRecorder.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaRecorder instanceof MediaRecorderNative) {
            ((MediaRecorderNative) mMediaRecorder).activityStop();
        }
        hideProgress();
        mProgressDialog = null;

    }

}
