package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.CryptMethods;
import specular.systems.FilesManagement;
import specular.systems.FragmentManagement;
import specular.systems.MessageFormat;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.Visual;
import specular.systems.activities.SendMsg;


public class Response extends DialogFragment {
    Contact contact;
    public Response(FragmentManager fm){
        show(fm,"");
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.response, null);
        builder.setView(v);
        final CheckBox cb = (CheckBox) v.findViewById(R.id.quote);
        if (FragmentManagement.currentLayout == R.layout.edit_contact) {
                contact = ContactsDataSource.contactsDataSource.findContact(Long.parseLong(
                        ((TextView) getActivity().findViewById(R.id.contact_id)).getText().toString()));
        } else if (FragmentManagement.currentLayout == R.layout.decrypted_msg) {
            contact = ContactsDataSource.contactsDataSource.findContactByKey(StaticVariables.friendsPublicKey);
            if (StaticVariables.msg_content != null && StaticVariables.msg_content.length() > 0) {
                cb.setVisibility(View.VISIBLE);
                cb.setChecked(true);
            }
        }
        final ImageButton bt = (ImageButton) v.findViewById(R.id.send);
        bt.setEnabled(false);
        final EditText et = (EditText) v.findViewById(R.id.message);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    bt.setEnabled(false);
                    bt.setImageResource(R.drawable.ic_send_disabled_holo_light);
                } else {
                    bt.setEnabled(true);
                    bt.setImageResource(R.drawable.ic_send_holo_light);
                }
            }
        });
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userInput = et.getText().toString();
                String msgContent;
                if (cb.isChecked())
                    if (StaticVariables.msg_content.contains(getString(R.string.quote_msg))) {
                        msgContent = getString(R.string.divide_msg)
                                + getString(R.string.quote_msg)
                                + getString(R.string.divide_msg)
                                + StaticVariables.msg_content
                                .replace(getString(R.string.quote_msg)
                                        + getString(R.string.divide_msg), "");
                    } else {
                        msgContent = getString(R.string.divide_msg)
                                + getString(R.string.quote_msg)
                                + getString(R.string.divide_msg)
                                + StaticVariables.msg_content;
                    }
                else {
                    msgContent = "";
                }
                String sss = contact.getSession().substring(0,contact.getSession().length()-2);
                final MessageFormat msg = new MessageFormat(null, CryptMethods.getMyDetails(getActivity()), "", userInput + msgContent
                        , sss);
                final ProgressDlg prgd = new ProgressDlg(getActivity(), R.string.encrypting);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] data = Visual.bin2hex(CryptMethods.encrypt(msg.getFormatedMsg(),
                                contact.getPublicKey())).getBytes();
                        boolean success = FilesManagement.createFilesToSend(getActivity(), data);
                        prgd.cancel();
                        if (success) {
                            Intent intent = new Intent(getActivity(), SendMsg.class);
                            intent.putExtra("type",SendMsg.MESSAGE);
                            intent.putExtra("contactId", contact.getId());
                            startActivity(intent);
                        } else {
                            Visual.toast(getActivity(),R.string.failed_to_create_files_to_send);
                        }
                        Response.this.getDialog().cancel();
                    }
                }).start();
            }
        });
        return builder.create();
    }
}
