package codec.x501;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import codec.asn1.ASN1;
import codec.asn1.ASN1Exception;
import codec.asn1.ASN1IA5String;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OpenType;
import codec.asn1.ASN1PrintableString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1SequenceOf;
import codec.asn1.ASN1Set;
import codec.asn1.ASN1SetOf;
import codec.asn1.ASN1String;
import codec.asn1.ASN1T61String;
import codec.asn1.ASN1Type;
import codec.asn1.ASN1UTF8String;
import codec.asn1.BERDecoder;
import codec.asn1.DEREncoder;
import codec.asn1.Decoder;
import codec.asn1.Resolver;


public class Name extends ASN1SequenceOf implements Principal, Resolver {

    public static final int IA5_ENCODING = ASN1.TAG_IA5STRING;

    public static final int PRINTABLE_ENCODING = ASN1.TAG_PRINTABLESTRING;

    public static final int T61_ENCODING = ASN1.TAG_T61STRING;

    public static final int UTF8_ENCODING = ASN1.TAG_UTF8STRING;

    protected static int defaultEncoding_ = PRINTABLE_ENCODING;

    static final String keys_[] = {"ALIASEDOBJECTNAME", "C", "CN", "DC",
            "DATEOFBIRTH", "DNQUALIFIER", "DESCRIPTION", "EMAILADDRESS",
            "GENDER", "GENERATION", "GN", "INITIALS", "IP", "L", "O", "OU",
            "PLACEOFBIRTH", "POSTALADDRESS", "POSTALCODE", "PSEUDONYM",
            "SERIALNUMBER", "SN", "ST", "STREET", "UID", "TITLE",};

    static final int oids_[][] = {{2, 5, 4, 1}, {2, 5, 4, 6},
            {2, 5, 4, 3}, {0, 9, 2342, 19200300, 100, 1, 25},
            {1, 3, 6, 1, 5, 5, 7, 9, 1}, {2, 5, 4, 46}, {2, 5, 4, 13},
            {1, 2, 840, 113549, 1, 9, 1}, {1, 3, 6, 1, 5, 5, 7, 9, 3},
            {2, 5, 4, 44}, {2, 5, 4, 42}, {2, 5, 4, 43},
            {1, 3, 6, 1, 4, 1, 42, 2, 11, 2, 1}, {2, 5, 4, 7},
            {2, 5, 4, 10}, {2, 5, 4, 11}, {1, 3, 6, 1, 5, 5, 7, 9, 2},
            {2, 5, 4, 16}, {2, 5, 4, 17}, {2, 5, 4, 65}, {2, 5, 4, 5},
            {2, 5, 4, 4}, {2, 5, 4, 8}, {2, 5, 4, 9},
            {0, 9, 2342, 19200300, 100, 1, 1}, {2, 5, 4, 12},};

    protected HashMap a2oid_;


    protected HashMap oid2a_;


    private String name_;

    List tmp_;


    public Name(String rfc2253String) throws Exception {
        this(rfc2253String, -1);
    }

    private Name(String rfc2253String, int encType) throws Exception {
        super(8);

        ASN1ObjectIdentifier oid;
        RFC2253Parser p;
        ASN1Sequence seq;
        Iterator i;
        ASN1Set set;
        String key;
        String val;
        AVA entry;

        initMaps();

        if (!(encType == -1)){
            if (encType != UTF8_ENCODING && encType != T61_ENCODING
                    && encType != PRINTABLE_ENCODING && encType != IA5_ENCODING) {
                throw new Exception("Unknown EncodingType: " + encType);
            }
        }

        p = new RFC2253Parser();
        set = new ASN1Set(1);

        for (i = p.parse(rfc2253String).iterator(); i.hasNext(); ) {
            entry = (AVA) i.next();
            key = entry.getKey();
            key = key.toUpperCase();
            oid = (ASN1ObjectIdentifier) a2oid_.get(key);
            seq = new ASN1Sequence(2);

            if (oid == null) {
                try {
                    oid = new ASN1ObjectIdentifier(key);
                } catch (Exception e) {
                    throw new Exception("Unsupported attribute key: \""
                            + key + "\"");
                }
            }
            seq.add(oid.clone());

            if (entry.isEncodedValue()) {
                ByteArrayInputStream in;
                BERDecoder dec;
                ASN1Type obj;
                byte[] buf;

                try {
                    buf = entry.getEncodedValue();
                    in = new ByteArrayInputStream(buf);
                    dec = new BERDecoder(in);
                    obj = dec.readType();

                    dec.close();
                } catch (Exception e) {
                    throw new Exception(
                            "Binary data is not a valid BER encoding!");
                }
                seq.add(obj);
            } else {
                val = entry.getValue();

                if (entry.getKey().equalsIgnoreCase("EMAILADDRESS")
                        || entry.getKey().equalsIgnoreCase("UID")) {
                    seq.add(new ASN1IA5String(val));
                } else if (entry.getKey().equalsIgnoreCase("C")
                        || entry.getKey().equalsIgnoreCase("SERIALNUMBER")) {
                    seq.add(new ASN1PrintableString(val));
                } else {

                    int currentEncoding_ = defaultEncoding_;
                    switch (currentEncoding_) {
                        case (ASN1.TAG_UTF8STRING):
                            seq.add(new ASN1UTF8String(val));
                            break;
                        case (ASN1.TAG_IA5STRING):
                            seq.add(new ASN1IA5String(val));
                            break;
                        case (ASN1.TAG_PRINTABLESTRING):
                            if (checkPrintableSpelling(val)) {
                                seq.add(new ASN1PrintableString(val));
                            } else {
                                throw new Exception(
                                        "Illegal characters for PrintableString "
                                                + "in characters");
                            }
                            break;
                        case (ASN1.TAG_T61STRING):
                            seq.add(new ASN1T61String(val));
                            break;
                    }
                }
            }
            set.add(seq);

            if (entry.hasSibling()) {
                continue;
            }
            set.trimToSize();

            super.add(0, set);
            set = new ASN1Set(1);
        }
        trimToSize();
    }

