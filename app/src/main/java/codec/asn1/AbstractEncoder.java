package codec.asn1;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public abstract class AbstractEncoder extends FilterOutputStream implements
        Encoder {
    public AbstractEncoder(OutputStream out) {
        super(out);
    }

    public void writeType(ASN1Type t) throws ASN1Exception, IOException {
        t.encode(this);
    }


}
