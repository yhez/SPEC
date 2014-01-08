package codec.x501;

import codec.Hex;


public class AVA {
    /**
     * The attribute key of the AVA.
     */
    private String key_;

    /**
     * The string value of the AVA, if given.
     */
    private String val_;

    /**
     * The encoded value of the AVA, if given.
     */
    private byte[] buf_;

    /**
     * Flag indicating, whether the AVA has a continuation at the same level.
     */
    private boolean sib_;

    /**
     * Creates an instance.
     *
     * @param key        The attribute key.
     * @param value      The attribute value.
     * @param hasSibling <code>true</code> iff this AVA is followed by another
     *                   AVA at the same level. In other words, a value of true
     *                   signals that this AVA is one in a sequence of AVAs of a
     *                   multi valued RDN.
     */
    public AVA(String key, String value, boolean hasSibling) {
        key_ = key;
        val_ = value;
        sib_ = hasSibling;
    }

    /**
     * Creates an instance with a DER encoded value.
     *
     * @param key        The attribute key.
     * @param buf        The encoded attribute value.
     * @param hasSibling <code>true</code> iff this AVA is followed by another
     *                   AVA at the same level. In other words, a value of true
     *                   signals that this AVA is one in a sequence of AVAs of a
     *                   multi valued RDN.
     */
    public AVA(String key, byte[] buf, boolean hasSibling) {
        key_ = key;
        buf_ = buf;
        sib_ = hasSibling;
    }

    /**
     * Returns the attribute key of the AVA
     *
     * @return The attribute key of the AVA
     */
    public String getKey() {
        return key_;
    }

    /**
     * Returns the string value of the AVA. If only an encoded value is given,
     * this value is transformed first.
     *
     * @return the string value of the AVA.
     */
    public String getValue() {
        if (val_ == null && isEncodedValue()) {
            val_ = Hex.encode(buf_);
        }
        return val_;
    }

    /**
     * @return <code>true</code> if this AVA is followed by another one that
     * was separated from this one by means of a plus sign. In other
     * words, this AVA and the next belong to the same RDN.
     */
    public boolean hasSibling() {
        return sib_;
    }

    /**
     * Returns the status of the attribute value.
     *
     * @return <code>true</code> if the attribute value is a byte array.
     */
    public boolean isEncodedValue() {
        return (buf_ != null);
    }

    /**
     * Returns the encoded value of the AVA, if given.
     *
     * @return the encoded value of the AVA.
     */
    public byte[] getEncodedValue() {
        return buf_;
    }

    /**
     * Returns the string representation of the AVA.
     *
     * @return the string representation of the AVA.
     */
    public String toString() {
        String output = key_ + "=";

        if (isEncodedValue()) {
            output += "#";
        }
        output += getValue();
        return output;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the given
     * object; <code>false</code> otherwise.
     */
    public boolean equals(Object o) {
        AVA entry;

        if (o == null) {
            return false;
        }

        if (!(o instanceof AVA)) {
            return false;
        }
        entry = (AVA) o;

        if (getKey().equals(entry.getKey())) {
            if (getValue().equals(entry.getValue())) {
                return true;
            }
        }
        return false;
    }
}
