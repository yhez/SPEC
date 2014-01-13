package codec.x501;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import codec.Hex;
import codec.UTF8InputStreamReader;


public class RFC2253Parser {

    /**
     * Characters that are treated in a special way. Some of them must be
     * escaped or quoted, some serve as delimiters and more.
     */
    public static final String TOKENIZERS = " ,+=\"#;\\<>\r\n";

    /**
     * Characters that are treated in a special way. Some of them must be
     * escaped or quoted, some serve as delimiters and more.
     */
    public static final String SPECIALS = "\"\\,=+<>#;";

    /**
     * Delimiters for the <code>StringTokenizer</code>.
     */
    public static final String SEPARATORS = ";,+";

    /**
     * Valid hex characters.
     */
    public static final String HEXCHAR = "0123456789ABCDEFabcdef";

    /**
     * The pair introducer.
     */
    public static final String ESCAPE = "\\";

    /**
     * The quote character.
     */
    public static final String QUOTE = "\"";

    public static final String PLUS = "+";

    /**
     * The space character.
     */
    public static final String SPC = " ";

    /**
     * The line break character.
     */
    public static final String LINEBREAK = "\r";

    /**
     * The carriage return character.
     */
    public static final String RETURN = "\n";

    /**
     * Whitespace characters.
     */
    public static final String WHITESPACE = SPC + LINEBREAK + RETURN;

