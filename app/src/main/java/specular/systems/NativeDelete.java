package specular.systems;

public class NativeDelete
{
    public void delete(long bignum,int bitSize)
    {
        nativeDelete(bignum,bitSize);
    }
    public native void nativeDelete(long bignum,int bitSize);

    static {
        System.loadLibrary("app");
    }
}
