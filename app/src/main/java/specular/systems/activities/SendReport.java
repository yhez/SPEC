package specular.systems.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.FilenameFilter;

import specular.systems.CustomExceptionHandler;
import specular.systems.FilesManagement;
import specular.systems.KeysDeleter;
import specular.systems.R;
import specular.systems.Visual;

public class SendReport extends Activity {
    final FilenameFilter filterreported = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return !s.endsWith(".stacktrace");
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(), this));
        }
        File folder = new File(getFilesDir() + FilesManagement.REPORTS);
        for (String f : folder.list(filterreported)) {
            new File(folder, f).delete();
        }
        if (folder.list().length == 0) {
            finish();
        } else {
            setContentView(R.layout.send_report);
        }
    }

    public void send(View v) {
        Intent i = new Intent(this,SendMsg.class);
        i.putExtra("type",SendMsg.REPORT);
        finish();
        startActivity(i);
    }

    public void finish(View v) {
        File folder = new File(getFilesDir() + FilesManagement.REPORTS);
        for (String s : folder.list()) {
            new File(getFilesDir() + FilesManagement.REPORTS, s).delete();
        }
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        new KeysDeleter(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        KeysDeleter.stop();
    }
}
