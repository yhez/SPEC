package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import specular.systems.R;


public class DialogAddGroup extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        builder.setTitle("Join to The Group").setMessage("would you like to add this group?\nIt'll give you the option to decrypt messages that was sent to the group")
                .setNegativeButton("NO",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DialogAddGroup.this.getDialog().cancel();
                    }
                }).setPositiveButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                NotImplemented ni = new NotImplemented();
                ni.show(getFragmentManager(),"ni");
            }
        });
        return builder.create();
    }
}


