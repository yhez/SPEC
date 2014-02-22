package specular.systems.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import specular.systems.Dialogs.NotImplemented;
import specular.systems.R;


public class Demo extends Activity {
    int status;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.demo);
        status = 0;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_to_spec:
                finish();
                break;
            case R.id.demo_bank:
                status = 1;
                setContentView(R.layout.wf_demo);
                setImage();
                findViewById(R.id.sign).animate().alpha(1).setDuration(5000).start();
                break;
            case R.id.safe:
                NotImplemented ni = new NotImplemented();
                ni.show(getFragmentManager(), "hfhf");
                break;
        }
    }

    private void setImage() {
        int choice = (int) System.currentTimeMillis() % 3;
        ((ImageView) findViewById(R.id.girl)).setImageResource(choice == 0 ? R.drawable.signon_background01 : (choice == 1 ? R.drawable.signon_background02 : R.drawable.signon_background03));
    }

    public void signOn(View v) {
        Toast t = Toast.makeText(this, "Wrong Username or password\nTry using your NFC Tag", Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }

    byte[] nfcContent;

    @Override
    public void onNewIntent(Intent i) {
        if (i.getAction() != null && i.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            if (getFragmentManager().findFragmentByTag("gg") == null) {
                new keyDialog().show(getFragmentManager(), "gg");
            }
            Parcelable raw[] = i.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (raw == null)
                return;
            NdefMessage msg = (NdefMessage) raw[0];
            NdefRecord pvk = msg.getRecords()[0];
            if (pvk == null)
                return;
            nfcContent = pvk.getPayload();
        }
    }

    @Override
    public void onBackPressed() {
        if (status == 0)
            finish();
        else if (status == 1) {
            setContentView(R.layout.demo);
            status = 0;
        } else if (status == 2) {
            setContentView(R.layout.wf_demo);
            setImage();
            status = 1;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcAdapter.getDefaultAdapter(this) != null && NfcAdapter.getDefaultAdapter(this).isEnabled()) {
            PendingIntent pi = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass())
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
            IntentFilter tagDetected = new IntentFilter(
                    NfcAdapter.ACTION_TAG_DISCOVERED);
            IntentFilter[] filters = new IntentFilter[]{tagDetected};
            NfcAdapter
                    .getDefaultAdapter(this)
                    .enableForegroundDispatch(this, pi, filters, null);
        }
    }

    public class keyDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LockPatternView lpv = new LockPatternView(getActivity());
            lpv.setOnPatternListener(new LockPatternView.OnPatternListener() {
                @Override
                public void onPatternStart() {

                }

                @Override
                public void onPatternCleared() {

                }

                @Override
                public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

                }

                @Override
                public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                    if (pattern.size() != 10) {
                        notCorrect(lpv);
                        return;
                    }
                    ArrayList<LockPatternView.Cell> password = new ArrayList<LockPatternView.Cell>(10);
                    password.add(LockPatternView.Cell.of(0, 0));
                    password.add(LockPatternView.Cell.of(0, 1));
                    password.add(LockPatternView.Cell.of(0, 2));
                    password.add(LockPatternView.Cell.of(0, 3));
                    password.add(LockPatternView.Cell.of(1, 2));
                    password.add(LockPatternView.Cell.of(2, 1));
                    password.add(LockPatternView.Cell.of(3, 0));
                    password.add(LockPatternView.Cell.of(3, 1));
                    password.add(LockPatternView.Cell.of(3, 2));
                    password.add(LockPatternView.Cell.of(3, 3));
                    for (int a = 0; a < password.size(); a++) {
                        if (!password.get(a).equals(pattern.get(a))) {
                            notCorrect(lpv);
                            return;
                        }
                    }
                    getActivity().setContentView(R.layout.images_demo);
                    status = 2;
                    keyDialog.this.getDialog().cancel();
                }
            });
            builder.setView(lpv);
            return builder.create();
        }

        void notCorrect(LockPatternView lpv) {
            lpv.setDisplayMode(LockPatternView.DisplayMode.Wrong);
            lpv.invalidate();
            Toast t = Toast.makeText(Demo.this, "Password is wrong\nhint [Z]", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
    }
}
