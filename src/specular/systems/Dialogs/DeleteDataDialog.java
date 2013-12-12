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

import specular.systems.FilesManagement;
import specular.systems.R;
import specular.systems.activitys.Splash;

public class DeleteDataDialog extends DialogFragment {
    private ArrayList mSelectedItems;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Toast t = Toast.makeText(getActivity(),"",Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER_VERTICAL,0,0);
        // Use the Builder class for convenient dialog construction
        mSelectedItems = new ArrayList();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.clean_up)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.data_to_delete, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                        // Set the action buttons
                .setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mSelectedItems.size() == 0) {
                            t.setText(R.string.not_cosen_clean);t.show();
                        } else {
                            if (mSelectedItems.contains(Integer.valueOf(0))) {
                                FilesManagement.deleteTmp(getActivity());
                                t.setText(R.string.tmp_files_deleted);t.show();
                            }
                            if (mSelectedItems.contains(Integer.valueOf(1))) {
                                t.setText(R.string.keys_deleted);t.show();
                                FilesManagement.deleteKeys(getActivity());
                            }
                            if (mSelectedItems.contains(Integer.valueOf(2))) {
                                FilesManagement.deleteContacts(getActivity());
                                t.setText(R.string.contacts_deleted);t.show();
                            }
                            if (mSelectedItems.contains(Integer.valueOf(3))) {
                                NotImplemented ni = new NotImplemented();
                                ni.show(getFragmentManager(), "ni0");
                            }
                            getActivity().finish();
                            Intent i = new Intent(getActivity(), Splash.class);
                            startActivity(i);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }
}