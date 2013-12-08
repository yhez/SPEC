package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import specular.systems.Contact;
import specular.systems.CryptMethods;
import specular.systems.FilesManagement;
import specular.systems.MessageFormat;
import specular.systems.PublicStaticVariables;
import specular.systems.R;


public class Response extends DialogFragment {
    Contact contact;

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
        if (PublicStaticVariables.currentLayout == R.layout.decrypted_msg) {
            if (PublicStaticVariables.friendsPublicKey != null) {
                contact = PublicStaticVariables.contactsDataSource.findContactByKey(PublicStaticVariables.friendsPublicKey);
                cb.setVisibility(View.VISIBLE);
                cb.setChecked(true);
            }
        } else if (PublicStaticVariables.currentLayout == R.layout.edit_contact) {
            contact = PublicStaticVariables.contactsDataSource.findContact(Long.parseLong(
                    ((TextView) getActivity().findViewById(R.id.contact_id)).getText().toString()));
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
                    tv.setText(PublicStaticVariables.MSG_LIMIT_FOR_QR - editable.length() > 0 ? PublicStaticVariables.MSG_LIMIT_FOR_QR - editable.length() + "" : getString(R.string.no_qr));
                }
            }
        });
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PublicStaticVariables.currentLayout == R.layout.decrypted_msg)
                    getActivity().findViewById(R.id.answer).setVisibility(View.GONE);
                final String userInput = et.getText().toString();
                String msgContent;
                if (cb.isChecked())
                    if (PublicStaticVariables.msg_content.contains(getString(R.string.quote_msg)))
                        msgContent = getString(R.string.divide_msg)
                                + getString(R.string.quote_msg)
                                + getString(R.string.divide_msg)
                                + PublicStaticVariables.msg_content
                                .replace(getString(R.string.quote_msg)
                                        + getString(R.string.divide_msg), "");

                    else
                        msgContent = getString(R.string.divide_msg)
                                + getString(R.string.quote_msg)
                                + getString(R.string.divide_msg)
                                + PublicStaticVariables.msg_content;

                else
                    msgContent = "";
                final MessageFormat msg = new MessageFormat(null, "", userInput + msgContent
                        , contact.getSession());
                final ProgressDlg prgd = new ProgressDlg(getActivity(), R.string.encrypting);
                prgd.setCancelable(false);
                prgd.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CryptMethods.encrypt(msg.getFormatedMsg(),
                                contact.getPublicKey());
                        boolean success = FilesManagement.createFilesToSend(getActivity(), userInput.length() < PublicStaticVariables.MSG_LIMIT_FOR_QR);
                        if (success) {
                            ArrayList<Uri> files = FilesManagement.getFilesToSend(getActivity());
                            if (files == null)
                                Toast.makeText(getActivity(), R.string.failed_attach_files, Toast.LENGTH_LONG).show();
                            else {
                                SendMsgDialog sendMsgDialog = new SendMsgDialog(files, contact.getEmail());
                                sendMsgDialog.show(getFragmentManager(), "smd");
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.failed_to_create_files_to_send, Toast.LENGTH_LONG).show();
                        }
                        prgd.cancel();
                        Response.this.getDialog().cancel();
                    }
                }).start();
            }
        });
        return builder.create();
    }
}
