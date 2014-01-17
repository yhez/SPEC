package codec.asn1;


public final class ASN1 {
    public static final int TAG_EOC = 0;
    public static final int TAG_BOOLEAN = 1;
    public static final int TAG_INTEGER = 2;
    public static final int TAG_BITSTRING = 3;
    public static final int TAG_OCTETSTRING = 4;
    public static final int TAG_NULL = 5;
    public static final int TAG_OID = 6;
    public static final int TAG_REAL = 9;
    public static final int TAG_UTF8STRING = 12;
    public static final int TAG_SEQUENCE = 16;
    public static final int TAG_SET = 17;
    public static final int TAG_PRINTABLESTRING = 19;
    public static final int TAG_T61STRING = 20;
    public static final int TAG_IA5STRING = 22;
    public static final int TAG_VISIBLESTRING = 26;
    public static final int TAG_MASK = 0x1f;
    public static final int TAG_LONGFORM = 0x1f;
    public static final int CLASS_UNIVERSAL = 0x00;
    public static final int CLASS_APPLICATION = 0x40;
    public static final int CLASS_CONTEXT = 0x80;
    public static final int CLASS_PRIVATE = 0xc0;
    public static final int CLASS_MASK = 0xc0;
    public static final int CONSTRUCTED = 0x20;
    public static final int LENGTH_LONGFORM = 0x80;
    public static final int LENGTH_MASK = 0x7f;

    private ASN1() {
    }
}
