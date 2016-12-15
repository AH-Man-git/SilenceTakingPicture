package com.pounds.nonesurviewcamera;

/**
 * Created by Administrator on 2016/12/14 0014.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;
public class CameraUtil {

    private SurfaceView sView; //画布视图
    private SurfaceHolder surfaceHolder; //画布Holder
    public Camera camera; // 定义系统所用的照相机
    private boolean isPreview = false;// 是否在浏览中
    private AudioManager manager; //声音管理
    private int volumn; //声音值
    private String picPath = "";
    private MainActivity context;


    public CameraUtil(Context context) {
        this.context = (MainActivity) context;
    }


    @SuppressWarnings("deprecation")
    public void initCameraFirst() {
        manager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        manager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        volumn = manager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        if (volumn != 0) {
            // 如果需要静音并且当前未静音（muteMode的设置可以放在Preference中）
            manager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0,
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
        sView = (SurfaceView) ((Activity) context).findViewById(R.id.surfaceView1);


        if (MyApplication.invadeMonitor()) {
            // 获得SurfaceView的SurfaceHolder
            surfaceHolder = sView.getHolder();
            // 为surfaceHolder添加一个回调监听器
            surfaceHolder.addCallback(new Callback() {
                public void surfaceChanged(SurfaceHolder holder, int format,
                                           int width, int height) {
                }


                public void surfaceCreated(SurfaceHolder holder) {
                    // surface被创建时打开摄像头
                    initCamera();


                }


                // surface摧毁时释放摄像头
                public void surfaceDestroyed(SurfaceHolder holder) {
                    // 如果camera不为null ,释放摄像头
                    if (camera != null) {
                        // 7.结束程序时，调用Camera的StopPriview()结束取景预览，并调用release()方法释放资源.
                        if (isPreview)
                            camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                }
            });
            // 设置该SurfaceView自己不维护缓冲
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    private void initCamera() {
        if (!isPreview) {
            int cameraCount = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras(); // get cameras number

            if (cameraCount == 1) {
                // camera = Camera.open();// 调用Camera的open()方法打开相机。
                Log.e("TAG", "无前置摄像头");
            } else {
                Log.e("TAG", "__________4");
                for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                    Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                        try {
                            camera = Camera.open(camIdx);// 调用Camera的open()方法打开相机。
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (camera != null && !isPreview) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 开始预览
            camera.startPreview();
            isPreview = true;
        }
    }


    public PictureCallback myjpegCallback = new PictureCallback() {


        @SuppressLint("SimpleDateFormat")
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.e("TAG", "拍照成功");
            // 重置声音
            manager.setStreamVolume(AudioManager.STREAM_SYSTEM, volumn,
                    AudioManager.FLAG_ALLOW_RINGER_MODES);
            // 根据拍照所得的数据创建位图
            final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                    data.length);
            if (ExistSDCard()) {


                picPath = "camera_test_"
                        + new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date()) + ".jpg";


                File file = new File(picPath);
                FileOutputStream outStream = null;
                try {
                    // 打开指定文件对应的输出流
                    outStream = new FileOutputStream(file);
                    // 把位图输出到指定文件中
                    Matrix matrix = new Matrix();
                    Bitmap bm = Bitmap.createBitmap(1200,
                            1200 * bitmap.getHeight() / bitmap.getWidth(),
                            Config.ARGB_8888);// 固定所拍相片大小
                    matrix.setScale(
                            (float) bm.getWidth() / (float) bitmap.getWidth(),
                            (float) bm.getHeight() / (float) bitmap.getHeight());// 注意参数一定是float哦
                    Canvas canvas = new Canvas(bm);// 用bm创建一个画布
                    // 可以往bm中画入东西了
                    canvas.drawBitmap(bitmap, matrix, null);
                    bm.compress(CompressFormat.JPEG, 40, outStream);
                    outStream.close();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("TAG", "SD卡不可用");
            }


            camera.stopPreview();
            camera.startPreview();
            isPreview = true;


            context.cameraRefresh(picPath);


        }
    };


    public static boolean ExistSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }
}
