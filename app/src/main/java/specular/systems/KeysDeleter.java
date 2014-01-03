package specular.systems;


import android.util.Log;

public class KeysDeleter {
    private static Thread t;

    public KeysDeleter() {
        if (CryptMethods.privateExist()) {
            if (t != null && t.isAlive()) {
                t.interrupt();
                t = null;
            }
            t = new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        try {
                            wait(15000);
                            StaticVariables.currentKeys = CryptMethods.privateExist() && CryptMethods.publicExist() ? 0 : CryptMethods.publicExist() ? 1 : CryptMethods.privateExist() ? 2 : 3;
                            CryptMethods.deleteKeys();
                            Log.e("keys have been deleted", "time for deleting 15 seconds");
                        } catch (InterruptedException e) {
                        }
                    }
                }
            });
            t.start();
        }
    }

    public static void stop() {
        if (t != null && t.isAlive())
            t.interrupt();
    }

    public static void delete() {
        StaticVariables.currentKeys = CryptMethods.privateExist() && CryptMethods.publicExist() ? 0 : CryptMethods.publicExist() ? 1 : CryptMethods.privateExist() ? 2 : 3;
        CryptMethods.deleteKeys();
    }
}