    public void clear() {
        super.clear();

        if (tmp_ != null) {
            tmp_.clear();
        }
    }

    protected void initMaps() {
        int i;
        ASN1ObjectIdentifier oid;

        if (a2oid_ == null) {
            a2oid_ = new HashMap();
            oid2a_ = new HashMap();

            for (i = 0; i < keys_.length; i++) {
                oid = new ASN1ObjectIdentifier(oids_[i]);

                a2oid_.put(keys_[i], oid);
                oid2a_.put(oid, keys_[i]);
            }
        }
    }

    private boolean checkPrintableSpelling(String val) {
        char[] allowed;
        char[] value;
        allowed = ("abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789 (),-./:=?").toCharArray();

        value = val.toCharArray();

        for (char aValue : value) {
            for (int j = 0; j < allowed.length; j++) {
                if (aValue == allowed[j]) {
                    break;
                }
                if (j == allowed.length - 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public String toString() {
        return getName();
    }

    public String getName() {
        StringBuffer buf;
        Iterator it;
        AVA entry;

        if (name_ != null) {
            return name_;
        }
        buf = new StringBuffer();

        for (it = getAVAList().iterator(); it.hasNext(); ) {
            entry = (AVA) it.next();

            buf.insert(0, entry.toString());

            if (it.hasNext()) {
                if (entry.hasSibling()) {
                    buf.insert(0, " + ");
                } else {
                    buf.insert(0, ", ");
                }
            }
        }

        return buf.toString();
    }

    public List getAVAList() {
        ASN1ObjectIdentifier oid;
        ASN1Sequence ava;
        ArrayList list;
        ASN1Type obj;
        Iterator i;
        boolean sibling;
        ASN1Set rdn;
        String val;
        String key;
        AVA entry;
        int j;
        int n;

        list = new ArrayList(size());

        for (i = iterator(); i.hasNext(); ) {
            rdn = (ASN1Set) i.next();
            n = rdn.size();

            for (j = 0; j < n; j++) {

                sibling = (j < n - 1);

                ava = (ASN1Sequence) rdn.get(j);
                oid = (ASN1ObjectIdentifier) ava.get(0);
                obj = (ASN1Type) ava.get(1);
                key = (String) oid2a_.get(oid);

                if (key == null) {
                    key = oid.toString();
                }
                if (obj instanceof ASN1String) {
                    val = ((ASN1String) obj).getString();
                    entry = new AVA(key, val, sibling);
                } else {

                    ByteArrayOutputStream out;
                    DEREncoder enc;

                    try {
                        out = new ByteArrayOutputStream();
                        enc = new DEREncoder(out);

                        obj.encode(enc);

                        entry = new AVA(key, out.toByteArray(), sibling);

                        enc.close();
                    } catch (Exception e) {
                        throw new IllegalStateException("Cannot BER encode!");
                    }
                }
                list.add(entry);
            }
        }
        return list;
    }


    public ASN1Type resolve(ASN1Type caller) {
        if (caller == null) {
            throw new NullPointerException("caller");
        }
        if (tmp_ == null) {
            tmp_ = new ArrayList(8);
        }
        ASN1Sequence seq;

        seq = new ASN1Sequence(2);

        seq.add(new ASN1ObjectIdentifier());
        seq.add(new ASN1OpenType());
        tmp_.add(seq);

        return seq;
    }


    public void decode(Decoder dec) throws ASN1Exception, IOException {
        clear();

        super.decode(dec);

        ASN1Sequence seq;
        ASN1OpenType t;
        Iterator i;
        Object o;

        for (i = tmp_.iterator(); i.hasNext(); ) {
            seq = (ASN1Sequence) i.next();
            t = (ASN1OpenType) seq.get(1);
            o = t.getInnerType();

            seq.set(1, o);
        }
        tmp_ = null;
    }


    public boolean add(ASN1Set o) {
        ASN1Sequence seq;
        Iterator it;
        // Iterator j;
        ASN1Set set;
        Object p;

        if (o == null) {
            throw new NullPointerException("parameter is null");
        }
        set = o;

        for (it = set.iterator(); it.hasNext(); ) {
            p = it.next();

            if (!(p instanceof ASN1Sequence)) {
                throw new IllegalArgumentException("not a sequence: "
                        + p.getClass().getName());
            }
            seq = (ASN1Sequence) p;

            if (seq.size() != 2) {
                throw new IllegalArgumentException(
                        "sequence does not have 2 elements: " + seq.size());
            }
            if (!(seq.get(0) instanceof ASN1ObjectIdentifier)) {
                throw new IllegalArgumentException("attribute type not an OID");
            }
            p = seq.get(1);

            if (p == null || !(p instanceof ASN1Type)) {
                throw new IllegalArgumentException(
                        "illegal or no attribute value");
            }
        }
        super.add(set);

        return true;
    }
    public ASN1Type newElement() {
        ASN1SetOf set;

        set = new ASN1SetOf(this, 1);
        super.add(set);

        return set;
    }


    public boolean equals(Object o) {
        Hashtable table1;
        Hashtable table2;
        Enumeration en;
        Integer int1;
        Integer int2;
        String str;
        Name q;

        if (!(o instanceof Principal)) {
            return false;
        }

        if (!(o instanceof Name)) {
            try {
                q = new Name(((Principal) o).getName());
            } catch (Exception e) {
                return false;
            }
        } else {
            q = (Name) o;
        }

        table1 = getNameTable();
        table2 = q.getNameTable();

        if (table1.size() != table2.size()) {
            return false;
        }
        en = table1.keys();

        while (en.hasMoreElements()) {
            str = (String) en.nextElement();

            if (!table2.containsKey(str)) {
                return false;
            }

            int1 = (Integer) table1.get(str);
            int2 = (Integer) table2.get(str);

            if (int1.compareTo(int2) != 0) {
                return false;
            }
        }
        return true;
    }


    public Hashtable divide() {
        StringTokenizer st;
        Hashtable result;
        List list;
        int iou;
        int j;
        int i;

        result = new Hashtable();
        list = getAVAList();
        iou = 0;

        for (i = 0; i < list.size(); i++) {
            AVA ava = (AVA) list.get(i);

            if ("1.2.840.113549.1.9.2".equals(ava.getKey())) { // UN
                result.put("UN", ava.getValue());

                st = new StringTokenizer(ava.getValue(), ".");
                j = 0;

                while (st.hasMoreTokens()) {
                    j++;
                    result.put("UN" + j, st.nextToken());
                }
            } else if ("1.2.840.113549.1.9.8".equals(ava.getKey())) {
                result.put("UA", ava.getValue());
            } else if ("OU".equals(ava.getKey())) {
                if (result.get("OU") == null) {
                    result.put("OU", ava.getValue());
                } else { // there was already a OU
                    result.put("OU0", result.get("OU"));
                    iou++;
                    result.put("OU" + iou, ava.getValue());
                }
            } else {
                result.put(ava.getKey(), ava.getValue());
            }
        }
        return result;
    }


    protected Hashtable getNameTable() {
        Hashtable nameTable;
        Iterator it;
        String key;
        int order;
        AVA entry;
        int i;

        order = 1;
        nameTable = new Hashtable();
        it = getAVAList().iterator();

        while (it.hasNext()) {
            entry = (AVA) it.next();
            key = entry.toString();
            i = 1;

            while (nameTable.containsKey(key)) {
                key = (new AVA(entry.getKey() + i, entry.getValue(), entry
                        .hasSibling())).toString();

                i++;
            }
            nameTable.put(key, order);

            if (!entry.hasSibling()) {
                order = order + 1;
            }
        }
        return nameTable;
    }
}
