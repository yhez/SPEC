package specular.systems.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.print.PrintHelper;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import specular.systems.Contact;
import specular.systems.CustomExceptionHandler;
import specular.systems.FilesManagement;
import specular.systems.QRCodeEncoder;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.Visual;

public class SendMsg extends Activity {
    private static final int FILE = 0, IMAGE = 1, BOTH = 2;
    private static boolean done = false;
    private static List<ResolveInfo> file, image, both;
    ArrayList<Uri> uris;
    Contact contact;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        done = false;
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(), this));
        }
        uris = FilesManagement.getFilesToSend(this);
        contact = StaticVariables.contactsDataSource.findContact(getIntent().getLongExtra("contactId", -1));
        if (contact.getDefaultApp() == null) {
            show();
        } else {
            Intent i = new Intent();
            i.setComponent(contact.getDefaultApp());
            if (uris.get(0) == null || uris.get(1) == null)
                i.setAction(Intent.ACTION_SEND);
            else
                i.setAction(Intent.ACTION_SEND_MULTIPLE);
            if (uris.get(0) != null && uris.get(1) != null) {
                i.setType("*/*");
                i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            } else if (uris.get(0) != null) {
                i.setType("file/spec");
                i.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            } else {
                i.setType("image/png");
                i.putExtra(Intent.EXTRA_STREAM, uris.get(1));
            }
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{contact.getEmail()});
            i.putExtra(Intent.EXTRA_SUBJECT,
                    getResources().getString(R.string.subject_encrypt));
            try {
                InputStream is = getAssets().open("spec_tmp_msg.html");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer)));
            } catch (Exception e) {
                Toast.makeText(this, R.string.failed, Toast.LENGTH_LONG)
                        .show();
            }
            try {
                done = true;
                startActivity(i);
            } catch (Exception e) {
                show();
            }
        }
    }

    private void show() {
        setContentView(R.layout.send_msg_dlg);
        updateViews();
        if (uris.get(0) != null) {
            ((TextView) findViewById(R.id.file_size)).setText(Visual.getSize(new File(uris.get(0).getPath()).length()).replace(" ", "\n"));
            ((ImageView) findViewById(R.id.file_icon)).setImageResource(R.drawable.logo);
            EditText etFile = (EditText) findViewById(R.id.name_file);
            etFile.setText(getName(FILE));
            etFile.setSelection(etFile.getText().length());
            etFile.setFilters(Visual.filters());
        }
        if (uris.get(1) != null) {
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(StaticVariables.encryptedMsgToSend,
                    BarcodeFormat.QR_CODE.toString(), 76);
            Bitmap bitmap;
            try {
                bitmap = qrCodeEncoder.encodeAsBitmap();
                ((ImageView) findViewById(R.id.qr_icon)).setImageBitmap(bitmap);
            } catch (WriterException e) {
                ((ImageView) findViewById(R.id.qr_icon)).setImageResource(R.drawable.logo);
                e.printStackTrace();
            }
            ((TextView) findViewById(R.id.qr_size)).setText(Visual.getSize(new File(uris.get(1).getPath()).length()).replace(" ", "\n"));
            EditText etImage = (EditText) findViewById(R.id.qr_name_file);
            etImage.setText(getName(IMAGE));
            etImage.setSelection(etImage.getText().length());
            etImage.setFilters(Visual.filters());
        }
        Visual.setAllFonts(this, (ViewGroup) findViewById(android.R.id.content));
    }

    private void getApps(int a) {
        Intent intent;
        switch (a) {
            case FILE:
                if (file != null)
                    return;
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("file/*");
                file = getPackageManager().queryIntentActivities(intent, 0);
                for (a = 0; a < file.size(); a++)
                    if (file.get(a).activityInfo.packageName.equals(getPackageName())) {
                        file.remove(a);
                        break;
                    }
                return;
            case IMAGE:
                if (image != null)
                    return;
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("image/png");
                List<ResolveInfo> temp = getPackageManager().queryIntentActivities(intent, 0);
                intent.setAction(Intent.ACTION_SEND);
                image = getPackageManager().queryIntentActivities(intent, 0);
                int index = image.size() - 1;
                while (index > 0) {
                    String pn = image.get(index).activityInfo.packageName;
                    if (pn.equals(getPackageName()))
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
                both = getPackageManager().queryIntentActivities(intent, 0);
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
        if (((CheckBox) findViewById(R.id.check_default)).isChecked()) {
            contact.update(rs.activityInfo.packageName + "\n" + rs.activityInfo.name, this);
        }
        EditText etFile = (EditText) findViewById(R.id.name_file),
                etImage = (EditText) findViewById(R.id.qr_name_file);
        if ((etFile.getText().length() == 0 && (what == BOTH || what == FILE))
                || (etImage.getText().length() == 0 && (what == IMAGE || what == BOTH))) {
            Toast t = Toast.makeText(this, R.string.length_name_of_file, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            t.show();
            return;
        }
        ComponentName cn;
        cn = new ComponentName(rs.activityInfo.packageName, rs.activityInfo.name);
        Intent i = new Intent();
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{contact.getEmail()});
        i.setComponent(cn);
        i.putExtra(Intent.EXTRA_SUBJECT,
                getResources().getString(R.string.subject_encrypt));
        try {
            InputStream is = getAssets().open("spec_tmp_msg.html");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.failed, Toast.LENGTH_LONG)
                    .show();
        }
        if (what == IMAGE || what == BOTH) {
            File f = new File(uris.get(1).getPath());
            File newPath = new File(getFilesDir(), etImage.getText() + ".png");
            if (!f.equals(newPath)) {
                if (newPath.exists())
                    newPath.delete();
                f.renameTo(newPath);
                uris.set(1, Uri.parse("file://" + newPath));
            }
        }
        if (what == FILE || what == BOTH) {
            File f = new File(uris.get(0).getPath());
            File newPath = new File(getFilesDir(), etFile.getText() + ".SPEC");
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
                i.setType("file/spec");
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
        try {
            done = true;
            startActivity(i);
        } catch (Exception e) {
            //todo
        }
    }

    private void updateViews() {
        if (uris.get(0) == null || uris.get(1) == null) {
            findViewById(R.id.gl_both).setVisibility(View.GONE);
            findViewById(R.id.text_both_share).setVisibility(View.GONE);
            findViewById(R.id.divider).setVisibility(View.GONE);
            if (uris.get(0) != null) {
                loadIcons(FILE);
                findViewById(R.id.gl_app_image).setVisibility(View.GONE);
                findViewById(R.id.divider3).setVisibility(View.GONE);
                findViewById(R.id.title_image).setVisibility(View.GONE);
                findViewById(R.id.qr_details).setVisibility(View.GONE);
            } else if (uris.get(1) != null) {
                loadIcons(IMAGE);
                findViewById(R.id.gl_app_file).setVisibility(View.GONE);
                findViewById(R.id.divider2).setVisibility(View.GONE);
                findViewById(R.id.title_file).setVisibility(View.GONE);
                findViewById(R.id.file_details).setVisibility(View.GONE);
            } else {
                //todo both null
            }
        } else {
            findViewById(R.id.gl_app_file).setVisibility(View.GONE);
            findViewById(R.id.divider2).setVisibility(View.GONE);
            findViewById(R.id.title_file).setVisibility(View.GONE);
            loadIcons(IMAGE);
            loadIcons(BOTH);
        }
    }

    private void loadIcons(final int what) {
        getApps(what);
        GridLayout gl = null;
        List<ResolveInfo> a = null;
        switch (what) {
            case FILE:
                gl = (GridLayout) findViewById(R.id.gl_app_file);
                a = file;
                break;
            case IMAGE:
                gl = (GridLayout) findViewById(R.id.gl_app_image);
                a = image;
                break;
            case BOTH:
                gl = (GridLayout) findViewById(R.id.gl_both);
                a = both;
                break;
        }
        for (ResolveInfo aFile : a) {
            final ResolveInfo rs = aFile;
            ImageButton b = Visual.glow(rs.loadIcon(getPackageManager()), this);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startOnClick(what, rs);
                }
            });
            gl.addView(b);
        }
        if (what == IMAGE) {
            final PrintHelper photoPrinter = new PrintHelper(this);
            if (PrintHelper.systemSupportsPrint()) {
                ImageButton b = Visual.glow(getResources().getDrawable(R.drawable.printer), this);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                        try {
                            photoPrinter.printBitmap(getString(R.string.subject_encrypt), FilesManagement.getQRToShare(SendMsg.this));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
                gl.addView(b);
            }
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

    @Override
    public void onResume() {
        super.onResume();
        if (done) {
            done = false;
            onBackPressed();
        }
    }
}