package specular.systems;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class AddContactDlg extends DialogFragment {
    PublicContactCard pcc;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        pcc=Splash.fileContactCard;
        Splash.fileContactCard=null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.add_contact_dlg, null);
        builder.setView(v)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        new Contact(getActivity(),pcc.getName(),pcc.getEmail(),pcc.getPublicKey());
                        if(Main.currentLayout!=R.layout.decrypted_msg){
                            Main.changed=true;
                            getActivity().onBackPressed();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddContactDlg.this.getDialog().cancel();
                    }
                });
        ((TextView)v.findViewById(R.id.acd_chosen_name)).setText(pcc.getName());
        ((TextView)v.findViewById(R.id.acd_chosen_email)).setText(pcc.getEmail());
        ((ImageView)v.findViewById(R.id.acd_chosen_icon)).setImageBitmap(Contact.getPhoto(pcc.getPublicKey()));
        return builder.create();
    }
}
