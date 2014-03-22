package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import specular.systems.Group;
import specular.systems.GroupDataSource;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.Visual;
import specular.systems.activities.Main;


public class DialogAddGroup extends DialogFragment {
    public DialogAddGroup(FragmentManager fm){
        show(fm,"");
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        builder.setTitle(getString(R.string.join_group)).setMessage(getString(R.string.add_group_explain))
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DialogAddGroup.this.getDialog().cancel();
                    }
                }).setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Group g = new Group(StaticVariables.decryptedGroup);
                Group d = GroupDataSource.groupDataSource.findGroupByPublic(g.getPublicKey());
                if(d!=null){
                    Main.main.contactChosen(false, d.getId());
                    Visual.toast(getActivity(), R.string.groups_same_name_exist);
                }else
                    new Group(getActivity(),g);
            }
        });
        return builder.create();
    }
}


