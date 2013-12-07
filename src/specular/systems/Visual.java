package specular.systems;

import android.app.Activity;
import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class Visual {
    private static final String HEXES = "0123456789ABCDEF";

    public static String bin2hex(byte[] raw) {
        if (raw == null)
            return null;
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    // return bin data from hexadecimal string
    public static byte[] hex2bin(String data) {
        if (data==null||data.length() % 2 != 0)
            return null;
        byte hexa[] = data.getBytes();
        byte bin[] = new byte[hexa.length / 2];
        for (int a = 0; a < hexa.length; a++) {
            byte tmp1 = hexa[a];
            if (tmp1 <= '9' && tmp1 >= '0')
                tmp1 -= '0';
            else if (tmp1 <= 'F' && tmp1 >= 'A')
                tmp1 -= 'A' - 10;
            else
                return null;
            byte tmp2 = hexa[a + 1];
            if (tmp2 <= '9' && tmp2 >= '0')
                tmp2 -= '0';
            else if (tmp2 <= 'F' && tmp2 >= 'A')
                tmp2 -= 'A' - 10;
            else
                return null;
            int n = tmp1 * 16 + tmp2;
            a++;
            bin[a / 2] = (byte) n;
        }
        return bin;
    }

    public static void setAllFonts(Activity act, ViewGroup v) {
        for (int a = 0; a < v.getChildCount(); a++)
            try {
                setAllFonts(act, (ViewGroup) v.getChildAt(a));
            } catch (Exception e) {
                try {
                    ((TextView) v.getChildAt(a)).setTypeface(FilesManagement.getOs(act));
                } catch (Exception ignore) {
                }
            }
    }

    public static void edit(Activity a, EditText et, ImageButton ib) {
        if (et.getKeyListener() == null) {
            ib.setImageResource(R.drawable.save);
            et.setKeyListener(PublicStaticVariables.edit);
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.requestFocus();
            et.setFilters(filters());
            et.setSelection(et.getText().length());
            InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(et, 0);
        } else {
            ib.setImageResource(R.drawable.edit);
            et.setKeyListener(null);
            et.setFocusable(false);
            InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        }
    }
    public static InputFilter[] filters(){
        final char[] dang = "|\\?*<\":>+[]/'".toCharArray();
        InputFilter[] filter = new InputFilter[2];
        filter[0] = new InputFilter.LengthFilter(40);
        filter[1] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    for (char c : dang)
                        if (source.charAt(i) == c) {
                            return "";
                        }
                }
                return null;
            }
        };
        return filter;
    }
}
