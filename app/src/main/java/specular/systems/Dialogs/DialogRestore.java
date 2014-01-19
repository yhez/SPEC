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
import specular.systems.Group;
import specular.systems.R;


public class DialogRestore extends DialogFragment {
    private ArrayList<Contact> cn;

    public DialogRestore(ArrayList<Contact> cn, ArrayList<Group> gr) {
        this.cn = cn;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<Boolean> checkedItems = new ArrayList<Boolean>();
        checkedItems.add(false);checkedItems.add(false);checkedItems.add(false);
        final ArrayList<Contact> noDbl = Backup.cleanList(cn, 0);
        final ArrayList<Contact> cleanEmail = Backup.cleanList(cn, 1);
        final ArrayList<Contact> cleanPublic = Backup.cleanList(cn, 2);
        final ArrayList<Contact> cleanBoth = Backup.cleanList(cn, 3);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        builder.setTitle("restore from backup?\nfound "+noDbl.size()).
                //setMessage("would you like to restore missing contacts from this backup file?\nfount " + cn.size() + " missing contacts.")
                setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DialogRestore.this.getDialog().cancel();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int count = 0;
                if (Backup.getConflicts() == 0) {
                    for (Contact c : cn) {
                        count++;
                        new Contact(getActivity(), c);
                    }
                } else {
                    if (cleanEmail.size() > 0 && checkedItems.get(0)) {
                        for (Contact c : cleanEmail) {
                            count++;
                            new Contact(getActivity(), c);
                        }
                    }
                    if (cleanPublic.size() > 0 && checkedItems.get(1)) {
                        for (Contact c : cleanPublic) {
                            count++;
                            new Contact(getActivity(), c);
                        }
                   }
                    if (cleanBoth.size() > 0 && checkedItems.get(2)) {
                        for (Contact c : cleanBoth) {
                            count++;
                            new Contact(getActivity(), c);
                        }
                    }
                }

                Toast t = Toast.makeText(getActivity(), count + " contacts has added to your list", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        }).setMultiChoiceItems(new CharSequence[]{"email found " + cleanEmail.size(), "key found " + cleanPublic.size(), "both found " + cleanBoth.size()},
                null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedItems.set(which, isChecked);
            }
        });
        return builder.create();
    }
}

