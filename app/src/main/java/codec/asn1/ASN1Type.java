package codec.asn1;

public interface ASN1Type {

    public Object getValue();

    public void setOptional(boolean optional);

    public boolean isOptional();

    public int getTag();

    public int getTagClass();

    public void setExplicit(boolean explicit);

    public boolean isExplicit();


    public boolean isType(int tag, int tagclass);

    public void encode(Encoder enc);

    public void decode(Decoder dec);


    public void setConstraint(Constraint o);


    public void checkConstraints();

}
