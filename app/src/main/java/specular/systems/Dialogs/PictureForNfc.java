package specular.systems.Dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import specular.systems.FilesManagement;
import specular.systems.R;

public class PictureForNfc extends DialogFragment {
    public PictureForNfc(FragmentManager fm) {
        show(fm, "");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                startActivityForResult(FilesManagement.id_picture.createIntent(getActivity()),0);
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).setTitle(R.string.secret_picture_title).setMessage(R.string.take_picture_explain);
        return builder.create();
    }
}
