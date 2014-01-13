package specular.systems;


public class KeysDeleter {
    public static boolean keysDeleted = true;
    public static int oldStatus;
    private static Thread t;

    public KeysDeleter() {
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
                            wait(15000);
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
        oldStatus = CryptMethods.privateExist() && CryptMethods.publicExist() ? 0 : CryptMethods.publicExist() ? 1 : CryptMethods.privateExist() ? 2 : 3;
        CryptMethods.deleteKeys();
        keysDeleted=true;
    }
}
