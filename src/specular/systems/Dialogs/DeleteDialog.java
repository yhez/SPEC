package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import specular.systems.Contact;
import specular.systems.Main;
import specular.systems.R;


public class DeleteDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Contact contact = Main.contactsDataSource.findContact(Long
                        .valueOf(((TextView) getActivity().findViewById(R.id.contact_id))
                                .getText().toString()));
                Main.contactsDataSource.deleteContact(contact);
                getActivity().onBackPressed();
            }
        })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DeleteDialog.this.getDialog().cancel();
                    }
                }).setMessage(R.string.verify_delete_msg);
        return builder.create();
    }
}