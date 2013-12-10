package specular.systems;


import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;
    String filename;
    /* 
     * if any of the parameters is null, the respective functionality 
     * will not be used 
     */
    public CustomExceptionHandler(String filename) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.filename=filename;
    }

    public void uncaughtException(Thread t, Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        writeToFile(stacktrace);
        android.os.Process.killProcess(android.os.Process.myPid());
        defaultUEH.uncaughtException(t, e);
    }

    private void writeToFile(String stacktrace) {
        try {
            File path = Environment.getExternalStorageDirectory();
            File folder = new File(path.getPath()+"/spec reports");
            if(!folder.exists())
                folder.mkdir();
            File file = new File(path.getPath()+"/spec reports",filename);
            OutputStream os = new FileOutputStream(file);
            os.write(stacktrace.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 }