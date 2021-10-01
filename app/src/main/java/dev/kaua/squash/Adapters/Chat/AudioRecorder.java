package dev.kaua.squash.Adapters.Chat;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;


public class AudioRecorder {

    MediaRecorder recorder;
    public final String path;

    public AudioRecorder(String path) {
        Log.d("RECORD_AUDIO", "PATCH -> " + path);
        this.path = sanitizePath(path);
    }

    private String sanitizePath(String path) {
        if (!path.startsWith("/")) path = "/" + path;
        if (!path.contains(".")) path += ".3gp";
        return /*Environment.getExternalStorageDirectory().getAbsolutePath()
                + */path;
    }

    public void start() throws IOException {
        String state = android.os.Environment.getExternalStorageState();
        if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
            throw new IOException("SD Card is not mounted.  It is " + state
                    + ".");
        }

        // make sure the directory we plan to store the recording in exists
        File directory = new File(path).getParentFile();
        if (directory == null || !directory.exists() && !directory.mkdirs())
            throw new IOException("Path to file could not be created.");

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(path);
        recorder.prepare();
        recorder.start();
    }

    public String stop() {
        recorder.stop();
        recorder.release();
        return path;
    }

    public void playarcoding(String path) throws IOException {
        MediaPlayer mp = new MediaPlayer();
        mp.setDataSource(path);
        mp.prepare();
        mp.start();
        mp.setVolume(10, 10);
    }
}
