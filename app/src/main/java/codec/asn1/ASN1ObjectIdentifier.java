package codec.asn1;

public class ASN1ObjectIdentifier extends ASN1AbstractType implements
        Cloneable, Comparable {

    private int[] value_ = new int[2];

    public ASN1ObjectIdentifier() {
        super();
    }


    public Object getValue() {
        return value_.clone();
    }


    public int[] getOID() {
        return value_.clone();
    }

    public void setOID(int[] oid) {
        set0(oid);
        checkConstraints();
    }

    private void set0(int[] oid) {
        int n;

        if (oid == null) {
            throw new NullPointerException("Need an OID!");
        }

        n = oid.length;

        if (n < 2) {
            throw new IllegalArgumentException(
                    "OID must have at least 2 elements!");
        }

        if ((oid[0] < 0) || (oid[0] > 2)) {
            throw new IllegalArgumentException("OID[0] must be 0, 1, or 2!");
        }

        if ((oid[1] < 0) || (oid[1] > 39)) {
            throw new IllegalArgumentException(
                    "OID[1] must be in the range 0,..,39!");
        }

        value_ = new int[n];
        System.arraycopy(oid, 0, value_, 0, n);
    }

    public int getTag() {
        return ASN1.TAG_OID;
    }

    public void encode(Encoder enc){
        enc.writeObjectIdentifier(this);
    }

    public void decode(Decoder dec){
        dec.readObjectIdentifier(this);
        checkConstraints();
    }

    public String toString() {
        StringBuffer buf;
        int i;

        buf = new StringBuffer();

        for (i = 0; i < value_.length; i++) {
            buf.append(value_[i]).append(".");
        }

        if (value_.length > 0) {
            buf.setLength(buf.length() - 1);
        }

        return buf.toString();
    }


    public boolean equals(Object o) {
        int i;
        ASN1ObjectIdentifier oid;

        if (!(o instanceof ASN1ObjectIdentifier)) {
            return false;
        }

        oid = (ASN1ObjectIdentifier) o;
        if (oid.value_.length != value_.length) {
            return false;
        }

        for (i = 0; i < value_.length; i++) {
            if (value_[i] != oid.value_[i]) {
                return false;
            }
        }

        return true;
    }


    public int hashCode() {
        int i;
        int h;

        h = 23;
        for (i = 0; i < value_.length; i++) {
            h = (h * 7) + value_[i];
        }

        return h;
    }


    public int compareTo(Object o) {
        int n;
        int i;
        int[] oid;

        oid = ((ASN1ObjectIdentifier) o).value_;

        n = Math.min(value_.length, oid.length);
        for (i = 0; i < n; i++) {
            if (value_[i] < oid[i]) {
                return -1;
            } else if (value_[i] > oid[i]) {
                return 1;
            }
        }
        if (value_.length > n) {
            return 1;
        }

        if (oid.length > n) {
            return -1;
        }

        return 0;
    }

    public Object clone() {
        int[] m;
        ASN1ObjectIdentifier oid;

        oid = new ASN1ObjectIdentifier();
        m = new int[value_.length];
        System.arraycopy(value_, 0, m, 0, m.length);
        oid.value_ = m;

        oid.setConstraint(getConstraint());

        return oid;
    }
}
