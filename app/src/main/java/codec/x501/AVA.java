package codec.x501;

import codec.Hex;


public class AVA {
    private String key_;
    private String val_;
    private byte[] buf_;
    private boolean sib_;


    public AVA(String key, String value, boolean hasSibling) {
        key_ = key;
        val_ = value;
        sib_ = hasSibling;
    }


    public AVA(String key, byte[] buf, boolean hasSibling) {
        key_ = key;
        buf_ = buf;
        sib_ = hasSibling;
    }


    public String getKey() {
        return key_;
    }


    public String getValue() {
        if (val_ == null && isEncodedValue()) {
            val_ = Hex.encode(buf_);
        }
        return val_;
    }


    public boolean hasSibling() {
        return sib_;
    }


    public boolean isEncodedValue() {
        return (buf_ != null);
    }


    public byte[] getEncodedValue() {
        return buf_;
    }


    public String toString() {
        String output = key_ + "=";

        if (isEncodedValue()) {
            output += "#";
        }
        output += getValue();
        return output;
    }


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
