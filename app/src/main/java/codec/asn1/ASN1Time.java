package codec.asn1;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


abstract public class ASN1Time extends ASN1VisibleString {

    private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

    protected static final String ZEROES = "0000";


    protected Date date_;


    public Date getDate() {
        return (Date) date_.clone();
    }


    public long getTime() {
        return date_.getTime();
    }

    public void setDate(Calendar calendar) {
        if (calendar == null) {
            throw new NullPointerException("calendar");
        }
        date_ = calendar.getTime();

        setString0(toString(date_));
    }

    public void setDate(Date date) {
        if (date == null) {
            throw new NullPointerException("date");
        }
        date_ = (Date) date.clone();

        setString0(toString(date_));
    }


    public void setDate(long time) {
        date_ = new Date(time);

        setString0(toString(date_));
    }

    public void setDate(String date) {
        if (date == null) {
            throw new NullPointerException("date string");
        }
        date_ = toDate(date);

        setString0(toString(date_));
    }

    public void setString(String s) {
        date_ = toDate(s);

        setString0(s);
    }

    public void encode(Encoder enc) throws ASN1Exception, IOException {
        enc.writeTime(this);
    }

    public void decode(Decoder enc) throws ASN1Exception, IOException {
        enc.readTime(this);
    }

    abstract protected int[] getFields();

    abstract protected int[] getFieldLengths();

    abstract protected int[] getFieldCorrections();


    protected String toString(Date date) {
        StringBuffer buf;
        Calendar cal;
        String s;
        int[] lengths;
        int[] correct;
        int[] fields;
        int len;
        int w;
        int n;
        int v;
        int lastzero;

        if (date == null) {
            throw new NullPointerException("date");
        }
        cal = new GregorianCalendar(TZ);
        fields = getFields();
        correct = getFieldCorrections();
        lengths = getFieldLengths();
        buf = new StringBuffer(20);

        cal.setTime(date);

        for (n = 0; n < fields.length; n++) {
            v = cal.get(fields[n]) - correct[n];
            s = String.valueOf(v);
            len = s.length();

            w = lengths[n];

            if (w == 0) {
                if (v > 0) {

                    buf.append(".");
                    s = ZEROES.substring(0, 3 - s.length()) + s;

                    if (s.charAt(s.length() - 1) != '0') {
                        buf.append(s);
                    } else {
                        lastzero = s.length() - 1;
                        while ((lastzero > 0)
                                && (s.charAt(lastzero - 1) == '0')) {
                            lastzero--;
                        }
                        buf.append(s.substring(0, lastzero));
                    }
                }
                continue;
            }

            if (w < 0) {
                w = -w;
            }
            if (len < w) {
                buf.append(ZEROES.substring(0, w - len));
                buf.append(s);
            }

            else if (len > w) {
                buf.append(s.substring(len - w));
            }

            else {
                buf.append(s);
            }
        }
        buf.append('Z');

        return buf.toString();
    }


    protected Date toDate(String code) {
        Calendar cal;
        Calendar res;
        TimeZone tz;
        int[] lengths;
        int[] correct;
        int[] fields;
        String s;
        int pos;
        int len;
        int n;
        int w;
        int v;
        int c;

        if (code == null) {
            throw new NullPointerException("code");
        }
        cal = new GregorianCalendar(TZ);
        cal.setTime(new Date(0));
        fields = getFields();
        correct = getFieldCorrections();
        lengths = getFieldLengths();
        len = code.length();

        for (pos = 0, n = 0; n < fields.length; n++) {

            w = lengths[n];

            if (w == 0) {

                if (pos >= len) {
                    continue;
                }
                c = code.charAt(pos);

                if (c != '.' && c != ',') {
                    continue;
                }
                pos++;

                for (v = 0; (v < 3 && pos < len); v++) {
                    if (!Character.isDigit(code.charAt(pos))) {
                        break;
                    }
                    pos++;
                }

                if (v == 0) {
                    throw new IllegalArgumentException(
                            "Milliseconds format error!");
                }
                s = code.substring(pos - v, pos);

                if (v < 3) {
                    s = s + ZEROES.substring(0, 3 - v);
                }
                v = Integer.parseInt(s);
                v = v + correct[n];

                cal.set(fields[n], v);

                continue;
            }
            if (w < 0) {
                w = -w;

                if (pos >= len || !Character.isDigit(code.charAt(pos))) {
                    continue;
                }
            }
            s = code.substring(pos, pos + w);
            v = Integer.parseInt(s);
            v = v + correct[n];
            pos = pos + w;

            if (fields[n] == Calendar.YEAR && lengths[n] == 2) {
                v = v + ((v < 70) ? 2000 : 1900);
            }
            cal.set(fields[n], v);
        }
        if (pos < len) {
            c = code.charAt(pos);

            if (c == '+' || c == '-') {
                s = code.substring(pos, pos + 5);
                tz = TimeZone.getTimeZone("GMT" + s);
                pos = pos + 5;
            }
            else if (code.charAt(pos) != 'Z') {
                throw new IllegalArgumentException(
                        "Illegal char in place of 'Z' (" + pos + ")");
            }
            else {
                tz = TimeZone.getTimeZone("GMT");
                pos++;
            }
        }
        else {
            tz = TimeZone.getDefault();
        }
        if (pos != len) {
            throw new IllegalArgumentException(
                    "Trailing characters after encoding! (" + pos + ")");
        }
        res = Calendar.getInstance(tz);
        res.setTime(new Date(0));

        for (n = 0; n < fields.length; n++) {
            res.set(fields[n], cal.get(fields[n]));
        }
        return res.getTime();
    }

}
