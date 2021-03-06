package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import specular.systems.Contact;
import specular.systems.ContactCard;
import specular.systems.ContactsDataSource;
import specular.systems.FragmentManagement;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.Visual;
import specular.systems.activities.Main;
import zxing.QRCodeEncoder;
import zxing.WriterException;


public class AddContactDlg extends DialogFragment {
    private final ContactCard pcc;
    final long id;
    private final String session;

    public AddContactDlg(FragmentManager fm,ContactCard pcc, String session, long id) {
        StaticVariables.fileContactCard = null;
        this.pcc = pcc;
        this.id = id;
        this.session = session;
        show(fm,"");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.add_contact_dlg, null);
        if (!(id < 0)) {
            v.findViewById(R.id.check_box_update).setVisibility(View.VISIBLE);
        }
        builder.setView(v)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Contact c;
                        View vv = v.findViewById(R.id.check_box_update);
                        if (vv!=null&&((CheckBox) vv).isChecked()) {
                            c = ContactsDataSource.contactsDataSource.findContactByEmail(pcc.getEmail());
                            c.update(getActivity(), null, null, pcc.getPublicKey(), null);
                        } else
                            c = new Contact(getActivity(), pcc.getName(), pcc.getEmail(), pcc.getPublicKey(), session);
                        if (FragmentManagement.currentLayout == R.layout.decrypted_msg) {
                            ((TextView) getActivity().findViewById(R.id.flag_contact_exist)).setText(true + "");
                            getActivity().invalidateOptionsMenu();
                        }
                        if (FragmentManagement.currentLayout == R.layout.encrypt) {
                            Main.main.contactChosen(true,c.getId());
                        }
                        StaticVariables.fileContactCard=null;

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        StaticVariables.fileContactCard=null;
                        AddContactDlg.this.getDialog().cancel();
                    }
                });
        ((TextView) v.findViewById(R.id.acd_chosen_name)).setText(pcc.getName());
        ((TextView) v.findViewById(R.id.acd_chosen_email)).setText(pcc.getEmail());
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(pcc.getPublicKey(),  128);
        try {
            ((ImageView) v.findViewById(R.id.acd_chosen_icon)).setImageBitmap(qrCodeEncoder.encodeAsBitmap());
        } catch (WriterException e) {
            e.printStackTrace();
        }
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
}
