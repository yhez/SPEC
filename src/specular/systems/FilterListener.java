package specular.systems;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by yehezkelk on 11/11/13.
 */
public class FilterListener {
    private EditText et;
    public FilterListener(EditText et){
        this.et=et;
    }
    public void start(){
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
