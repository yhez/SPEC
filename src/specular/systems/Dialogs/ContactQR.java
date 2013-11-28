package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import specular.systems.QRCodeEncoder;
import specular.systems.R;


public class ContactQR extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        TextView tv;
        String pb = ((TextView) getActivity().findViewById(R.id.contact_pb)).getText().toString();
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(pb, BarcodeFormat.QR_CODE.toString(), 512);
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
            tv.setText("failed to create QR");
            e.printStackTrace();
            builder.setView(tv);
        }

        //Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
}
