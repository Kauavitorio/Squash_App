package dev.kaua.squash.Tools;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.widget.TextView;

public class TypeWriter extends ContextWrapper {

    private CharSequence mText;
    private TextView text_view;
    private int mIndex;
    private long mDelay = 150; //Default 150ms delay


    public TypeWriter(Context context) {
        super(context);
    }

    private final Handler mHandler = new Handler();
    private final Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            if(text_view != null && getApplicationContext() != null){
                text_view.setText(mText.subSequence(0, mIndex++));
                if(mIndex <= mText.length()) {
                    mHandler.postDelayed(characterAdder, mDelay);
                }
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;

        text_view.setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }

    public void setTextView(TextView text) {
        text_view = text;
    }

    public void Cancel() {
        mHandler.removeCallbacks(characterAdder);
    }
}