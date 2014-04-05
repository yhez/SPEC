package codec.asn1;

import java.io.FilterOutputStream;
import java.io.OutputStream;


public abstract class AbstractEncoder extends FilterOutputStream implements
        Encoder {
    public AbstractEncoder(OutputStream out) {
        super(out);
    }

    public void writeType(ASN1Type t){
        t.encode(this);
    }


}
