package specular.systems.activitys;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import specular.systems.CustomExceptionHandler;
import specular.systems.FilesManagement;
import specular.systems.R;
import specular.systems.Visual;

public class SendReport extends Activity {
    FilenameFilter filterreported = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if(s.endsWith(".stacktrace"))
                return false;
            return true;
        }
    };
    FilenameFilter filterNotReported = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if(s.endsWith(".stacktrace"))
                return true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FilesManagement.getKeysFromSDCard(this);
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt()));
        }
        setContentView(R.layout.fragment_send_report);
        File folder = new File(Environment.getExternalStorageDirectory()+"/spec reports");
        for(String f:folder.list(filterreported)){
            new File(folder,f).delete();
        }
        if(folder.list().length==0){
            Toast.makeText(this,R.string.clean_up_reported_files,Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    public void send(View v){
        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email_for_reports)});
        i.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.send_report_subject));
        i.putExtra(Intent.EXTRA_TEXT,getString(R.string.send_report_content));
        File folder = new File(Environment.getExternalStorageDirectory()+"/spec reports");
        ArrayList<Parcelable> uris = new ArrayList<Parcelable>();
        for(String s:folder.list(filterNotReported)){
            File oldname = new File(Environment.getExternalStorageDirectory()+"/spec reports",s);
            File newNmae = new File(Environment.getExternalStorageDirectory()+"/spec reports",s.split("\\.")[0]+".txt");
            oldname.renameTo(newNmae);
            uris.add(Uri.fromFile(newNmae));
        }
        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(i);
        finish();
    }
    public void finish(View v){
        finish();
    }
}
