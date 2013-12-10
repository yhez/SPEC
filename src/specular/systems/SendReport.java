package specular.systems;

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
        setContentView(R.layout.fragment_send_report);
        File folder = new File(Environment.getExternalStorageDirectory()+"/spec reports");
        for(String f:folder.list(filterreported)){
            new File(folder,f).delete();
        }
        if(folder.list().length==0){
            Toast.makeText(this,"clean up old reports",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    public void send(View v){
        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"yhezkel88@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT,"bug report");
        i.putExtra(Intent.EXTRA_TEXT,"attached a file containing the stack trace causing the crash");
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
