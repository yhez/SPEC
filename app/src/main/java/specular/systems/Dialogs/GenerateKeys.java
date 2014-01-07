package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import specular.systems.FragmentManagement;
import specular.systems.R;
import specular.systems.Visual;
import specular.systems.activities.Main;


public class GenerateKeys extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.replace_keys, null);
        builder.setView(v);
        LinearLayout titles = (LinearLayout) v.findViewById(R.id.ll_titles);
        final View[] line = new View[3];
        final TextView tv = (TextView) v.findViewById(R.id.text_content);
        final CheckBox cb  = (CheckBox)v.findViewById(R.id.check_verify);
        for (int a = 0; a < 3; a++)
            line[a] = titles.getChildAt(a);
        final Button bt = (Button) v.findViewById(R.id.next);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (line[0].getVisibility() == View.VISIBLE) {
                    line[0].setVisibility(View.INVISIBLE);
                    line[1].setVisibility(View.VISIBLE);
                    tv.setText(R.string.second_text);
                    bt.setText(R.string.got_it);
                } else if (line[1].getVisibility() == View.VISIBLE) {
                    line[1].setVisibility(View.INVISIBLE);
                    line[2].setVisibility(View.VISIBLE);
                    tv.setText(R.string.third_text);
                    bt.setText(R.string.generate);
                    bt.setClickable(false);
                    cb.setVisibility(View.VISIBLE);
                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            bt.setClickable(b);
                        }
                    });
                } else {
                    FragmentManagement.currentLayout = R.layout.create_new_keys;
                    final Fragment fragment = new FragmentManagement();
                    final FragmentManager fragmentManager = Main.main.getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    GenerateKeys.this.getDialog().cancel();
                }
            }
        });
        v.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (line[0].getVisibility() == View.VISIBLE)
                    GenerateKeys.this.getDialog().cancel();
                else if (line[1].getVisibility() == View.VISIBLE) {
                    line[1].setVisibility(View.INVISIBLE);
                    line[0].setVisibility(View.VISIBLE);
                    tv.setText(R.string.first_text);
                    bt.setText(R.string.confirm_first);
                } else if (line[2].getVisibility() == View.VISIBLE) {
                    line[2].setVisibility(View.INVISIBLE);
                    line[1].setVisibility(View.VISIBLE);
                    tv.setText(R.string.second_text);
                    bt.setText(R.string.got_it);
                    bt.setClickable(true);
                    cb.setVisibility(View.GONE);
                }
            }
        });
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
}
