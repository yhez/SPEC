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
import specular.systems.Group;
import specular.systems.GroupDataSource;
import specular.systems.MessageFormat;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.Visual;
import specular.systems.activities.SendMsg;


public class Response extends DialogFragment {
    Contact contact;
    Group group;
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
            if (getActivity().findViewById(R.id.group_details).getVisibility() == View.VISIBLE) {
                group = GroupDataSource.groupDataSource.findGroup(Long
                        .valueOf(((TextView) getActivity().findViewById(R.id.contact_id))
                                .getText().toString()));
            }else{
                contact = ContactsDataSource.contactsDataSource.findContact(Long.parseLong(
                        ((TextView) getActivity().findViewById(R.id.contact_id)).getText().toString()));
            }
        } else if (FragmentManagement.currentLayout == R.layout.decrypted_msg) {
            contact = ContactsDataSource.contactsDataSource.findContactByKey(StaticVariables.friendsPublicKey);
            if (StaticVariables.msg_content != null && StaticVariables.msg_content.length() > 0) {
                cb.setVisibility(View.VISIBLE);
                cb.setChecked(true);
            }
        }
        final TextView tv = (TextView) v.findViewById(R.id.text_counter);
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
                    tv.setVisibility(View.GONE);
                } else {
                    bt.setEnabled(true);
                    bt.setImageResource(R.drawable.ic_send_holo_light);
                    tv.setVisibility(View.VISIBLE);
                    tv.setText(StaticVariables.MSG_LIMIT_FOR_QR - editable.length() > 0 ? StaticVariables.MSG_LIMIT_FOR_QR - editable.length() + "" : getString(R.string.no_qr));
                }
            }
        });
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (FragmentManagement.currentLayout == R.layout.decrypted_msg)
                //    getActivity().findViewById(R.id.answer).setVisibility(View.GONE);
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
                String sss = contact!=null?contact.getSession().substring(0,contact.getSession().length()-2):group.getMentor();
                final MessageFormat msg = new MessageFormat(null, CryptMethods.getMyDetails(getActivity()), "", userInput + msgContent
                        , sss);
                final ProgressDlg prgd = new ProgressDlg(getActivity(), R.string.encrypting);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] data = Visual.bin2hex(CryptMethods.encrypt(msg.getFormatedMsg(),
                                contact!=null?contact.getPublicKey():group.getPublicKey())).getBytes();
                        boolean success = FilesManagement.createFilesToSend(getActivity(), userInput.length() < StaticVariables.MSG_LIMIT_FOR_QR, data);
                        prgd.cancel();
                        if (success) {
                            Intent intent = new Intent(getActivity(), SendMsg.class);
                            if(FragmentManagement.currentLayout==R.layout.decrypted_msg
                                    ||getActivity().findViewById(R.id.group_details).getVisibility()==View.GONE)
                                intent.putExtra("type",SendMsg.MESSAGE);
                            else
                                intent.putExtra("type",SendMsg.MESSAGE_FOR_GROUP);
                            intent.putExtra("contactId", contact!=null?contact.getId():group.getId());
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
