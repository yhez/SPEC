package codec.asn1;

import java.util.Calendar;
import java.util.Date;

public class ASN1UTCTime extends ASN1Time {

    private static final int[] FIELDS = {Calendar.YEAR, Calendar.MONTH,
            Calendar.DATE, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
            Calendar.SECOND};


    private static final int[] LENGTHS = {2, 2, 2, 2, 2, -2};


    private static final int[] CORRECT = {0, -1, 0, 0, 0, 0};

    public ASN1UTCTime() {
        setDate(new Date(0));
    }

    public ASN1UTCTime(Calendar cal) {
        setDate(cal);
    }

    public int getTag() {
        return ASN1.TAG_UTCTIME;
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

    public void setDate(Calendar calendar) {
        if (calendar == null) {
            throw new NullPointerException("calendar");
        }
        date_ = new Date((calendar.getTime().getTime() / 1000) * 1000);

        setString0(toString(date_));
    }

    public void setDate(Date date) {
        if (date == null) {
            throw new NullPointerException("date");
        }
        date_ = new Date((date.getTime() / 1000) * 1000);

        setString0(toString(date_));
    }
}
