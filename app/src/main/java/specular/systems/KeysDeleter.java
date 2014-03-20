package specular.systems;


import android.app.Activity;
import android.preference.PreferenceManager;

import specular.systems.activities.Splash;

public class KeysDeleter {
    public static boolean keysDeleted = true;
    public static int oldStatus = -1;
    private static Thread t;
    private static Activity activity;

    public KeysDeleter(Activity activity) {
        KeysDeleter.activity=activity;
        if(!(activity instanceof Splash))
            oldStatus = CryptMethods.privateExist() && CryptMethods.publicExist() ? 0 : CryptMethods.publicExist() ? 1 : CryptMethods.privateExist() ? 2 : 3;
        if (CryptMethods.privateExist()) {
            keysDeleted=false;
            if (t != null && t.isAlive()) {
                t.interrupt();
                t = null;
            }
            t = new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        try {
                            ((Object)this).wait(15000);
                            delete();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            });
            t.start();
        }
    }

    public static void stop() {
        if (t != null && t.isAlive()){
            t.interrupt();
            t=null;
        }
    }

    public static void delete() {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putLong("onPause",System.currentTimeMillis()).commit();
        CryptMethods.deleteKeys();
        keysDeleted=true;
    }
}
