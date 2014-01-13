package specular.systems;


import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler defaultUEH;
    final String filename;
    final Activity a;

    /*
     * if any of the parameters is null, the respective functionality 
     * will not be used 
     */
    public CustomExceptionHandler(String filename, Activity a) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.filename = filename;
        this.a = a;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String version = "";
        try {
            PackageInfo pInfo = a.getPackageManager().getPackageInfo(a.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        writeToFile("spec version: " + version + "\n" + stacktrace);
        android.os.Process.killProcess(android.os.Process.myPid());
        defaultUEH.uncaughtException(t, e);
    }

    private void writeToFile(String stacktrace) {
        try {
            File path = Environment.getExternalStorageDirectory();
            File folder = new File(path.getPath() + "/SPEC/reports");
            if (!folder.exists())
                folder.mkdirs();
            File file = new File(path.getPath() + "/SPEC/reports", filename);
            OutputStream os = new FileOutputStream(file);
            os.write(stacktrace.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}