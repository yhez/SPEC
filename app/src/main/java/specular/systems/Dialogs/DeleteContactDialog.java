package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.Group;
import specular.systems.GroupDataSource;
import specular.systems.R;
import specular.systems.StaticVariables;


public class DeleteContactDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (getActivity().findViewById(R.id.group_details).getVisibility() == View.VISIBLE) {
                    Group group = GroupDataSource.groupDataSource.findGroup(Long
                            .valueOf(((TextView) getActivity().findViewById(R.id.contact_id))
                                    .getText().toString()));
                    GroupDataSource.groupDataSource.deleteGroup(getActivity(), group);
                } else {
                    Contact contact = ContactsDataSource.contactsDataSource.findContact(Long
                            .valueOf(((TextView) getActivity().findViewById(R.id.contact_id))
                                    .getText().toString()));
                    ContactsDataSource.contactsDataSource.deleteContact(getActivity(), contact);
                    StaticVariables.luc.remove(getActivity(), contact);
                }
                getActivity().onBackPressed();
            }
        }

        )
                .

                        setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DeleteContactDialog.this.getDialog().cancel();
                            }
                        }

                        ).

                setMessage(R.string.verify_delete_msg);

        return builder.create();
    }
}