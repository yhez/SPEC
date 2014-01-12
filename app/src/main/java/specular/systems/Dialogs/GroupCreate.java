package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import specular.systems.CryptMethods;
import specular.systems.Group;
import specular.systems.R;
import specular.systems.Visual;


public class GroupCreate extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View v = inflater.inflate(R.layout.group_def, null);
        v.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CryptMethods.createKeys();
                String pub = CryptMethods.getPublicTmp();
                byte[] pvt = CryptMethods.getPrivateTmp();
                Group g = new Group(getActivity(),
                        ((EditText)v.findViewById(R.id.name)).getText().toString(),
                        ((EditText)v.findViewById(R.id.email)).getText().toString(),
                        ((EditText)v.findViewById(R.id.session)).getText().toString(),
                        pub,pvt,((CheckBox)v.findViewById(R.id.force)).isChecked(),
                        ((CheckBox)v.findViewById(R.id.open)).isChecked());
                byte[] b = g.getLightGroupToShare();
                GroupCreate.this.getDialog().cancel();
            }
        });
        builder.setView(v);
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
}
