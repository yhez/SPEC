package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import specular.systems.R;


public class ExitWithoutSave extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                getActivity().findViewById(R.id.save).callOnClick();
            }
        })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((EditText) getActivity().findViewById(R.id.contact_name))
                                .setText(((TextView) getActivity().findViewById(R.id.orig_name)).getText().toString());
                        ((EditText) getActivity().findViewById(R.id.contact_email))
                                .setText(((TextView) getActivity().findViewById(R.id.orig_eamil)).getText().toString());
                        getActivity().onBackPressed();
                    }
                }).setMessage(R.string.save_changes_back_pressed);
        return builder.create();
    }
}