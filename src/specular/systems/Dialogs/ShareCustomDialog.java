package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.InputStream;
import java.util.List;

import specular.systems.CryptMethods;
import specular.systems.FilesManagement;
import specular.systems.R;
import specular.systems.Visual;


public class ShareCustomDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.share_dialog, null);

        builder.setView(v);

        GridLayout glFile = (GridLayout)v.findViewById(R.id.gl_app_file);
        List<ResolveInfo> file = getApps("file/*");
        for(int a=0;a<file.size();a++){
            ImageButton b = new ImageButton(getActivity());
            b.setBackgroundColor(Color.TRANSPARENT);
            final ResolveInfo rs =file.get(a);
            b.setImageDrawable(rs.loadIcon(getActivity().getPackageManager()));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ComponentName cn;
                    cn = new ComponentName(rs.activityInfo.packageName,rs.activityInfo.name);
                    Intent i = new Intent();
                    i.setComponent(cn);
                    i.setType("file/*");
                    i.setAction(Intent.ACTION_SEND);
                    i.putExtra(Intent.EXTRA_STREAM,FilesManagement.getFileToShare(getActivity()));
                    try {
                        InputStream is = getActivity().getAssets().open("spec_temp_share.html");
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
                            getResources().getString(R.string.subject_share));
                    startActivity(i);
                }
            });
            glFile.addView(b);
        }

        glFile = (GridLayout)v.findViewById(R.id.gl_app_image);
        file = getApps("image/*");
        for(int a=0;a<file.size();a++){
            ImageButton b = new ImageButton(getActivity());
            b.setBackgroundColor(Color.TRANSPARENT);
            final ResolveInfo rs = file.get(a);
            b.setImageDrawable(rs.loadIcon(getActivity().getPackageManager()));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ComponentName cn;
                    cn = new ComponentName(rs.activityInfo.packageName,rs.activityInfo.name);
                    Intent i = new Intent();
                    i.setComponent(cn);
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_SEND);
                    i.putExtra(Intent.EXTRA_STREAM, FilesManagement.getQRToShare(getActivity()));
                    try {
                        InputStream is = getActivity().getAssets().open("spec_temp_share.html");
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
                            getResources().getString(R.string.subject_share));
                    startActivity(i);
                }
            });
            glFile.addView(b);
        }
        glFile = (GridLayout)v.findViewById(R.id.gl_app_text);
        file = getApps("text/*");
        for(int a=0;a<file.size();a++){
            ImageButton b = new ImageButton(getActivity());
            b.setBackgroundColor(Color.TRANSPARENT);
            final ResolveInfo rs = file.get(a);
            b.setImageDrawable(rs.loadIcon(getActivity().getPackageManager()));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ComponentName cn;
                    cn = new ComponentName(rs.activityInfo.packageName,rs.activityInfo.name);
                    Intent i = new Intent();
                    i.setComponent(cn);
                    i.setType("text/*");
                    i.setAction(Intent.ACTION_SEND);
                    i.putExtra(Intent.EXTRA_SUBJECT,
                            getResources().getString(R.string.subject_share));
                    i.putExtra(Intent.EXTRA_TEXT, CryptMethods.getPublic());
                    startActivity(i);
                }
            });
            glFile.addView(b);
        }
        Visual.setAllFonts(getActivity(),(ViewGroup)v);
        return builder.create();
    }
    private List<ResolveInfo> getApps(String type){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(type);
        return getActivity().getPackageManager().queryIntentActivities(intent, 0);
    }
}
