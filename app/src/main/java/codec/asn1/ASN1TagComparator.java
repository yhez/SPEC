package codec.asn1;

import java.util.Comparator;



public class ASN1TagComparator implements Comparator {

    public int compare(Object o1, Object o2) {

        ASN1Type a1 = (ASN1Type) o1;
        ASN1Type a2 = (ASN1Type) o2;

        if (a1.getTagClass() > a2.getTagClass())
            return 1;
        if (a1.getTagClass() == a2.getTagClass()) {
            if (a1.getTag() > a2.getTag()) {
                return 1;
            }
            if (a1.getTag() == a2.getTag()) {
                return 0;
            }
            return -1;
        }
        return -1;
    }

}