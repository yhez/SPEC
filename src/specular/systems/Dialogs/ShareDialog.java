package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

import specular.systems.CryptMethods;
import specular.systems.FilesManagement;
import specular.systems.R;

public class ShareDialog extends DialogFragment {
    private ArrayList mSelectedItems;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        mSelectedItems = new ArrayList();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.what_to_share)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.choice, null,
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
                .setPositiveButton(R.string.share_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mSelectedItems.size() == 0) {
                            Toast.makeText(getActivity(), R.string.not_chosen, Toast.LENGTH_LONG).show();
                        } else {

                            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                            intent.setType("*/*");
                            intent.putExtra(Intent.EXTRA_SUBJECT,
                                    getResources().getString(R.string.subject_share));
                            try {
                                InputStream is = getActivity().getAssets().open("spec_temp_share.html");
                                int size = is.available();
                                byte[] buffer = new byte[size];
                                 is.read(buffer);
                                is.close();
                                intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer)));
                            } catch (Exception e) {
                                Toast.makeText(getActivity(), R.string.failed, Toast.LENGTH_LONG)
                                        .show();
                            }
                            ArrayList<Uri> file = new ArrayList<Uri>();
                            if (mSelectedItems.contains(Integer.valueOf(0))) {
                                file.add(FilesManagement.getFileToShare(getActivity()));
                            }
                            if (mSelectedItems.contains(Integer.valueOf(1))) {
                                file.add(FilesManagement.getQRToShare(getActivity()));
                            }
                            if (file.size() > 0) {
                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, file);
                            }
                            if (mSelectedItems.contains(Integer.valueOf(2))) {
                                intent.putExtra(Intent.EXTRA_TEXT, CryptMethods.getPublic());
                            }
                            startActivity(Intent.createChooser(intent, getResources()
                                    .getString(R.string.share_dialog)));
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //...
                dialog.cancel();
            }
        });

        return builder.create();
    }
}