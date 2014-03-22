package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import java.security.KeyPair;

import specular.systems.CryptMethods;
import specular.systems.Group;
import specular.systems.R;
import specular.systems.Visual;


public class GroupCreate extends DialogFragment {
    public GroupCreate(FragmentManager fm){
        show(fm,"");
    }
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
                KeyPair kp = CryptMethods.createKeysForGroup();
                String pub = Visual.bin2hex(kp.getPublic().getEncoded());
                byte[] pvt = kp.getPrivate().getEncoded();
                String name = ((EditText) v.findViewById(R.id.name)).getText().toString(),
                        email = ((EditText) v.findViewById(R.id.email)).getText().toString(),
                        session = ((EditText) v.findViewById(R.id.session)).getText().toString();
                if (name.length() > 2 && email.length() > 2 && session.length() > 2) {
                    new Group(getActivity(),
                            name, email, session,
                            pub, pvt, ((CheckBox) v.findViewById(R.id.force)).isChecked(),
                            ((CheckBox) v.findViewById(R.id.open)).isChecked());
                    GroupCreate.this.getDialog().cancel();
                } else {
                    Visual.toast(getActivity(), R.string.group_too_short_data);
                }
            }
        });
        builder.setView(v);
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
}
