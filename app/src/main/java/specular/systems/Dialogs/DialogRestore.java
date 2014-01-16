package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;

import specular.systems.Backup;
import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.R;
import specular.systems.StaticVariables;


public class DialogRestore extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        builder.setTitle("restore from backup").setMessage("would you like to restore missing contacts from this backup file?")
                .setNegativeButton("NO",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DialogRestore.this.getDialog().cancel();
                    }
                }).setPositiveButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ArrayList<Contact> lst = Backup.restore(StaticVariables.decryptedBackup);
                int count =0;
                for(Contact c:lst){
                    if(null==ContactsDataSource.contactsDataSource.findContactByKey(c.getPublicKey())){
                        new Contact(getActivity(),c);
                        count++;
                    }
                }
                Toast t = Toast.makeText(getActivity(),count +" contacts has added to your list",Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER,0,0);
                t.show();
            }
        });
        return builder.create();
    }
}

