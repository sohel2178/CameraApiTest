package com.baudiabatash.cameraapitest;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUIRED_PERMISSION = 2000;
    private TextureView textureView;
    private Button btnTakePhoto;

    private Size previewSize;
    private String cameraId;

    private TextureView.SurfaceTextureListener surfaceTextureListener
            = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //Setup the Camera when surface Textture is Available
            Log.d("HHH","onSurfaceTextureAvailable Called");
            setupCamera(width,height);
            openCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };



    private CameraDevice cameraDevice;
    private CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice=camera;
                    createCameraPreview();

                    //Toast.makeText(MainActivity.this, "I am in State Callback", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice=null;

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice=null;

                }
            };

    private CaptureRequest previewCaptureRequest;
    private CaptureRequest.Builder previewCaptureRequestBuilder;

    private CameraCaptureSession cameraCaptureSession;
    private CameraCaptureSession.CaptureCallback cameraCaptureSessionCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }
            };

    private void createCameraPreview() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(),previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            //CaptureRequestBuilder is initialize here
            previewCaptureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewCaptureRequestBuilder.addTarget(previewSurface);
            //Create a Camera Sesson Here
            cameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if(cameraDevice==null){
                                return;
                            }

                            try{
                                previewCaptureRequest=previewCaptureRequestBuilder.build();
                                cameraCaptureSession=session;
                                cameraCaptureSession.setRepeatingRequest(previewCaptureRequest,
                                        cameraCaptureSessionCallback,null);

                            }catch (CameraAccessException e){
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                            Toast.makeText(MainActivity.this, "create Camera Session Failed", Toast.LENGTH_SHORT).show();

                        }
                    },null);

        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            for(String cameraId:cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                        == CameraMetadata.LENS_FACING_FRONT){
                    continue;
                }

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                //set Camera Preview
                previewSize = getPreviewSize(map.getOutputSizes(SurfaceTexture.class),width,height);
                //set the Camera Id
                this.cameraId=cameraId;
                Log.d("HHH",cameraId);
                return;

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getPreviewSize(Size[] mapSizes,int width,int height){
        List<Size> collectorSizes = new ArrayList<>();

        for(Size x: mapSizes){
            if(width>height){
                if(x.getWidth()>width && x.getHeight()>height){
                    collectorSizes.add(x);

                }
            }else {
                if(x.getWidth()>height && x.getHeight()>width){
                    collectorSizes.add(x);
                }
            }
        }

        if(collectorSizes.size()>0){
            return Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return Long.signum(o1.getWidth()*o1.getHeight()-o2.getWidth()*o2.getHeight());
                }
            });
        }

        return mapSizes[0];

    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            Log.d("HHH","I am in openCamera");
            //noinspection MissingPermission
            cameraManager.openCamera(cameraId,stateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requiredPermission();
    }

    @AfterPermissionGranted(REQUIRED_PERMISSION)
    private void requiredPermission() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
            if(textureView.isAvailable()){

            }else {
                textureView.setSurfaceTextureListener(surfaceTextureListener);
            }

        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "App need to Permission for Location",
                    REQUIRED_PERMISSION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initView() {
        textureView = (TextureView) findViewById(R.id.texture_view);
        btnTakePhoto = (Button) findViewById(R.id.take_photo);
        btnTakePhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
