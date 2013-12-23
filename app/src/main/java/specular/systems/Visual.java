package specular.systems;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
        if (data == null || data.length() % 2 != 0)
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
            et.setSelection(et.getText().length());
            et.setKeyListener(StaticVariables.edit);
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.requestFocus();
            et.setFilters(filters());
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

    public static InputFilter[] filters() {
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

    public static ImageButton glow(Drawable drawable, Activity a) {
        // An added margin to the initial image
        int margin = 24;
        int halfMargin = margin / 2;

        // the glow radius
        int glowRadius = 16;

        // the glow color
        int glowColor = Color.rgb(0, 192, 255);

        // The original image to use

        Bitmap src = ((BitmapDrawable) drawable).getBitmap();

        // extract the alpha from the source image
        Bitmap alpha = src.extractAlpha();

        // The output bitmap (with the icon + glow)
        Bitmap bmp = Bitmap.createBitmap(src.getWidth() + margin,
                src.getHeight() + margin, Bitmap.Config.ARGB_8888);

        // The canvas to paint on the image
        Canvas canvas = new Canvas(bmp);

        Paint paint = new Paint();
        paint.setColor(glowColor);

        // outer glow
        paint.setMaskFilter(new BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.OUTER));
        canvas.drawBitmap(alpha, halfMargin, halfMargin, paint);

        // original icon
        canvas.drawBitmap(src, halfMargin, halfMargin, null);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new BitmapDrawable(a.getResources(), bmp));
        states.addState(new int[]{}, drawable);
        ImageButton ib = new ImageButton(a);
        ib.setImageDrawable(states);
        ib.setBackgroundColor(Color.TRANSPARENT);
        return ib;
    }
    public static String getNameReprt(){
        String timestamp = new SimpleDateFormat("MM-dd HH-mm-ss").format(Calendar
                .getInstance().getTime());
        return timestamp+".stacktrace";
    }
    public static String getSize(long numBytes) {
        double size=numBytes;
        String unit = "byte";
        if (size > 1023) {
            size /= 1024;
            unit = "KB";
        }
        if (size > 1023) {
            size /= 1024;
            unit = "MB";
        }
        if (size > 1023) {
            size /= 1024;
            unit = "GB";
        }
        String total = (size + "").split("\\.")[0];
        if ((size + "").split("\\.").length > 1) {
            int l = (size+"").split("\\.")[1].length();
            total += "." + (size + "").split("\\.")[1].substring(0, Math.min(2,l));
        }
        return total + " " + unit;
    }
    public static String getFileName(Activity a,Uri contentURI) {
        Cursor cursor = a.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getLastPathSegment();
        }
        cursor.moveToFirst();
        String rs = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
        if (!rs.contains(".")) {
            rs += "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(
                    cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)));
        }
        cursor.close();
        return rs;
    }
}
