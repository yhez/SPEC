package codec.x509;


public class X509Exception extends Exception {
    protected Throwable t_;

    public X509Exception(String message) {
        super(message);
    }

    public String getMessage() {
        if (t_ == null)
            return super.getMessage();

        return t_.getMessage();
    }

    public void printStackTrace() {
        if (t_ != null) {
            System.out.println("Encapsulated in " + getClass().getName());

            t_.printStackTrace();
        } else
            super.printStackTrace();
    }
}
