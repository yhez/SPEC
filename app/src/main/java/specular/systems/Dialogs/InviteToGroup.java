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
import specular.systems.ContactsDataSource;
import specular.systems.CryptMethods;
import specular.systems.FilesManagement;
import specular.systems.Group;
import specular.systems.MySimpleArrayAdapter;
import specular.systems.R;
import specular.systems.Visual;
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
        MySimpleArrayAdapter my = MySimpleArrayAdapter.getAdapter();
        if(my!=null){
            my.setFlag(MySimpleArrayAdapter.SIMPLE);
        }else{
            my = new MySimpleArrayAdapter(getActivity());
            my.setFlag(MySimpleArrayAdapter.SIMPLE);
        }
        final MySimpleArrayAdapter myy = my;
        lv.setAdapter(myy);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Contact c = ContactsDataSource.fullList.get(i);
                byte[] b = CryptMethods.encrypt(g.getGroupToShare(), c.getPublicKey());
                byte[] bb = Visual.bin2hex(b).getBytes();
                FilesManagement.createGroupFileToSend(getActivity(),bb);
                myy.setFlag(MySimpleArrayAdapter.EDIT);
                InviteToGroup.this.getDialog().cancel();
                Intent intent = new Intent(getActivity(), SendMsg.class);
                intent.putExtra("type",SendMsg.INVITE_GROUP);
                intent.putExtra("contactId", c.getId());
                startActivity(intent);
            }
        });
        builder.setView(lv);
        return builder.create();
    }
}
