package specular.systems.highlight;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import specular.systems.R;

/**
 * Created by yehezkelk on 11/29/13.
 */
public class CustomToast {
    public CustomToast(Activity a, View target,String title) {
        Toast t = Toast.makeText(a, "", Toast.LENGTH_LONG);
        LayoutInflater inflater1 = a.getLayoutInflater();
        FrameLayout v = (FrameLayout) inflater1.inflate(R.layout.toast_highlight, null);
        ImageView imageView1 = new ImageView(a);
        imageView1.setImageResource(R.drawable.cling);
        int[] coordinates = new int[2];
        int size =Math.max(target.getWidth(), target.getHeight());
        target.getLocationInWindow(coordinates);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) (size * 1.5), (int) (size * 1.5));
        layoutParams.setMargins(coordinates[0]-size/2, coordinates[1]-size/2,0,0);
        Log.d("size", target.getWidth() + "-" + target.getHeight());
        imageView1.setLayoutParams(layoutParams);
        v.addView(imageView1);
        Log.d("loc", imageView1.getX() + "-" + imageView1.getY());
        t.setView(v);
        t.show();
    }
}
