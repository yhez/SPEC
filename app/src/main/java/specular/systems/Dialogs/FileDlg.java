package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.Visual;

public class FileDlg extends DialogFragment {
    String name;
    EditText et;
    ImageButton ib;
    CheckBox mCheckBox;
    public FileDlg(FragmentManager fm,String fileName){
        name = fileName;
        show(fm,"");
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View v = inflater.inflate(R.layout.dlg_file, null);
        ((TextView)v.findViewById(R.id.text_view)).setText("name");
        et = (EditText)v.findViewById(R.id.edit_text);
        ib = (ImageButton)v.findViewById(R.id.image_button);
        mCheckBox = (CheckBox) v.findViewById(R.id.verify);
        if (StaticVariables.edit == null)
            StaticVariables.edit = et.getKeyListener();
        et.setText(name);
        et.setKeyListener(null);
        et.setFocusable(false);
        v.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckBox.isChecked()) {
                    //todo delete file from file contains all file from list and the file
                    new NotImplemented(getActivity().getFragmentManager());
                }else{
                    Visual.toast(getActivity(),R.string.verify_delete);
                }
            }
        });
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et.getKeyListener() == null) {
                    Visual.edit(getActivity(), et, ib);
                    et.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                    et.setSelection(et.length());
                } else {
                    Visual.edit(getActivity(), et, ib);
                }
            }
        });
        builder.setView(v);
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
    @Override
    public void onStart(){
        super.onStart();
        getDialog().getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        );
    }
}
