package com.baudiabatash.cameraapitest;

import android.media.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Genius 03 on 8/3/2017.
 */

public class ImageSaver implements Runnable {
    private final Image image;
    private File imageFile;

    private ImageSaver(Image image,File imageFile) {
        this.image = image;
        this.imageFile=imageFile;
    }
    @Override
    public void run() {

        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        byte[] bytes= new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);

        // Create a File Input Stream
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(imageFile.getPath());
            fileOutputStream.write(bytes);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            image.close();
            if(fileOutputStream!=null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
