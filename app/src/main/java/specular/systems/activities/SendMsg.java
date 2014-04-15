package specular.systems.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.CustomExceptionHandler;
import specular.systems.FilesManagement;
import specular.systems.Group;
import specular.systems.GroupDataSource;
import specular.systems.KeysDeleter;
import specular.systems.R;
import specular.systems.Visual;

import static android.support.v4.content.FileProvider.getUriForFile;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class SendMsg extends Activity {
    public static boolean msgSended;
    public static final int CONTACT = 1, MESSAGE = 2, INVITE_GROUP = 3, MESSAGE_FOR_GROUP = 4, BACKUP = 5, REPORT = 6, OPEN_FILE = 7;
    private static List<ResolveInfo> file;
    Uri uris;
    Contact contact;
    Group group;
    int type = -1;
    String mimetype;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        msgSended = false;
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(), this));
        }
        type = getIntent().getIntExtra("type", -1);
        mimetype = getIntent().getStringExtra("mimetype");
        if (type == REPORT || type == OPEN_FILE) {
            show();
            return;
        }
        uris = FilesManagement.getFilesToSend(this);
        long id = getIntent().getLongExtra("contactId", -1);
        if (type == MESSAGE || type == INVITE_GROUP)
            contact = ContactsDataSource.contactsDataSource.findContact(id);
        else if (type == MESSAGE_FOR_GROUP) {
            group = GroupDataSource.groupDataSource.findGroup(id);
        }
        if (contact == null || contact.getDefaultApp() == null) {
            show();
        } else {
            Intent i = new Intent();
            i.setComponent(contact.getDefaultApp());
            if (uris != null) {
                grantUriPermission(contact.getDefaultApp().getPackageName(), uris, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.setAction(Intent.ACTION_SEND);
                i.setType("file/spec");
                i.putExtra(Intent.EXTRA_STREAM, uris);
            }
            if (contact != null) {
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{contact.getEmail()});
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getResources().getString(R.string.subject_encrypt));
                try {
                    InputStream is = getAssets().open("spec_tmp_msg.html");
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer) + Visual.timeAndDate()));
                } catch (Exception ignore) {
                }
            }
            try {
                msgSended = true;
                startActivity(i);
                if (contact != null)
                    contact.update(this);
            } catch (Exception e) {
                show();
            }
        }
    }

    private void show() {
        setContentView(R.layout.send_msg_dlg);
        updateViews();
        if (uris != null) {
            if (type == BACKUP)
                ((TextView) findViewById(R.id.file_size)).setText(Visual.getSize(new File(getFilesDir() + FilesManagement.MESSAGES, getString(R.string.file_name_Backup_msg)).length()).replace(" ", Visual.strings.NEW_LINE));
            else
                ((TextView) findViewById(R.id.file_size)).setText(Visual.getSize(new File(getFilesDir() + FilesManagement.MESSAGES, getString(R.string.file_name_secure_msg)).length()).replace(" ", Visual.strings.NEW_LINE));
            ((ImageView) findViewById(R.id.file_icon)).setImageResource(R.drawable.logo);
            EditText etFile = (EditText) findViewById(R.id.name_file);
            etFile.setText(getName());
            etFile.setSelection(etFile.length());
            etFile.setFilters(Visual.filters());
        }
        Visual.setAllFonts(this, (ViewGroup) findViewById(android.R.id.content));
    }

    private void getApps() {
        Intent intent;
        if (type == OPEN_FILE) {
            intent = new Intent(Intent.ACTION_VIEW);
            if (mimetype == null)
                intent.setType(Visual.strings.MIME_ALL);
            else
                intent.setType(mimetype);
            file = getPackageManager().queryIntentActivities(intent, 0);
            for (int a = 0; a < file.size(); a++)
                if (file.get(a).activityInfo.packageName.equals(getPackageName())) {
                    file.remove(a);
                    break;
                }
        } else {
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("file/*");
            file = getPackageManager().queryIntentActivities(intent, 0);
            ResolveInfo drive=null,me=null;
            for (int a = 0; a < file.size(); a++)
                if (file.get(a).activityInfo.packageName.equals(getPackageName()))
                    me = file.get(a);
                else if(file.get(a).activityInfo.packageName.equals("com.google.android.apps.docs"))
                    drive = file.get(a);
            if(me!=null) file.remove(me);
            if(drive!=null)file.remove(drive);
        }
    }

    final FilenameFilter filterNotReported = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return s.endsWith(".stacktrace");
        }
    };

    private void startOnClick(ResolveInfo rs) {
        if (type == REPORT) {
            ComponentName cn = new ComponentName(rs.activityInfo.packageName, rs.activityInfo.name);
            Intent i = new Intent();
            i.setAction(Intent.ACTION_SEND_MULTIPLE);
            i.setComponent(cn);
            i.setType(Visual.strings.MIME_ALL);
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email_for_reports)});
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_report_subject));
            i.putExtra(Intent.EXTRA_TEXT, getString(R.string.send_report_content));
            File folder = new File(getFilesDir() + FilesManagement.REPORTS);
            ArrayList<Parcelable> uris = new ArrayList<Parcelable>();
            for (String s : folder.list(filterNotReported)) {
                File oldname = new File(getFilesDir() + FilesManagement.REPORTS, s);
                File newNmae = new File(getFilesDir() + FilesManagement.REPORTS, s.split("\\.")[0] + ".txt");
                oldname.renameTo(newNmae);
                uris.add(getUriForFile(this, getPackageName(), newNmae));
            }
            for (Parcelable u : uris) {
                grantUriPermission(cn.getPackageName(), (Uri) u, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            finish();
            startActivity(i);
            return;
        }
        if (type == OPEN_FILE) {
            ComponentName cn = new ComponentName(rs.activityInfo.packageName, rs.activityInfo.name);
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setComponent(cn);
            i.setType(mimetype == null ? Visual.strings.MIME_ALL : mimetype);
            File folder = new File(getFilesDir() + FilesManagement.ATTACHMENTS);
            File f = new File(folder, folder.list()[0]);
            Uri uri = getUriForFile(this, getPackageName(), f);
            grantUriPermission(rs.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setData(uri);
            startActivity(i);
            finish();
            return;
        }
        if (contact != null)
            if (((CheckBox) findViewById(R.id.check_default)).isChecked()) {
                contact.update(rs.activityInfo.packageName + Visual.strings.NEW_LINE + rs.activityInfo.name, null);
            }
        EditText etFile = (EditText) findViewById(R.id.name_file);
        if (etFile.length() == 0) {
            Visual.toast(this, R.string.length_name_of_file);
            return;
        }
        ComponentName cn = new ComponentName(rs.activityInfo.packageName, rs.activityInfo.name);
        Intent i = new Intent();
        if (type == MESSAGE || type == MESSAGE_FOR_GROUP) {
            String email = contact != null ? contact.getEmail() : group.getEmail();
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            i.putExtra(Intent.EXTRA_SUBJECT,
                    getResources().getString(R.string.subject_encrypt));
            try {
                InputStream is = getAssets().open("spec_tmp_msg.html");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer) + Visual.timeAndDate()));
            } catch (Exception ignore) {
            }
        } else if (type == INVITE_GROUP) {
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{contact.getEmail()});
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_send_group));
            try {
                InputStream is = getAssets().open("spec_tmp_invite.html");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer) + Visual.timeAndDate()));
            } catch (Exception ignore) {
            }
        } else if (type == BACKUP) {
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_backup_data));
            try {
                InputStream is = getAssets().open("backup_file.html");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer) + Visual.timeAndDate()));
            } catch (Exception ignore) {
            }
        }
        i.setComponent(cn);
        if (((type == MESSAGE || type == MESSAGE_FOR_GROUP) && !(etFile.getText() + ".SPEC").equals(getString(R.string.file_name_secure_msg)))
                || (type == BACKUP && !(etFile.getText() + ".SPEC").equals(getString(R.string.file_name_Backup_msg)))) {
            File newPath = new File(getFilesDir(), etFile.getText() + ".SPEC");
            if (newPath.exists())
                newPath.delete();
            File oldPath;
            if (type == BACKUP)
                oldPath = new File(getFilesDir() + FilesManagement.MESSAGES, getString(R.string.file_name_Backup_msg));
            else
                oldPath = new File(getFilesDir() + FilesManagement.MESSAGES, getString(R.string.file_name_secure_msg));
            oldPath.renameTo(newPath);
            uris = getUriForFile(this, getPackageName(), newPath);
        }
        i.setAction(Intent.ACTION_SEND);
        grantUriPermission(cn.getPackageName(), uris, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        i.setType("file/spec");
        i.putExtra(Intent.EXTRA_STREAM, uris);
        try {
            msgSended = true;
            startActivity(i);
            if (contact != null)
                contact.update(this);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        new KeysDeleter(this);
    }

    private void updateViews() {
        loadIcons();
        if (type == REPORT) {
            ((TextView) findViewById(R.id.title_file)).setText("Share stack trace files VIA...");
        } else if (type == OPEN_FILE) {
            ((TextView) findViewById(R.id.title_file)).setText(R.string.open_by_external_app);
        }
    }

    private void loadIcons() {
        getApps();
        GridLayout gl = (GridLayout) findViewById(R.id.gl_app_file);
        for (ResolveInfo aFile : file) {
            final ResolveInfo rs = aFile;
            ImageButton b = Visual.glow(rs.loadIcon(getPackageManager()), this);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startOnClick(rs);
                }
            });
            gl.addView(b);
        }
    }

    private String getName() {
        String[] tmp = new File(uris.getPath()).getName().split("\\.");
        String name = "";
        if (tmp.length > 0)
            for (int t = 0; t < tmp.length - 1; t++)
                name += tmp[t];
        return name;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (msgSended) {
            finish();
        } else
            KeysDeleter.stop();
    }
}