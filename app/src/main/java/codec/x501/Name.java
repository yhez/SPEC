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
import java.util.Vector;

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
    /**
     * The serial version UID of the class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constant for IA5Encoding of the Name
     */
    public static final int IA5_ENCODING = ASN1.TAG_IA5STRING;

    /**
     * Constant for Printable Encoding of the Name
     */
    public static final int PRINTABLE_ENCODING = ASN1.TAG_PRINTABLESTRING;

    /**
     * Constant for Teletex (T61) Encoding of the Name
     */
    public static final int T61_ENCODING = ASN1.TAG_T61STRING;

    /**
     * Constant for UTF8Encoding of the Name
     */
    public static final int UTF8_ENCODING = ASN1.TAG_UTF8STRING;

    /**
     * flag that determines, how the Name class shall behave, either as usual
     * with a default encoding or forcing all Name using classes to use the new
     * constructor.
     */
    protected static final boolean allowDefaultEncoding_ = true;

    /**
     * Determines in what kind of encoding all Name objects will be encoded as
     * long as no special constructors are used. Default is UTF8 Encoding.
     */
    protected static int defaultEncoding_ = PRINTABLE_ENCODING;

    /**
     * defines the encoding of the current Name Object
     */
    private int currentEncoding_ = defaultEncoding_;

    /**
     * The (uppercase) acronyms of the default attributes allowed in this Name
     * class.
     */
    static final String keys_[] = {"ALIASEDOBJECTNAME", "C", "CN", "DC",
            "DATEOFBIRTH", "DNQUALIFIER", "DESCRIPTION", "EMAILADDRESS",
            "GENDER", "GENERATION", "GN", "INITIALS", "IP", "L", "O", "OU",
            "PLACEOFBIRTH", "POSTALADDRESS", "POSTALCODE", "PSEUDONYM",
            "SERIALNUMBER", "SN", "ST", "STREET", "UID", "TITLE",};

    /**
     * The OID of the default attributes allowed in this Name class.
     */
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

    /**
     * Mapping from acronyms to OID.
     */
    protected HashMap a2oid_;

    /**
     * Mapping from OID to acronyms.
     */
    protected HashMap oid2a_;

    /**
     * The cached string representation.
     */
    private String name_;

    /**
     * The cached reverse string representation.
     */
    private String rname_;

    /**
     * The temporary list of AVAs that is collected during DER decoding.
     */
    List tmp_;

    /**
     * This constructor calls the initASN1Structure() method, do create an empty
     * structure for a Relative Distinguished Name object. Used incase one wants
     * to use the clone function for Name.
     */
    public Name() {
        super(8);
        initMaps();
    }

    /**
     * This constructor parses the given String according to <a
     * href="http://sunsite.auc.dk/RFC/">RFC2253</a> and builds the internal
     * ASN.1 representation of it in big-endian order (most significant
     * attribute first). This is the order used for encoding the name.
     * <p/>
     * <p/>
     * Any names parsed with instances of this class remain in the order they
     * were encoded in order not to invalidate any digital signatures on the
     * encoded representation when writing the encoded instance back to some
     * output stream.
     *
     * @param rfc2253String String of RFC2253 representation.
     * @deprecated
     */
    public Name(String rfc2253String) throws BadNameException {
        this(rfc2253String, -1);
    }

    /**
     * special constructor, that overrides the global EncodingType. To use, if
     * during the runtime mixed encodingtypes are needed.
     *
     * @param rfc2253String String of RFC2253 representation.
     * @param encType       The encoding type for strings. If <code>-1</code>, the
     *                      default encoding is used.
     * @throws BadNameException
     */
    public Name(String rfc2253String, int encType) throws BadNameException {
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

        if (encType == -1) {
            if (false) {
                throw new BadNameException(
                        "Use the other constructor with the explicit "
                                + "encoding parameter!");
            }
            currentEncoding_ = defaultEncoding_;
        } else {
            if (encType != UTF8_ENCODING && encType != T61_ENCODING
                    && encType != PRINTABLE_ENCODING && encType != IA5_ENCODING) {
                throw new BadNameException("Unknown EncodingType: " + encType);
            }
            currentEncoding_ = encType;
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
                    throw new BadNameException("Unsupported attribute key: \""
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
                    throw new BadNameException(
                            "Binary data is not a valid BER encoding!");
                }
                seq.add(obj);
            } else {
                val = entry.getValue();

		/*
         * This is a workaround for email addresses which contain the
		 * '@' symbol. This symbol is not in the character set of the
		 * ASN.1 PrintableString. Hence, we have to take a IA5String
		 * instead.
		 */
                if (entry.getKey().equalsIgnoreCase("EMAILADDRESS")
                        || entry.getKey().equalsIgnoreCase("UID")) {
                    seq.add(new ASN1IA5String(val));
                } else if (entry.getKey().equalsIgnoreCase("C")
                        || entry.getKey().equalsIgnoreCase("SERIALNUMBER")) {
                    seq.add(new ASN1PrintableString(val));
                } else {
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
                                throw new BadNameException(
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

    /**
     * Clears this name instance.
     */
    public void clear() {
        super.clear();

        if (tmp_ != null) {
            tmp_.clear();
        }
    }

    /**
     * This method initializes the hashmaps, which are needed to create the
     * ASN1Structure Tree.
     */
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

    /**
     * Check if the given String is printable.
     *
     * @return <code>true</code>, iff the giben String only contains
     * printable characters (letters, digits, or one of " (),-./:=?").
     */
    private boolean checkPrintableSpelling(String val) {
        boolean result;
        char[] allowed;
        char[] value;

        result = true;

        allowed = ("abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789 (),-./:=?").toCharArray();

        value = val.toCharArray();

        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < allowed.length; j++) {
                if (value[i] == allowed[j]) {
                    break;
                }
                if (j == allowed.length - 1) {
                    return false;
                }
            }
        }

        return result;
    }

    /**
     * Returns the String representation. This implementation simply calles
     * {@link #getName getName}.
     *
     * @return The String representation.
     */
    public String toString() {
        return getName();
    }

    /**
     * This method returns the name of this principal. The order is
     * little-endian (least significant attribute first).
     *
     * @return Name of this principal
     */
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

            // only insert a seperator, if another AVA is
            // still in the list
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

    /**
     * This method returns the <code>Name</code> as a list of <code>AVA</code>
     * instances. The order is the same as the order in which the
     * <code>AVA</code> instances appear in the code.
     */
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
		/*
		 * We have to mark siblings. An AVA has a sibling if it is not
		 * the last AVA in the set.
		 */
                sibling = (j < n - 1);

		/*
		 * Convert key and value into strings. These values are then put
		 * into an AVA instance.
		 */
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
		    /*
		     * OK, we have to encode the damn ASN.1 object. Outrageous
		     * inefficient but hey, what choice do we have, if it is not
		     * a string?
		     */
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

    /**
     * Resolves AttributeValueAssertions for the component RDNs of this Name.
     * This method is for internal use only. Do not call it or bad things will
     * happen. You have been warned.
     * <p/>
     * <p/>
     * This method basically registers the AVAs of the RDNs so that the internal
     * Open Types can be discarded after decoding. This makes some objects
     * available for garbage collection that are not required anymore.
     *
     * @param caller The calling RDN.
     * @return The AVA instance that is added to the calling RDN in the decoding
     * process.
     */
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

    /**
     * This method reads the DER encoded ASN.1 sequence into a hashmap. The
     * implementation uses a (perfectly legal) trick. Method
     * {@link #newElement newElement} adds the AttributeValueAssertions
     * instances to a temporary list which is processed at the end of this
     * method. The temporary list is used to eliminate Open Types that are not
     * required any more after decoding in a way that saves us laborious
     * descending in the various depths of the Name.
     * <p/>
     *
     * @param dec The {@link codec.asn1.Decoder Decoder} to use.
     * @throws codec.asn1.ASN1Exception if the expected ANS1Type cannot be found.
     */
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
	/*
	 * We don't need the temporary list anymore.
	 */
        tmp_ = null;
    }

    /**
     * This method adds the given object to this Name if it is a valid RDN (a
     * set with enclosed sequences with an OID and non null attribute value
     * each).
     *
     * @param o The RDN to add.
     * @return <code>true</code>. This method accepts multiple elements which
     * are the same, and adheres to the contract of add(Object) for
     * collections.
     * @throws IllegalArgumentException if the given object is not a valid RDN.
     */
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
	/*
	 * Finally, we can add the RDN.
	 */
        super.add(set);

        return true;
    }

    /**
     * This method returns a new set of AttributeValueAssertions (AVA).
     *
     * @return The new instance to decode.
     */
    public ASN1Type newElement() {
        ASN1SetOf set;

	/*
	 * Here, we add this instance as the resolver of the ASN1SetOf. Upon the
	 * 'resolve' callback, the AVAs are added to the respective RDNs.
	 */
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
            } catch (BadNameException e) {
                return false;
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



    public static Name clone(Name source) throws IllegalArgumentException {
        ASN1Sequence seq;
        Vector sets;
        Name result;

        result = null;

        if (source == null || source.getName().length() == 0) {
            throw new IllegalArgumentException(
                    "Name/Principal must not be null nor empty !");
        }
        seq = source;
        sets = new Vector();

        for (int i = 0; i < seq.size(); i++) {
            sets.add(seq.get(i));
        }
        result = new Name();

        for (int j = 0; j < sets.size(); j++) {
            result.add((ASN1Set) sets.elementAt(j));
        }
        return result;
    }


    public static Name clone(Principal sourcePrincipal)
            throws BadNameException, IllegalArgumentException {

        if (sourcePrincipal == null || sourcePrincipal.getName().length() == 0) {
            throw new IllegalArgumentException(
                    "Name/Principal must not be null nor empty !");
        }
        return new Name(sourcePrincipal.getName(), Name.defaultEncoding_);
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
            } else if ("1.2.840.113549.1.9.8".equals(ava.getKey())) { // UA
                // unstructured
                // adress
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
            nameTable.put(key, new Integer(order));

            if (!entry.hasSibling()) {
                order = order + 1;
            }
        }
        return nameTable;
    }
}
