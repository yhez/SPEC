package specular.systems;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by yehezkelk on 10/28/13.
 */
public class ProgressDlg extends ProgressDialog {
    private ObjectAnimator mProgressBarAnimator;
    protected boolean mAnimationHasEnded = false;
    HoloCircularProgressBar hcpb;
    public ProgressDlg(Context context) {
        super(context);
    }
    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.circular);
        hcpb = (HoloCircularProgressBar)findViewById(R.id.circle);
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
    }
    private void animate(final HoloCircularProgressBar progressBar, final Animator.AnimatorListener listener) {
        final float progress = (float) (Math.random() * 2);
        int duration = 3000;
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
    }}
