package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import specular.systems.Group;
import specular.systems.GroupDataSource;
import specular.systems.MySimpleArrayAdapter;
import specular.systems.R;
import specular.systems.Visual;


public class GroupCreate extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.group_def, null);
        final ListView lv = (ListView)v.findViewById(R.id.list);
        MySimpleArrayAdapter cl = new MySimpleArrayAdapter(getActivity(),MySimpleArrayAdapter.CHECKABLE);
        lv.setAdapter(cl);
        v.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Group.list == null) {
                    GroupDataSource gds = new GroupDataSource(getActivity());
                    // Group.list=gds.getAllContacts();
                    GroupCreate.this.getDialog().cancel();
                }
            }
        });
        builder.setView(v);
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
}
