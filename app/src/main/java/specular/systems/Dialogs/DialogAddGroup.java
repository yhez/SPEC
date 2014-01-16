package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import specular.systems.Group;
import specular.systems.GroupDataSource;
import specular.systems.R;
import specular.systems.StaticVariables;


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
                Group g = new Group(StaticVariables.decryptedGroup);
                Group d = GroupDataSource.groupDataSource.findGroupByPublic(g.getPublicKey());
                if(d!=null){
                    Toast t = Toast.makeText(getActivity(),"group with the same session already exist",Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER,0,0);
                    t.show();
                }else
                    new Group(getActivity(),g);
            }
        });
        return builder.create();
    }
}


