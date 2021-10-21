package dev.kaua.squash.Tools;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import dev.kaua.squash.Activities.MessageActivity;
import dev.kaua.squash.Adapters.Chat.ViewProxy;

public class AudioRecord extends MessageActivity {
    private static final boolean DEBUG = false;

    public static boolean RecordAudio(MotionEvent motionEvent, View view){
        if(!recording) btn_rec_audio.startAnimation(myAnim);
        UserPermissions.validatePermissions(PERMISSION_audio, instance, REQUEST_RECORD_AUDIO_PERMISSION);
        final int RECORD_PERMISSION = ContextCompat.checkSelfPermission(instance, Manifest.permission.RECORD_AUDIO);
        if (RECORD_PERMISSION == PackageManager.PERMISSION_GRANTED) {

            x2 = motionEvent.getX();
            float deltaX = x2 - x1;
            if(DEBUG){
                Log.d(TAG, "MIN -> " + MIN_DISTANCE);
                Log.d(TAG, "CURRENT -> " + deltaX);
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if(!recording){
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                            .getLayoutParams();
                    params.leftMargin = dp(30);
                    slideText.setLayoutParams(params);
                    ViewProxy.setAlpha(slideText, 1);
                    startedDraggingX = -1;
                    // startRecording();
                    if(MessageActivity.getInstance() != null) MessageActivity.getInstance().StartRecord();
                    btn_rec_audio.getParent()
                            .requestDisallowInterceptTouchEvent(true);
                }
            } else if (Math.abs(deltaX) >= MIN_DISTANCE && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if(recording){
                    startedDraggingX = -1;
                    if(DEBUG) Log.d(TAG, "CANCEL ACTION");
                    if(MessageActivity.getInstance() != null) MessageActivity.getInstance().StopRecord(true);
                    // stopRecording(true);
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                float x = motionEvent.getX();
                    /*if (x < -distCanMove) {
                        StopRecord(false);
                        // stopRecording(false);
                    }*/
                x = x + ViewProxy.getX(btn_rec_audio);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                        .getLayoutParams();
                if (startedDraggingX != -1) {
                    final float dist = (x - startedDraggingX);
                    params.leftMargin = dp(120) + (int) dist;
                    slideText.setLayoutParams(params);
                    float alpha = 1.0f + dist / distCanMove;
                    if (alpha > 1) alpha = 1;
                    else if (alpha < 0)
                        alpha = 0;
                    ViewProxy.setAlpha(slideText, alpha);
                }
                if (x <= ViewProxy.getX(slideText) + slideText.getWidth()
                        + dp(30)) {
                    if (startedDraggingX == -1) {
                        startedDraggingX = x;
                        distCanMove = (recordPanel.getMeasuredWidth()
                                - slideText.getMeasuredWidth() - dp(48)) / 2.0f;
                        if (distCanMove <= 0)
                            distCanMove = dp(80);
                        else if (distCanMove > dp(80))
                            distCanMove = dp(80);
                    }
                }
                if (params.leftMargin > dp(30)) {
                    params.leftMargin = dp(30);
                    slideText.setLayoutParams(params);
                    ViewProxy.setAlpha(slideText, 1);
                    startedDraggingX = -1;
                }
            }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                if(recording){
                    startedDraggingX = -1;
                    if(DEBUG) Log.d(TAG, "SENT ACTION");
                    if(MessageActivity.getInstance() != null) MessageActivity.getInstance().StopRecord(false);
                }
            }
            view.onTouchEvent(motionEvent);
            return true;
        }
        return true;
    }
}
