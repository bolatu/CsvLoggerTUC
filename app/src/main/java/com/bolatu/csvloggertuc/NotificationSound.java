package com.bolatu.csvloggertuc;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by bolatu on 4/20/16.
 */
public class NotificationSound {

    public Context mContext;
    private MediaPlayer mp = new MediaPlayer();

    public NotificationSound(Context context) {
        this.mContext = context;
    }

    public void playErrorSound() {
        if (mp.isPlaying()) {
            mp.stop();
        }

        try {
            mp.reset();
            AssetFileDescriptor afd;
            afd = mContext.getAssets().openFd("Error.mp3");
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.start();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    public void playSuccessSound() {
        if (mp.isPlaying()) {
            mp.stop();
        }

        try {
            mp.reset();
            AssetFileDescriptor afd;
            afd = mContext.getAssets().openFd("Cool.mp3");
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.start();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

    }

}