package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.CryptMethods;
import specular.systems.FilesManagement;
import specular.systems.R;
import specular.systems.Visual;
import specular.systems.activities.Main;


public class DialogRestore extends DialogFragment {
    private ArrayList<Contact> cn;
    String myDetails[];

    public DialogRestore(FragmentManager fm,ArrayList<Contact> cn) {
        this.cn = cn;
        Contact me = cn.get(0);
        myDetails = new String[]{me.getContactName(),me.getEmail(),me.getPublicKey()};
        cn.remove(0);
        show(fm,"");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        if(cn!=null)
        builder.setTitle(R.string.backup_verified_title).
                        setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DialogRestore.this.getDialog().cancel();
                            }
                        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ContactsDataSource.contactsDataSource.deleteAllContact();
                for(Contact c:cn)
                    new Contact(getActivity(),c);
                CryptMethods.setDetails(myDetails[0],myDetails[1]);
                CryptMethods.setPublic(myDetails[2]);
                FilesManagement.save(getActivity(),true);
                if(FilesManagement.motNFCMod(getActivity()))
                    FilesManagement.savePrivate(getActivity());
                Visual.toast(getActivity(),R.string.notify_after_restore);
                getActivity().finish();
                Intent in = new Intent(getActivity(), Main.class);
                startActivity(in);
            }
        }).setMessage(ContactsDataSource.fullList!=null&&ContactsDataSource.fullList.size()!=0
                ?R.string.backup_found_old_data
                :R.string.backup_not_found_old_data);
        else
        builder.setTitle(R.string.not_verified_backup_title)
                .setMessage(R.string.not_verified_backup_message)
                .setCancelable(true);
        return builder.create();
    }
}

