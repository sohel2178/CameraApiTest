package com.baudiabatash.cameraapitest;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Genius 03 on 8/3/2017.
 */

public class CameraHandler {
    private Context context;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private CameraManager cameraManager;
    private ImageReader imageReader;
    private final ImageReader.OnImageAvailableListener imageAvailavleListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
           // backgroundHandler.post(new ImageSaver(reader.acquireNextImage()));

        }
    };

    public CameraHandler(Context context) {
        this.context = context;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void resume(){
        openBackgroundThread();

    }
    public void pause(){
        closeBackgroundThread();
    }


    private void setupFrontCamera(int width,int height){
        try {
            for(String cameraId:cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                        == CameraMetadata.LENS_FACING_BACK){
                    continue;
                }

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                //get Largest Image Size
                Size largestImageSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size o1, Size o2) {
                                return Long.signum(o1.getWidth()*o1.getHeight()-o2.getWidth()*o2.getHeight());
                            }
                        });

                // Initialize Image Reader Here
                imageReader = ImageReader.newInstance(largestImageSize.getWidth(),largestImageSize.getHeight(),
                        ImageFormat.JPEG,1);
                // Set the listener
                imageReader.setOnImageAvailableListener(imageAvailavleListener,backgroundHandler);
                //set Camera Preview
                //previewSize = getPreviewSize(map.getOutputSizes(SurfaceTexture.class),width,height);
                //set the Camera Id
                //this.cameraId=cameraId;
                Log.d("HHH",cameraId);
                return;

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void openBackgroundThread(){
        //init background Thread
        backgroundThread = new HandlerThread("Camera2Api Thread");
        backgroundThread.start();

        //init background Handler
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void closeBackgroundThread(){

        if(backgroundThread!=null){
            backgroundThread.quitSafely();

            try {
                backgroundThread.join();
                backgroundThread=null;
                backgroundHandler=null;

            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    }

}
