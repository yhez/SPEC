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
import android.util.Log;
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
    ArrayList<Uri> uris;
    String nameFile,extFile,nameQR,extQR;
    public SendMsgDialog(ArrayList<Uri> uris) {
        this.uris = uris;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View v = inflater.inflate(R.layout.send_msg_dlg, null);
        builder.setView(v);

        GridLayout glFile = (GridLayout) v.findViewById(R.id.gl_both);
        Intent intent;
        if (uris.get(0) == null || uris.get(1) == null) {
            glFile.setVisibility(View.GONE);
            v.findViewById(R.id.text_both_share).setVisibility(View.GONE);
            v.findViewById(R.id.divider).setVisibility(View.GONE);
        } else {
            intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("*/*");
            getApps(BOTH);
            for (ResolveInfo aFile : both) {
                ImageButton b = new ImageButton(getActivity());
                b.setBackgroundColor(Color.TRANSPARENT);
                final ResolveInfo rs = aFile;
                b.setImageDrawable(rs.loadIcon(getActivity().getPackageManager()));
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File f = new File(uris.get(0).getPath());
                        File newPath = new File(getActivity().getFilesDir(),((EditText)v.findViewById(R.id.name_file)).getText()+".SPEC");
                        if(newPath.exists())
                            newPath.delete();
                        f.renameTo(newPath);
                        uris.set(0,Uri.parse("file://" + newPath));
                        f = new File(uris.get(1).getPath());
                        newPath = new File(getActivity().getFilesDir(),((EditText)v.findViewById(R.id.qr_name_file)).getText()+".png");
                        if(newPath.exists())
                            newPath.delete();
                        f.renameTo(newPath);
                        uris.set(1,Uri.parse("file://" + newPath));
                        ComponentName cn;
                        cn = new ComponentName(rs.activityInfo.packageName, rs.activityInfo.name);
                        Intent i = new Intent();
                        i.setComponent(cn);
                        i.setType("*/*");
                        i.setAction(Intent.ACTION_SEND_MULTIPLE);
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{((TextView)getActivity()
                                .findViewById(R.id.chosen_email)).getText().toString()});
                        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);
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
                        i.putExtra(Intent.EXTRA_SUBJECT,
                                getResources().getString(R.string.subject_encrypt));
                        startActivity(i);
                    }
                });
                glFile.addView(b);
            }
        }
        glFile = (GridLayout) v.findViewById(R.id.gl_app_file);
        if (uris.get(0) == null) {
            v.findViewById(R.id.file_details);
            glFile.setVisibility(View.GONE);
           v.findViewById(R.id.divider2).setVisibility(View.GONE);
            v.findViewById(R.id.title_file).setVisibility(View.GONE);
            v.findViewById(R.id.file_details).setVisibility(View.GONE);
        } else {
            getApps(FILE);
            for (ResolveInfo aFile : file) {
                ImageButton b = new ImageButton(getActivity());
                b.setBackgroundColor(Color.TRANSPARENT);
                final ResolveInfo rs = aFile;
                b.setImageDrawable(rs.loadIcon(getActivity().getPackageManager()));
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File f = new File(uris.get(0).getPath());
                        File newPath = new File(getActivity().getFilesDir(),((EditText)v.findViewById(R.id.name_file)).getText()+".SPEC");
                        if(newPath.exists())
                            newPath.delete();
                        f.renameTo(newPath);
                        uris.set(0,Uri.parse("file://" + newPath));
                        ComponentName cn;
                        cn = new ComponentName(rs.activityInfo.packageName, rs.activityInfo.name);
                        Intent i = new Intent();
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{((TextView)getActivity()
                                .findViewById(R.id.chosen_email)).getText().toString()});
                        i.setComponent(cn);
                        i.setType("file/*");
                        i.setAction(Intent.ACTION_SEND);
                        i.putExtra(Intent.EXTRA_STREAM, uris.get(0));
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
                        i.putExtra(Intent.EXTRA_SUBJECT,
                                getResources().getString(R.string.subject_encrypt));
                        startActivity(i);
                    }
                });
                glFile.addView(b);
            }
        }
        glFile = (GridLayout) v.findViewById(R.id.gl_app_image);
        if (uris.get(1) == null) {
            glFile.setVisibility(View.GONE);
            v.findViewById(R.id.divider3).setVisibility(View.GONE);
            v.findViewById(R.id.title_image).setVisibility(View.GONE);
            v.findViewById(R.id.qr_details).setVisibility(View.GONE);
        } else {
            getApps(IMAGE);
            for (ResolveInfo aFile : image) {
                ImageButton b = new ImageButton(getActivity());
                b.setBackgroundColor(Color.TRANSPARENT);
                final ResolveInfo rs = aFile;
                b.setImageDrawable(rs.loadIcon(getActivity().getPackageManager()));
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File f = new File(uris.get(1).getPath());
                        File newPath = new File(getActivity().getFilesDir(),((EditText)v.findViewById(R.id.qr_name_file)).getText()+".png");
                        if(newPath.exists())
                            newPath.delete();
                        f.renameTo(newPath);
                        uris.set(1,Uri.parse("file://" + newPath));
                        ComponentName cn;
                        cn = new ComponentName(rs.activityInfo.packageName, rs.activityInfo.name);
                        Intent i = new Intent();
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{((TextView) getActivity()
                                .findViewById(R.id.chosen_email)).getText().toString()});
                        i.setComponent(cn);
                        i.setType("image/*");
                        i.setAction(Intent.ACTION_SEND);
                        i.putExtra(Intent.EXTRA_STREAM, uris.get(1));
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
                        i.putExtra(Intent.EXTRA_SUBJECT,
                                getResources().getString(R.string.subject_encrypt));
                        startActivity(i);
                    }
                });
                glFile.addView(b);
            }
        }
        if(uris.get(0)!=null){
            ((TextView)v.findViewById(R.id.file_size)).setText(new File(uris.get(0).getPath()).length()+"");
            ((ImageView)v.findViewById(R.id.file_icon)).setImageResource(R.drawable.logo);
            String[] tmp = new File(uris.get(0).getPath()).getName().split("\\.");
            Log.e("tmp",tmp+"");
            nameFile="";
            if(tmp.length>0)
                for(int t=0;t<tmp.length-1;t++)
                    nameFile+=tmp[t];
            ((TextView)v.findViewById(R.id.name_file)).setText(nameFile);
        }
        if(uris.get(1)!=null){
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(PublicStaticVariables.encryptedMsgToSend,
                    BarcodeFormat.QR_CODE.toString(), 96);
            Bitmap bitmap;
            try {
                bitmap = qrCodeEncoder.encodeAsBitmap();
                ((ImageView)v.findViewById(R.id.qr_icon)).setImageBitmap(bitmap);
            } catch (WriterException e) {
                ((ImageView)v.findViewById(R.id.qr_icon)).setImageResource(R.drawable.logo);
                e.printStackTrace();
            }
            ((TextView)v.findViewById(R.id.qr_size)).setText(new File(uris.get(1).getPath()).length()+"");
            String[] tmp = new File(uris.get(1).getPath()).getName().split("\\.");
            nameQR="";
            if(tmp.length>0)
                for(int t=0;t<tmp.length-1;t++)
                    nameQR+=tmp[t];
            ((EditText)v.findViewById(R.id.qr_name_file)).setText(nameQR);
        }
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
    private static List<ResolveInfo> file,image,both;
    private static final int FILE=0,IMAGE=1,BOTH=2;
    private void getApps(int a) {
        switch (a){
            case FILE:
                if(file!=null)
                    return;
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("file/*");
                file=getActivity().getPackageManager().queryIntentActivities(intent, 0);
                return;
            case IMAGE:
                if(image!=null)
                    return;
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                image=getActivity().getPackageManager().queryIntentActivities(intent, 0);
                return;
            case BOTH:
                if(both!=null)
                    return;
                if(image==null)
                    getApps(IMAGE);
                if(file==null)
                    getApps(FILE);
                intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("*/*");
                both=getActivity().getPackageManager().queryIntentActivities(intent, 0);
                ArrayList<ResolveInfo> tmp=new ArrayList<ResolveInfo>();
                tmp.addAll(both);
                for(ResolveInfo ri:tmp){
                    boolean b=false,d=false;
                    for(ResolveInfo rif:file)
                        if(ri.activityInfo.packageName.equals(rif.activityInfo.packageName)){
                            b=true;
                            break;
                        }
                    for(ResolveInfo rif:image)
                        if(ri.activityInfo.packageName.equals(rif.activityInfo.packageName)){
                            d=true;
                            break;
                        }
                    if(!(b&&d))
                        both.remove(ri);
                }
                return;
        }
    }
}