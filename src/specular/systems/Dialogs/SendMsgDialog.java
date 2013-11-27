package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import specular.systems.PublicStaticVariables;
import specular.systems.QRCodeEncoder;
import specular.systems.R;
import specular.systems.Visual;


public class SendMsgDialog extends DialogFragment {
    private static final int FILE = 0, IMAGE = 1, BOTH = 2;
    private static List<ResolveInfo> file, image, both;
    ArrayList<Uri> uris;
    View v;
    String email;

    public SendMsgDialog(ArrayList<Uri> uris, String email) {
        this.uris = uris;
        this.email = email;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        v = inflater.inflate(R.layout.send_msg_dlg, null);
        final char[] dang = "|\\?*<\":>+[]/'".toCharArray();
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    for (char c : dang)
                        if (source.charAt(i) == c) {
                            return "";
                        }
                }
                return null;
            }
        };
        builder.setView(v);
        updateViews();
        if (uris.get(0) != null) {
            ((TextView) v.findViewById(R.id.file_size)).setText(getSize(uris.get(0)));
            ((ImageView) v.findViewById(R.id.file_icon)).setImageResource(R.drawable.logo);
            EditText etFile = (EditText) v.findViewById(R.id.name_file);
            etFile.setText(getName(FILE));
            etFile.setFilters(new InputFilter[]{filter});

        }
        if (uris.get(1) != null) {
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(PublicStaticVariables.encryptedMsgToSend,
                    BarcodeFormat.QR_CODE.toString(), 96);
            Bitmap bitmap;
            try {
                bitmap = qrCodeEncoder.encodeAsBitmap();
                ((ImageView) v.findViewById(R.id.qr_icon)).setImageBitmap(bitmap);
            } catch (WriterException e) {
                ((ImageView) v.findViewById(R.id.qr_icon)).setImageResource(R.drawable.logo);
                e.printStackTrace();
            }
            ((TextView) v.findViewById(R.id.qr_size)).setText(getSize(uris.get(1)));
            EditText etImage = (EditText) v.findViewById(R.id.qr_name_file);
            etImage.setText(getName(IMAGE));
            etImage.setFilters(new InputFilter[]{filter});
        }
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
    private String getSize(Uri uri){
        double size = new File(uri.getPath()).length();
        String unit = "byte";
        if (size > 1023) {
            size /= 1024;
            unit = "KB";
        }
        if (size > 1023) {
            size /= 1024;
            unit = "MB";
        }
        if (size > 1023) {
            size /= 1024;
            unit = "GB";
        }
        String total = (size + "").split("\\.")[0];
        if ((size + "").split("\\.").length > 0) {
            total += "." + (size + "").split("\\.")[1].substring(0, 2);
        }
        return total+"\n"+unit;
    }
    private void getApps(int a) {
        Intent intent;
        switch (a) {
            case FILE:
                if (file != null)
                    return;
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("file/*");
                file = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                return;
            case IMAGE:
                if (image != null)
                    return;
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("image/png");
                List<ResolveInfo> temp = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                intent.setAction(Intent.ACTION_SEND);
                image = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                int index = image.size() - 1;
                while (index > 0) {
                    String pn = image.get(index).activityInfo.packageName;
                    if (pn.equals(getActivity().getPackageName()))
                        image.remove(index);
                    else
                        for (ResolveInfo rt : temp)
                            if (rt.activityInfo.packageName.equals(pn)) {
                                image.remove(index);
                                break;
                            }
                    index--;
                }
                return;
            case BOTH:
                if (both != null)
                    return;
                if (image == null)
                    getApps(IMAGE);
                if (file == null)
                    getApps(FILE);
                intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("*/*");
                both = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                ArrayList<ResolveInfo> tmp = new ArrayList<ResolveInfo>();
                tmp.addAll(both);
                for (ResolveInfo ri : tmp) {
                    boolean b = false, d = false;
                    for (ResolveInfo rif : file)
                        if (ri.activityInfo.packageName.equals(rif.activityInfo.packageName)) {
                            b = true;
                            break;
                        }
                    for (ResolveInfo rif : image)
                        if (ri.activityInfo.packageName.equals(rif.activityInfo.packageName)) {
                            d = true;
                            break;
                        }
                    if (!(b && d))
                        both.remove(ri);
                }
        }
    }

    private void startOnClick(int what, ResolveInfo rs) {
        ComponentName cn;
        cn = new ComponentName(rs.activityInfo.packageName, rs.activityInfo.name);
        Intent i = new Intent();
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        i.setComponent(cn);
        i.putExtra(Intent.EXTRA_SUBJECT,
                getResources().getString(R.string.subject_encrypt));
        try {
            InputStream is = getActivity().getAssets().open("spec_tmp_msg.html");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer)));
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.failed, Toast.LENGTH_LONG)
                    .show();
        }
        if (what == IMAGE || what == BOTH) {
            File f = new File(uris.get(1).getPath());
            File newPath = new File(getActivity().getFilesDir(), ((EditText) v.findViewById(R.id.qr_name_file)).getText() + ".png");
            if (!f.equals(newPath)) {
                if (newPath.exists())
                    newPath.delete();
                f.renameTo(newPath);
                uris.set(1, Uri.parse("file://" + newPath));
            }
        }
        if (what == FILE || what == BOTH) {
            File f = new File(uris.get(0).getPath());
            File newPath = new File(getActivity().getFilesDir(), ((EditText) v.findViewById(R.id.name_file)).getText() + ".SPEC");
            if (!f.equals(newPath)) {
                if (newPath.exists())
                    newPath.delete();
                f.renameTo(newPath);
                uris.set(0, Uri.parse("file://" + newPath));
            }
        }
        if (what == FILE || what == IMAGE)
            i.setAction(Intent.ACTION_SEND);
        else
            i.setAction(Intent.ACTION_SEND_MULTIPLE);
        switch (what) {
            case FILE:
                i.setType("file/*");
                i.putExtra(Intent.EXTRA_STREAM, uris.get(0));
                break;
            case IMAGE:
                i.setType("image/png");
                i.putExtra(Intent.EXTRA_STREAM, uris.get(1));
                break;
            case BOTH:
                i.setType("*/*");
                i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                break;
        }
        startActivity(i);
    }

    private void updateViews() {
        if (uris.get(0) == null || uris.get(1) == null) {
            v.findViewById(R.id.gl_both).setVisibility(View.GONE);
            v.findViewById(R.id.text_both_share).setVisibility(View.GONE);
            v.findViewById(R.id.divider).setVisibility(View.GONE);
            if (uris.get(0) != null) {
                loadIcons(FILE);
                v.findViewById(R.id.gl_app_image).setVisibility(View.GONE);
                v.findViewById(R.id.divider3).setVisibility(View.GONE);
                v.findViewById(R.id.title_image).setVisibility(View.GONE);
                v.findViewById(R.id.qr_details).setVisibility(View.GONE);
            } else if (uris.get(1) != null) {
                loadIcons(IMAGE);
                v.findViewById(R.id.gl_app_file).setVisibility(View.GONE);
                v.findViewById(R.id.divider2).setVisibility(View.GONE);
                v.findViewById(R.id.title_file).setVisibility(View.GONE);
                v.findViewById(R.id.file_details).setVisibility(View.GONE);
            } else {
                //todo both null
            }
        } else {
            loadIcons(IMAGE);
            loadIcons(FILE);
            loadIcons(BOTH);
        }
    }

    private void loadIcons(final int what) {
        getApps(what);
        GridLayout gl = null;
        List<ResolveInfo> a = null;
        switch (what) {
            case FILE:
                gl = (GridLayout) v.findViewById(R.id.gl_app_file);
                a = file;
                break;
            case IMAGE:
                gl = (GridLayout) v.findViewById(R.id.gl_app_image);
                a = image;
                break;
            case BOTH:
                gl = (GridLayout) v.findViewById(R.id.gl_both);
                a = both;
                break;
        }
        for (ResolveInfo aFile : a) {
            ImageButton b = new ImageButton(getActivity());
            b.setBackgroundColor(Color.TRANSPARENT);
            final ResolveInfo rs = aFile;
            b.setImageDrawable(rs.loadIcon(getActivity().getPackageManager()));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startOnClick(what, rs);
                }
            });
            gl.addView(b);
        }
    }

    private String getName(int who) {
        int index;
        if (who == IMAGE)
            index = 1;
        else
            index = 0;
        String[] tmp = new File(uris.get(index).getPath()).getName().split("\\.");
        String name = "";
        if (tmp.length > 0)
            for (int t = 0; t < tmp.length - 1; t++)
                name += tmp[t];
        return name;
    }
}