    public List parse(String rfc2253name) throws BadNameException {
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
        /*
         * We consume whitespace characters and wait for an attribute
		 * keyword in this state.
		 */
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
                                    throw new BadNameException("(" + state
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
                                    throw new BadNameException("(" + state
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
                    throw new BadNameException("(" + state
                            + ") Key starts with SPECIAL '" + tok + "'!");

                case 1:
		/*
		 * We again consume whitespace characters until we encounter an
		 * equals sign ('='). Then we advance our state.
		 */
                    if (WHITESPACE.contains(tok)) {
                        continue;
                    }
                    if (tok.equals("=")) {
                        state = 2;
                        continue;
                    }
                    throw new BadNameException("(" + state
                            + ") '=' expected after '" + key + "'!");

                case 2:
		/*
		 * We again consume whitespace characters until we hit the first
		 * non-space character. In that case we emulate an epsilon
		 * transition to state number 3. In other words, we fall through
		 * with the current token still being valid.
		 */
                    if (WHITESPACE.contains(tok)) {
                        continue;
                    }
		/*
		 * If the first token is a hash mark ('#') then we have n
		 * hexadecimal encoding that is treated in state 7.
		 */
                    if (tok.equals("#")) {
                        state = 7;

                        continue;
                    }
		/*
		 * Fall through, new state, token stays valid.
		 */
                    state = 3;

                case 3:
		/*
		 * The central state. It distinguishes between quoted and
		 * unquoted substrings, handles the truncation counter for
		 * trailing whitespace characters and more.
		 */
                    if (!tok.equals(ESCAPE) && !utfParsed) {
                        throw new BadNameException("(" + state
                                + ") Invalid UTF-8 code '"
                                + Hex.encode(baos.toByteArray()) + "'!");
                    }

                    if (!tokParsed) {
                        tokParsed = true;
                    }

		/*
		 * Check if we hit a whitespace character. This is a nasty case
		 * because the DN could be 'cn= "foo" ,...' which must be
		 * transformed into 'cn=foo'. that means all whitespace
		 * characters before the delimiter must be truncated.
		 */
                    if (WHITESPACE.contains(tok)) {
		    /*
		     * Ignore leading whitespace characters.
		     */
                        if (value.length() == 0) {
                            continue;
                        }

		    /*
		     * Remember start of trailing whitespace characters.
		     */
                        if (trunc == -1) {
                            trunc = value.length();
                        }

                        value.append(tok);
                        continue;
                    }

		/*
		 * If we hit upon a RDN separator then we can ship out the
		 * attribute and value.
		 */
                    if (SEPARATORS.indexOf(tok.charAt(0)) >= 0) {
		    /*
		     * Remove trailing whitespace characters, if existent
		     */
                        if (trunc != -1) {
                            value.setLength(trunc);
                        }

		    /*
		     * If the separator is a PLUS then we have to set the flag
		     * that says: "this AVA is followed by another one at the
		     * same level."
		     */
                        val = value.toString();
                        state = 0;
                        plus = tok.equals(PLUS);

                        value.setLength(0);

		    /*
		     * We got a key and an empty value. Now we ship it out and
		     * go on in state 0.
		     */
                        ava_.add(new AVA(key, val, plus));
                        continue;
                    }

                    if (trunc != -1) {
                        trunc = -1;
                    }

		/*
		 * An ESCAPE brings us into a state that simply returns after
		 * having read the escaped special character.
		 */
                    if (tok.equals(ESCAPE)) {
                        returnState = state;

                        state = 4;
                        continue;
                    }
		/*
		 * If we hit upon a QUOTE then we have to parse a quoted
		 * substring. The state for that simply returns as well.
		 */
                    if (tok.equals(QUOTE)) {
                        if (value.length() > 0) {
                            throw new BadNameException("(" + state
                                    + ") Only whitespace characters "
                                    + "are allowed before the first unescaped "
                                    + "quotation mark (\")!");
                        }
                        state = 5;
                        continue;
                    }
		/*
		 * Last not least, we check for a special character that is not
		 * escaped. If there isn't then we have plain chars that we
		 * append to the current value.
		 */
                    if (SPECIALS.indexOf(tok.charAt(0)) < 0) {
                        if (tok.length() > 0) {
                            value.append(tok);
                        }
                        continue;
                    }
                    throw new BadNameException("(" + state
                            + ") Unquoted special character '" + tok + "' after '"
                            + key + "'!");

                case 4:
		/*
		 * This state handles escaped SPECIAL characters, backslashes
		 * (\), quotations ("), and UTF-8 code. It returns to the set
		 * 'returnState'.
		 */
                    if (SPECIALS.indexOf(tok.charAt(0)) >= 0) {
		    /*
		     * We got an escaped special character, so we append it to
		     * the value.
		     */
                        value.append(tok);

                        state = returnState;
                        continue;
                    }
                    if (tok.length() > 1) {
                        t = tok.substring(0, 2);
		    /*
		     * we first have to check whether we are in a situation like
		     * 'CN=Before\0DAfter' (escaped non printable ascii
		     * character)
		     */
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

                                    throw new BadNameException("(" + state
                                            + ") Invalid UTF-8 code '"
                                            + Hex.encode(baos.toByteArray()) + "'!");
                                }
                            } catch (IllegalArgumentException iae) {
                                throw new BadNameException("(" + state
                                        + ") Invalid hex character '" + t + "'!");
                            }
                        } else {
			/*
			 * now we have to try to parse the UTF-8 code
			 */
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
                                throw new BadNameException("(" + state
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
                    throw new BadNameException("(" + state
                            + ") Can't ESCAPE non-special character '"
                            + tok.charAt(0) + "'!");

                case 5:
		/*
		 * This state means, that we are parsing a quoted value, upon
		 * hitting another unescaped quote.
		 */
                    if (!tok.equals(ESCAPE) && !utfParsed) {
                        throw new BadNameException("(" + state
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

		/*
		 * If we read an escape character then we again have to call a
		 * substate for handling that.
		 */
                    if (tok.equals(ESCAPE)) {
                        returnState = state;

                        state = 4;
                        continue;
                    }
		/*
		 * Since we are in quotation marks, we add what we find to the
		 * current value.
		 */
                    value.append(tok);
                    continue;

                case 6:
		/*
		 * This state means, that we have found the closing unescaped
		 * quotation mark, wait for a RDN separator, and handle
		 * whitespace characters.
		 */
                    if (SEPARATORS.indexOf(tok.charAt(0)) >= 0) {
		    /*
		     * Remove trailing whitespace characters, if existent
		     */
                        if (trunc != -1) {
                            value.setLength(trunc);
                        }

		    /*
		     * If the separator is a PLUS then we have to set the flag
		     * that says: "this AVA is followed by another one at the
		     * same level."
		     */
                        val = value.toString();
                        state = 0;
                        plus = tok.equals(PLUS);

                        value.setLength(0);

		    /*
		     * We got a key and an empty value. Now we ship it out and
		     * go on in state 0.
		     */
                        ava_.add(new AVA(key, val, plus));
                        continue;
                    }
                    if (!WHITESPACE.contains(tok)) {
                        throw new BadNameException("(" + state
                                + ") Only whitespace characters are "
                                + "allowed after the second unescaped quotation "
                                + "mark (\")!");
                    }
                    continue;

                case 7:
		/*
		 * This state decodes a string that represents a binary value.
		 * If we hit upon a separator then we ship out the accumulated
		 * hexadecimal string.
		 */
                    if (SEPARATORS.indexOf(tok.charAt(0)) >= 0) {
                        try {
                            val = value.toString();
                            state = 0;
                            plus = tok.equals(PLUS);
                            buf = Hex.decode(val);

                            value.setLength(0);
                        } catch (Exception e) {
                            throw new BadNameException("(" + state
                                    + ") Bad hexadecimal code '" + value.toString()
                                    + "'!");
                        }
                        if (buf.length == 0) {
                            throw new BadNameException("(" + state
                                    + ") Empty hexadecimal code '"
                                    + value.toString() + "'!");
                        }
                        ava_.add(new AVA(key, buf, plus));

                        continue;
                    }

		/*
		 * Remove trailing spaces.
		 */
                    if (WHITESPACE.contains(tok)) {
                        if (trunc == -1) {
                            trunc = value.length();
                        }
                        continue;
                    }
                    if (trunc != -1) {
                        throw new BadNameException("(" + state
                                + ") Non-trailing whitespace characters "
                                + "after hexadecimal code '" + value.toString()
                                + "'!");
                    }
		/*
		 * We check for specials now. If we do not hit one then we
		 * assume everything is fine and we go on. The hexadecimal
		 * encoding is checked when we ship out the string.
		 */
                    if (HEXCHAR.indexOf(tok.charAt(0)) >= 0) {
                        value.append(tok);
                        continue;
                    }
		/*
		 * Everything else is an error.
		 */
                    throw new BadNameException("(" + state
                            + ") Bad hexadecimal encoding '" + value.toString()
                            + "'!");

                default:
                    throw new IllegalStateException("(" + state
                            + ") Illegal state!");
            }
        }

        if (!utfParsed) {
            throw new BadNameException("(" + state + ") Invalid UTF-8 code '"
                    + Hex.encode(baos.toByteArray()) + "'!");
        }
        if (trunc != -1) {
            value.setLength(trunc);
        }

	/*
	 * We first check if the state machine is in a final state.
	 */
        if (state != 2 && state != 3 && state != 6 && state != 7) {
            throw new BadNameException("(" + state + ") Not in a final state!");
        }
	/*
	 * We have to check for the epsilon transitions of the final states.
	 */
        switch (state) {
            case 7:
                try {
                    val = value.toString();
                    buf = Hex.decode(val);
                } catch (Exception e) {
                    throw new BadNameException("(" + state
                            + ") Bad hexadecimal code '" + value.toString() + "'!");
                }
                if (buf.length == 0) {
                    throw new BadNameException("(" + state
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

    /**
     * Main method of the class.
     *
     * @param argv
     *                a sequence of RFC2253 strings (e.g. "CN=DE")
     */

}
