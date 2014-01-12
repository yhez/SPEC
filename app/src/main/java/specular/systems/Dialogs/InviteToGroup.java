package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import specular.systems.Contact;
import specular.systems.CryptMethods;
import specular.systems.FilesManagement;
import specular.systems.Group;
import specular.systems.MySimpleArrayAdapter;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.activities.SendMsg;


public class InviteToGroup extends DialogFragment {
    private Group g;
    public InviteToGroup(Group g){
        this.g=g;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        ListView lv = new ListView(getActivity());
        lv.setAdapter(new MySimpleArrayAdapter(getActivity(),MySimpleArrayAdapter.SIMPLE));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Contact c = StaticVariables.fullList.get(i);
                byte[] b = CryptMethods.encrypyt(g.getLightGroupToShare(),c.getPublicKey());
                FilesManagement.createFilesToSend(getActivity(),false,b);
                InviteToGroup.this.getDialog().cancel();
                Intent intent = new Intent(getActivity(), SendMsg.class);
                intent.putExtra("contactId", c.getId());
                startActivity(intent);
            }
        });
        builder.setView(lv);
        return builder.create();
    }
}
