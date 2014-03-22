package specular.systems;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;

import java.io.IOException;
import java.nio.charset.Charset;

public class NfcStuff {

    public static boolean nfcIsntAvailable(Activity a) {
        return NfcAdapter.getDefaultAdapter(a) == null;
    }

    public static boolean nfcIsOff(Activity a) {
        NfcAdapter n = NfcAdapter.getDefaultAdapter(a);
        return n != null && !n.isEnabled();
    }

    public static void listen(Activity activity, Class c) {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            PendingIntent pi = PendingIntent.getActivity(activity, 0,
                    new Intent(activity, c)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
            );
            IntentFilter tagDetected = new IntentFilter(
                    NfcAdapter.ACTION_TAG_DISCOVERED);
            IntentFilter[] filters = new IntentFilter[]{tagDetected};
            nfcAdapter.enableForegroundDispatch(activity, pi, filters, null);
        }
    }

    public static byte[] getData(Intent intent) {
        Parcelable raw[] = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (raw == null)
            return null;
        NdefMessage msg = (NdefMessage) raw[0];
        NdefRecord pvk = msg.getRecords()[0];
        return pvk.getPayload();
    }

    public static int write(Intent i, byte[] data) {
        Tag tag = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null)
            return -1;
        NdefRecord appRecord = NdefRecord
                .createApplicationRecord(Visual.strings.SPEC_PACK);
        byte[] mimeBytes = ("application/" + Visual.strings.SPEC_PACK)
                .getBytes(Charset.forName("US-ASCII"));
        NdefRecord cardRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                mimeBytes, new byte[0], data);
        NdefMessage message = new NdefMessage(new NdefRecord[]{cardRecord,
                appRecord});
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            int a = format(tag);
            if (a != R.string.tag_formatted)
                return a;
        }
        ndef = Ndef.get(tag);
        if (ndef == null)
            return R.string.cant_connect_nfc_adapter;
        try {
            ndef.connect();
            if (!ndef.isWritable()) {
                ndef.close();
                return R.string.failed_read_only;
            }
            int size = message.toByteArray().length;
            if (ndef.getMaxSize() < size) {
                ndef.close();
                return R.string.too_small_tag;
            }
            try {
                ndef.writeNdefMessage(message);
            } catch (FormatException e) {
                e.printStackTrace();
                return R.string.cant_format;
            }
            return R.string.tag_written;
        } catch (IOException e) {
            e.printStackTrace();
            return R.string.io_exception_format;
        }
    }

    private static int format(Tag tag) {
        NdefFormatable format = NdefFormatable.get(tag);
        if (format == null)
            return R.string.tag_not_supported;
        try {
            format.connect();
            format.format(null);
            return R.string.tag_formatted;
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return R.string.cant_format;
    }

}
