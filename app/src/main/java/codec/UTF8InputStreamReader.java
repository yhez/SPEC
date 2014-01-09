package codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;


public class UTF8InputStreamReader extends Reader {
    protected int numbytes_ = 0;
    protected int hasExtra_ = 0;
    protected char extraCh_ = 0;
    protected int byteptr_ = 0;
    protected InputStream ins_;
    protected byte[] bytebuf_;

    public UTF8InputStreamReader(InputStream i, int bufsize) {

        ins_ = i;
        bytebuf_ = new byte[bufsize];
    }

    public String getEncoding() {
        return "UTF8";
    }
    protected void checkOpen() throws IOException {
        if (ins_ == null) {
            throw new IOException("Stream closed");
        }
    }
    public int read() throws IOException {
        synchronized (lock) {
            return translate();
        }
    }
    public int read(char cbuf[], int off, int len) throws IOException {
        int result;
        int end;
        int ch;

        end = off + len;

        if ((len < 0) || (off < 0) || (cbuf.length < off) || (end < 0)
                || (cbuf.length < end)) {
            throw new IndexOutOfBoundsException();
        }
        result = 0;

        synchronized (lock) {
            checkOpen();

            if (len == 0) {
                return 0;
            }

            for (; len > 0; --len) {
                ch = translate();

                if (ch < 0) {
                    break;
                }
                cbuf[off++] = (char) ch;
                ++result;
            }
        }
        return (result == 0) ? -1 : result;
    }
    public String readLine() throws IOException {
        StringBuffer s;
        int ch2;
        int ch;

        s = null;

        synchronized (lock) {
            checkOpen();

            for (; ; ) {
                ch = translate();

                if (ch < 0) // eof
                {
                    return (s == null) ? null : s.toString();
                }

                if (ch == '\n') {
                    break;
                }
                if (ch == '\r') {
                    ch2 = translate();
                    if ((ch2 != '\n') && (ch2 >= 0)) {
                        hasExtra_ = 1;
                        // put back
                        extraCh_ = (char) ch2;
                    }
                    break;
                }
                if (s == null) {
                    s = new StringBuffer(80);
                }
                s.append((char) ch);
            }
        }
        return (s == null) ? "" : s.toString();
    }
    public boolean ready() throws IOException {
        synchronized (lock) {
            checkOpen();

            try {
                return (hasExtra_ > 0) || (numbytes_ > byteptr_)
                        || (ins_.available() > 0);
            } catch (IOException e1) {
                return false;
            }
        }
    }

    public void close() throws IOException {
        synchronized (lock) {
            if (ins_ != null) {
                ins_.close();
                ins_ = null;
                bytebuf_ = null;
            }
        }
    }

    private int morebyte() throws IOException {
        if (byteptr_ < numbytes_) {
            return 0xff & bytebuf_[byteptr_++];
        }
        byteptr_ = 0;

        // fill buffer from underlying stream
        numbytes_ = ins_.read(bytebuf_);

        if (numbytes_ > 0) {
            return 0xff & bytebuf_[byteptr_++];
        }
        numbytes_ = 0;

        // hit EOF
        return -1;
    }

    private int translate() throws IOException {
        int char1;
        int char2;
        int char3;
        int char4;
        int a4;

        if (hasExtra_ > 0) {
            hasExtra_ = 0;
            return extraCh_;
        }
        char1 = morebyte();

        if (char1 < 0) {
            // EOF
            return char1;
        }

        if (0 == (char1 & 0x80)) {
            // 1 byte UTF
            return char1;
        }

        switch (char1 >> 4) {
            case 0xc:

            case 0xd:
                char2 = morebyte();
                if (char2 < 0) {
                    // EOF
                    characterDecodingException();
                }

                if ((char2 & 0xc0) != 0x80) {
                    characterDecodingException();
                }
                // 2 byte UTF
                return ((char1 & 0x1f) << 6) | (char2 & 0x3f);

            case 0xe:
                char2 = morebyte();

                if (char2 < 0) {
                    // EOF
                    characterDecodingException();
                }

                char3 = morebyte();

                if (char3 < 0) {
                    // EOF
                    characterDecodingException();
                }

                if (((char2 & 0xc0) != 0x80) || ((char3 & 0xc0) != 0x80)) {
                    // EOF
                    characterDecodingException();
                }
                // 3 byte UTF
                return ((char1 & 0xf) << 12) | ((char2 & 0x3f) << 6)
                        | (char3 & 0x3f);

            case 0xf: {
                // 4 byte UTF
                char2 = morebyte();

                if (char2 < 0) {
                    // EOF
                    characterDecodingException();
                }

                char3 = morebyte();
                if (char3 < 0) {
                    // EOF
                    characterDecodingException();
                }

                char4 = morebyte();
                if (char4 < 0) {
                    // EOF
                    characterDecodingException();
                }

                if (((char2 & 0xc0) != 0x80) || ((char3 & 0xc0) != 0x80)
                        || ((char4 & 0xc0) != 0x80)) {
                    // EOF
                    characterDecodingException();
                }
                a4 = ((char1 & 0x7) << 18) | ((char2 & 0x3f) << 12)
                        | ((char3 & 0x3f) << 6) | (char4 & 0x3f);

                hasExtra_ = 1;
                extraCh_ = (char) ((a4 - 0x10000) % 0x400 + 0xdc00);

                return (char) ((a4 - 0x10000) / 0x400 + 0xd800);
            }
            default:
                throw new IOException("Character decoding exception.");
        }
    }

    protected void characterDecodingException() throws IOException {
        throw new IOException("Character decoding exception.");
    }
}
