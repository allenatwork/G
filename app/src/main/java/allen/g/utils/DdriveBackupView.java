package allen.g.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import allen.g.R;

/**
 * Created by local on 15/12/2017.
 */

public class DdriveBackupView extends LinearLayout {
    ProgressBar progressBar;
    TextView textView;
    int oldProgress;

    public DdriveBackupView(Context context) {
        super(context);
        init(context);
    }

    public DdriveBackupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DdriveBackupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.backup_layout, this);
        progressBar = findViewById(R.id.progress);
        textView = findViewById(R.id.status);
    }

    public void showInitialState() {
        textView.setText("Chuẩn bị để backup media ...");
        progressBar.setIndeterminate(true);
    }

    public void updateProgress(final int progress) {
        if (progress > 100) return;
        textView.setText(String.format("Đang upload media lên Google Drive (%d %%)", progress));
        if (progressBar.isIndeterminate()) {
            progressBar.setIndeterminate(false);
        }

        ProgressBarAnimation anim = new ProgressBarAnimation(progressBar, oldProgress, progress);
        anim.setDuration(300);
        progressBar.startAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                oldProgress = progress;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void showDoneState(boolean isSuccess) {
        updateProgress(100);
        if (isSuccess) {
            textView.setText("Upload media lên Google Drive thành công");
        } else {
            textView.setText("Upload media lên Google Drive thất bại");
        }
    }

    private static class ProgressBarAnimation extends Animation {
        private ProgressBar progressBar;
        private float from;
        private float to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }

    }
}
