package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import specular.systems.R;
import zxing.QRCodeEncoder;
import zxing.WriterException;


public class ContactQR extends DialogFragment {

    public ContactQR(FragmentManager fm){
        show(fm,"");
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        TextView tv;
        String pb = ((TextView) getActivity().findViewById(R.id.contact_pb)).getText().toString();
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(pb,  512);
        Bitmap bitmap;
        try {
            bitmap = qrCodeEncoder.encodeAsBitmap();
            ImageView iv = new ImageView(getActivity());
            iv.setImageBitmap(bitmap);
            iv.setAdjustViewBounds(true);
            iv.setFitsSystemWindows(true);
            builder.setView(iv);
        } catch (WriterException e) {
            tv = new TextView(getActivity());
            tv.setText(R.string.failed_create_qr);
            e.printStackTrace();
            builder.setView(tv);
        }
        return builder.create();
    }
}
