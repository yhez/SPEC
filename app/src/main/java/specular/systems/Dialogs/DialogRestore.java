package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.CryptMethods;
import specular.systems.FilesManagement;
import specular.systems.Group;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.activities.Main;


public class DialogRestore extends DialogFragment {
    private ArrayList<Contact> cn;
    String myDetails[];

    public DialogRestore(ArrayList<Contact> cn, ArrayList<Group> gr) {
        this.cn = cn;
        Contact me = cn.get(0);
        myDetails = new String[]{me.getContactName(),me.getEmail(),me.getPublicKey()};
        cn.remove(0);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        builder.setTitle("restore from backup?\nfound " + cn.size()+" contacts.").
                //setMessage("would you like to restore missing contacts from this backup file?\nfount " + cn.size() + " missing contacts.")
                        setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DialogRestore.this.getDialog().cancel();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ContactsDataSource.contactsDataSource.deleteAllContact(getActivity());
                for(Contact c:cn)
                    new Contact(getActivity(),c);
                CryptMethods.setDetails(myDetails[0],myDetails[1]);
                CryptMethods.setPublic(myDetails[2]);
                StaticVariables.NFCMode=true;
                FilesManagement.save(getActivity());
                FilesManagement.savePrivate(getActivity());
                Toast t = Toast.makeText(getActivity(),"all data has been restored", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                getActivity().finish();
                Intent in = new Intent(getActivity(), Main.class);
                startActivity(in);
            }
        }).setMessage("Are you sure?\nby restoring your backup all other data you've saved will be lost");
        return builder.create();
    }
}

