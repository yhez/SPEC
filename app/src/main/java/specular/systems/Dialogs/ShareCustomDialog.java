package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.print.PrintHelper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import specular.systems.FilesManagement;
import specular.systems.R;
import specular.systems.Visual;

import static android.support.v4.content.FileProvider.getUriForFile;


public class ShareCustomDialog extends DialogFragment {
    public ShareCustomDialog(FragmentManager fm){
        show(fm,"");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.share_dialog, null);
        builder.setView(v);
        GridLayout glFile = (GridLayout) v.findViewById(R.id.gl_app_file);
        List<ResolveInfo> file = getApps("file/*");
        for (ResolveInfo aFile : file) {
            final ResolveInfo rs = aFile;
            ImageButton b = Visual.glow(rs.loadIcon(getActivity().getPackageManager()), getActivity());
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ComponentName cn;
                    cn = new ComponentName(rs.activityInfo.packageName, rs.activityInfo.name);
                    Intent i = new Intent();
                    i.setComponent(cn);
                    i.setType("file/*");
                    i.setAction(Intent.ACTION_SEND);
                    Uri uri =  getUriForFile(getActivity(), getActivity().getPackageName(), FilesManagement.getFileToShare(getActivity()));
                    getActivity().grantUriPermission(rs.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    i.putExtra(Intent.EXTRA_STREAM,uri);
                    try {
                        InputStream is = getActivity().getAssets().open("spec_temp_share.html");
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer)+Visual.timeAndDate()));
                    } catch (Exception ignore) {
                    }
                    i.putExtra(Intent.EXTRA_SUBJECT,
                            getResources().getString(R.string.subject_share));
                    ShareCustomDialog.this.getDialog().cancel();
                    startActivity(i);
                }
            });
            glFile.addView(b);
        }

        glFile = (GridLayout) v.findViewById(R.id.gl_app_image);
        file = getApps("image/png");
        for (ResolveInfo aFile : file) {
            final ResolveInfo rs = aFile;
            ImageButton b = Visual.glow(rs.loadIcon(getActivity().getPackageManager()), getActivity());
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ComponentName cn;
                    cn = new ComponentName(rs.activityInfo.packageName, rs.activityInfo.name);
                    Intent i = new Intent();
                    i.setComponent(cn);
                    i.setType("image/png");
                    i.setAction(Intent.ACTION_SEND);
                    Uri uri = getUriForFile(getActivity(),getActivity().getPackageName(),FilesManagement.getQRToShare(getActivity()));
                    getActivity().grantUriPermission(cn.getPackageName(),uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    i.putExtra(Intent.EXTRA_STREAM, uri);
                    try {
                        InputStream is = getActivity().getAssets().open("spec_temp_share.html");
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer)+Visual.timeAndDate()/*+"<br><br><a href="+ CryptMethods.getMyLink()+"\"/>Direct Link</a>"*/));
                    } catch (Exception ignore) {
                    }
                    i.putExtra(Intent.EXTRA_SUBJECT,
                            getResources().getString(R.string.subject_share));
                    ShareCustomDialog.this.getDialog().cancel();
                    startActivity(i);
                }
            });
            glFile.addView(b);

        }
        final PrintHelper photoPrinter = new PrintHelper(getActivity());
        if (PrintHelper.systemSupportsPrint()) {
            ImageButton b = Visual.glow(getActivity().getResources().getDrawable(R.drawable.printer), getActivity());
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                    try {
                        photoPrinter.printBitmap(getString(R.string.subject_share), Uri.fromFile(FilesManagement.getQRToShare(getActivity())));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            glFile.addView(b);
        }
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }

    private List<ResolveInfo> getApps(String type) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(type);
        List<ResolveInfo> tmp = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        intent.setAction(Intent.ACTION_VIEW);
        List<ResolveInfo> view = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        tmp.removeAll(view);
        for (int a = 0; a < tmp.size(); a++)
            if (tmp.get(a).activityInfo.packageName.equals(getActivity().getPackageName())) {
                tmp.remove(a);
                break;
            }
        return tmp;
    }
}
