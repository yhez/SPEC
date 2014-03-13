package specular.systems;

public class NativeDelete
{
    public String delete(long bignum,int bitSize)
    {
        nativeDelete(bignum,bitSize);
        return "deleted";
    }
    public native void nativeDelete(long bignum,int bitSize);

    static {
        System.loadLibrary("app");
    }
}
