package codec.asn1;

import java.io.IOException;


public interface ASN1Type {

    public Object getValue();

    public void setOptional(boolean optional);

    public boolean isOptional();

    public int getTag();

    public int getTagClass();

    public void setExplicit(boolean explicit);

    public boolean isExplicit();


    public boolean isType(int tag, int tagclass);

    public void encode(Encoder enc) throws ASN1Exception, IOException;

    public void decode(Decoder dec) throws ASN1Exception, IOException;


    public void setConstraint(Constraint o);


    public Constraint getConstraint();


    public void checkConstraints() throws ConstraintException;

}
