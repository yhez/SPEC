package specular.systems.Dialogs;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import specular.systems.FilesManagement;
import specular.systems.R;

public class ProgressDlg extends ProgressDialog {
    protected boolean mAnimationHasEnded = false;
    HoloCircularProgressBar hcpb;
    TextView textViewSec, textViewDec, textView;
    long startTimeMillis;
    int title;
    Activity activity;
    private ObjectAnimator mProgressBarAnimator;
    private Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int dec = (int) ((System.currentTimeMillis() - startTimeMillis) / 100);
            int sec = dec / 10;
            textViewSec.setText(sec + "");
            textViewDec.setText((dec % 10) + "");
        }
    };

    public ProgressDlg(Activity activity, int title) {
        super(activity);
        this.activity = activity;
        this.title = title;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.circular);
        hcpb = (HoloCircularProgressBar) findViewById(R.id.circle);
        animate(hcpb, new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(final Animator animation) {
                animation.end();
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                if (!mAnimationHasEnded) {
                    animate(hcpb, this);
                } else {
                    mAnimationHasEnded = false;
                }
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

            @Override
            public void onAnimationStart(final Animator animation) {
            }
        });

        textView = (TextView) findViewById(R.id.title_progress_bar);
        textView.setText(title);
        textView.setTypeface(FilesManagement.getOs(activity));
        textViewSec = (TextView) findViewById(R.id.sec_progress);
        textViewSec.setTypeface(FilesManagement.getOs(activity));
        textViewDec = (TextView) findViewById(R.id.dec_progress);
        textViewDec.setTypeface(FilesManagement.getOs(activity));
        startTimeMillis = System.currentTimeMillis();
        animateText();
    }

    private void animateText() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mProgressBarAnimator.isRunning()) {
                    synchronized (this) {
                        hndl.sendEmptyMessage(0);
                        try {
                            wait(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private void animate(final HoloCircularProgressBar progressBar, final Animator.AnimatorListener listener) {
        final float progress = (float) (Math.random() * 2);
        int duration = 1500;
        animate(progressBar, listener, progress, duration);
    }

    private void animate(final HoloCircularProgressBar progressBar, final Animator.AnimatorListener listener,
                         final float progress, final int duration) {

        mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
        mProgressBarAnimator.setDuration(duration);

        mProgressBarAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(final Animator animation) {
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                progressBar.setProgress(progress);
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

            @Override
            public void onAnimationStart(final Animator animation) {
            }
        });
        if (listener != null) {
            mProgressBarAnimator.addListener(listener);
        }
        mProgressBarAnimator.reverse();
        mProgressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                progressBar.setProgress((Float) animation.getAnimatedValue());
            }
        });
        progressBar.setMarkerProgress(progress);
        mProgressBarAnimator.start();
    }
}
