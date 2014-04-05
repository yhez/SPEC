package codec.x501;

import java.util.ArrayList;
import java.util.Iterator;

import codec.asn1.ASN1OpenType;
import codec.asn1.ASN1RegisteredType;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Set;
import codec.asn1.ASN1SetOf;
import codec.asn1.ASN1Type;
import codec.asn1.Decoder;


public class Attribute extends ASN1Sequence implements ASN1RegisteredType {

    protected ASN1Set values_;


    public void decode(Decoder dec) {
        super.decode(dec);

        if (!(values_ instanceof ASN1SetOf)) {
            return;
        }
        ArrayList list;
        ASN1Type o;
        Iterator i;
            list = new ArrayList(values_.size());

            for (i = values_.iterator(); i.hasNext(); ) {
                o = (ASN1Type) i.next();

                if (o instanceof ASN1OpenType) {
                    o = ((ASN1OpenType) o).getInnerType();
                }
                list.add(o);
            }
            values_.clear();
            values_.addAll(list);

    }
}
