package codec.asn1;

import java.util.Calendar;
import java.util.Date;


public class ASN1GeneralizedTime extends ASN1Time {

    private static final int[] FIELDS = {Calendar.YEAR, Calendar.MONTH,
            Calendar.DATE, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
            Calendar.SECOND, Calendar.MILLISECOND};

    private static final int[] LENGTHS = {4, 2, 2, 2, 2, -2, 0};


    private static final int[] CORRECT = {0, -1, 0, 0, 0, 0, 0};

    public ASN1GeneralizedTime() {
        setDate(new Date(0));
    }


    protected int[] getFields() {
        return FIELDS.clone();
    }

    protected int[] getFieldLengths() {
        return LENGTHS.clone();
    }

    protected int[] getFieldCorrections() {
        return CORRECT.clone();
    }

    public int getTag() {
        return ASN1.TAG_GENERALIZEDTIME;
    }

}
