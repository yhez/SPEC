package codec.x501;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import codec.Hex;
import codec.UTF8InputStreamReader;


public class RFC2253Parser {

    public static final String TOKENIZERS = " ,+=\"#;\\<>\r\n";

    public static final String SPECIALS = "\"\\,=+<>#;";

    public static final String SEPARATORS = ";,+";

    public static final String HEXCHAR = "0123456789ABCDEFabcdef";

    public static final String ESCAPE = "\\";

    public static final String QUOTE = "\"";

    public static final String PLUS = "+";

    public static final String SPC = " ";

    public static final String LINEBREAK = "\r";

    public static final String RETURN = "\n";

    public static final String WHITESPACE = SPC + LINEBREAK + RETURN;

    public List parse(String rfc2253name) throws Exception {
        UTF8InputStreamReader utfReader;
        ByteArrayOutputStream baos;
        ByteArrayInputStream bais;
        StringTokenizer st;
        StringBuffer value;
        boolean tokParsed;
        boolean utfParsed;
        boolean plus;
        String str;
        String tok;
        String key;
        String val;
        String t;
        byte[] ascii;
        byte[] buf;
        byte[] utf;
        char[] chs;
        byte b;
        int returnState;
        int trunc;
        int state;
        int i;

        trunc = -1;
        returnState = -1;
        state = 0;
        tokParsed = true;
        utfParsed = true;
        key = "";
        value = new StringBuffer();
        tok = "";
        baos = new ByteArrayOutputStream();

        LinkedList ava_ = new LinkedList();

        if (rfc2253name.equals("")) {
            return ava_;
        }
        st = new StringTokenizer(rfc2253name, TOKENIZERS, true);

        while (st.hasMoreTokens() || !tokParsed) {
            if (tokParsed) {
                tok = st.nextToken();
            }

            switch (state) {
                case 0:

                    if (WHITESPACE.contains(tok)) {
                        continue;
                    }
                    if (SPECIALS.indexOf(tok.charAt(0)) < 0) {
                        tok = tok.trim();

                        if (tok.length() >= 4
                                && tok.substring(0, 4).equalsIgnoreCase("OID.")) {
                            tok = tok.substring(4);
                        }

                        if (Character.isDigit(tok.charAt(0))) {
                            chs = tok.toCharArray();

                            for (i = 1; i < chs.length; i++) {
                                if (!Character.isDigit(chs[i]) && !(chs[i] == '.')) {
                                    throw new Exception("(" + state
                                            + ") The key '" + tok
                                            + "' seems to be an OID, but it "
                                            + "contains the illegal character '"
                                            + chs[i] + "'!");
                                }
                            }
                        } else {
                            chs = tok.toCharArray();

                            for (i = 1; i < chs.length; i++) {
                                if (!Character.isDigit(chs[i])
                                        && !Character.isLetter(chs[i])
                                        && chs[i] != '-') {
                                    throw new Exception("(" + state
                                            + ") The key '" + tok
                                            + "' contains the the illegal "
                                            + "character '" + chs[i] + "'!");
                                }
                            }
                        }
                        key = tok;
                        state = 1;

                        continue;
                    }
                    throw new Exception("(" + state
                            + ") Key starts with SPECIAL '" + tok + "'!");

                case 1:

                    if (WHITESPACE.contains(tok)) {
                        continue;
                    }
                    if (tok.equals("=")) {
                        state = 2;
                        continue;
                    }
                    throw new Exception("(" + state
                            + ") '=' expected after '" + key + "'!");

                case 2:

                    if (WHITESPACE.contains(tok)) {
                        continue;
                    }

                    if (tok.equals("#")) {
                        state = 7;

                        continue;
                    }

                    state = 3;

                case 3:

                    if (!tok.equals(ESCAPE) && !utfParsed) {
                        throw new Exception("(" + state
                                + ") Invalid UTF-8 code '"
                                + Hex.encode(baos.toByteArray()) + "'!");
                    }

                    if (!tokParsed) {
                        tokParsed = true;
                    }


                    if (WHITESPACE.contains(tok)) {

                        if (value.length() == 0) {
                            continue;
                        }


                        if (trunc == -1) {
                            trunc = value.length();
                        }

                        value.append(tok);
                        continue;
                    }


                    if (SEPARATORS.indexOf(tok.charAt(0)) >= 0) {

                        if (trunc != -1) {
                            value.setLength(trunc);
                        }


                        val = value.toString();
                        state = 0;
                        plus = tok.equals(PLUS);

                        value.setLength(0);


                        ava_.add(new AVA(key, val, plus));
                        continue;
                    }

                    if (trunc != -1) {
                        trunc = -1;
                    }

                    if (tok.equals(ESCAPE)) {
                        returnState = state;

                        state = 4;
                        continue;
                    }
		    if (tok.equals(QUOTE)) {
                        if (value.length() > 0) {
                            throw new Exception("(" + state
                                    + ") Only whitespace characters "
                                    + "are allowed before the first unescaped "
                                    + "quotation mark (\")!");
                        }
                        state = 5;
                        continue;
                    }

                    if (SPECIALS.indexOf(tok.charAt(0)) < 0) {
                        if (tok.length() > 0) {
                            value.append(tok);
                        }
                        continue;
                    }
                    throw new Exception("(" + state
                            + ") Unquoted special character '" + tok + "' after '"
                            + key + "'!");

                case 4:

                    if (SPECIALS.indexOf(tok.charAt(0)) >= 0) {

                        value.append(tok);

                        state = returnState;
                        continue;
                    }
                    if (tok.length() > 1) {
                        t = tok.substring(0, 2);

                        try {
                            b = (Hex.decode(t))[0];
                        } catch (Exception e) {
                            throw new IllegalArgumentException("(" + state
                                    + ") Invalid hex character '" + t + "'!");
                        }

                        if (b >= 0 && b <= 37) {
                            try {
                                if (baos.size() == 0) {
                                    ascii = new byte[1];
                                    ascii[0] = b;
                                    str = new String(ascii);
                                    value.append(str);
                                } else {
                                    baos.write(b);

                                    throw new Exception("(" + state
                                            + ") Invalid UTF-8 code '"
                                            + Hex.encode(baos.toByteArray()) + "'!");
                                }
                            } catch (IllegalArgumentException iae) {
                                throw new Exception("(" + state
                                        + ") Invalid hex character '" + t + "'!");
                            }
                        } else {

                            try {
                                baos.write(b);
                                utf = baos.toByteArray();
                                bais = new ByteArrayInputStream(utf);

                                utfReader = new UTF8InputStreamReader(bais, 2048);

                                str = utfReader.readLine();
                                value.append(str);

                                baos.reset();
                                utfParsed = true;
                            } catch (Exception e) {
                                if (tok.length() == 2) {
                                    tokParsed = true;
                                    utfParsed = false;

                                    state = returnState;
                                    continue;
                                }
                                throw new Exception("(" + state
                                        + ") Invalid UTF-8 code '"
                                        + Hex.encode(baos.toByteArray()) + "'!");
                            }
                        }

                        if (tok.length() > 2) {
                            tokParsed = false;

                            tok = tok.substring(2);
                        } else {
                            tokParsed = true;
                        }

                        state = returnState;
                        continue;
                    }
                    throw new Exception("(" + state
                            + ") Can't ESCAPE non-special character '"
                            + tok.charAt(0) + "'!");

                case 5:

                    if (!tok.equals(ESCAPE) && !utfParsed) {
                        throw new Exception("(" + state
                                + ") Invalid UTF-8 code '"
                                + Hex.encode(baos.toByteArray()) + "'!");
                    }

                    if (!tokParsed) {
                        tokParsed = true;
                    }
                    if (tok.equals(QUOTE)) {
                        state = 6;
                        continue;
                    }


                    if (tok.equals(ESCAPE)) {
                        returnState = state;

                        state = 4;
                        continue;
                    }

                    value.append(tok);
                    continue;

                case 6:

                    if (SEPARATORS.indexOf(tok.charAt(0)) >= 0) {

                        if (trunc != -1) {
                            value.setLength(trunc);
                        }


                        val = value.toString();
                        state = 0;
                        plus = tok.equals(PLUS);

                        value.setLength(0);


                        ava_.add(new AVA(key, val, plus));
                        continue;
                    }
                    if (!WHITESPACE.contains(tok)) {
                        throw new Exception("(" + state
                                + ") Only whitespace characters are "
                                + "allowed after the second unescaped quotation "
                                + "mark (\")!");
                    }
                    continue;

                case 7:

                    if (SEPARATORS.indexOf(tok.charAt(0)) >= 0) {
                        try {
                            val = value.toString();
                            state = 0;
                            plus = tok.equals(PLUS);
                            buf = Hex.decode(val);

                            value.setLength(0);
                        } catch (Exception e) {
                            throw new Exception("(" + state
                                    + ") Bad hexadecimal code '" + value.toString()
                                    + "'!");
                        }
                        if (buf.length == 0) {
                            throw new Exception("(" + state
                                    + ") Empty hexadecimal code '"
                                    + value.toString() + "'!");
                        }
                        ava_.add(new AVA(key, buf, plus));

                        continue;
                    }

                    if (WHITESPACE.contains(tok)) {
                        if (trunc == -1) {
                            trunc = value.length();
                        }
                        continue;
                    }
                    if (trunc != -1) {
                        throw new Exception("(" + state
                                + ") Non-trailing whitespace characters "
                                + "after hexadecimal code '" + value.toString()
                                + "'!");
                    }

                    if (HEXCHAR.indexOf(tok.charAt(0)) >= 0) {
                        value.append(tok);
                        continue;
                    }
                    throw new Exception("(" + state
                            + ") Bad hexadecimal encoding '" + value.toString()
                            + "'!");

                default:
                    throw new IllegalStateException("(" + state
                            + ") Illegal state!");
            }
        }

        if (!utfParsed) {
            throw new Exception("(" + state + ") Invalid UTF-8 code '"
                    + Hex.encode(baos.toByteArray()) + "'!");
        }
        if (trunc != -1) {
            value.setLength(trunc);
        }


        if (state != 2 && state != 3 && state != 6 && state != 7) {
            throw new Exception("(" + state + ") Not in a final state!");
        }
        switch (state) {
            case 7:
                try {
                    val = value.toString();
                    buf = Hex.decode(val);
                } catch (Exception e) {
                    throw new Exception("(" + state
                            + ") Bad hexadecimal code '" + value.toString() + "'!");
                }
                if (buf.length == 0) {
                    throw new Exception("(" + state
                            + ") Empty hexadecimal code '" + value.toString()
                            + "'!");
                }
                ava_.add(new AVA(key, buf, false));
                break;

            case 6:
                val = value.toString();

                ava_.add(new AVA(key, val, false));
                break;

            case 3:
                val = value.toString();

                ava_.add(new AVA(key, val, false));
                break;

            case 2:
                ava_.add(new AVA(key, "", false));
                break;
        }
        return ava_;
    }
}
