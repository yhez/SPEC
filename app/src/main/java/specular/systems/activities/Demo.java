package specular.systems.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import specular.systems.Dialogs.NotImplemented;
import specular.systems.R;


public class Demo extends Activity {
    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.demo);
    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.return_to_spec:
                finish();
                break;
            case R.id.demo_bank:
                ImageView iv = new ImageView(this);
                iv.setImageResource(R.drawable.bank_demo);
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                setContentView(iv);
                break;
            case R.id.safe:
                NotImplemented ni = new NotImplemented();
                ni.show(getFragmentManager(),"hfhf");
                break;
        }
    }
}
