package specular.systems;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Wmain extends Activity {
    final static Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText((Context) msg.obj, R.string.failed,
                            Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Toast.makeText((Context) msg.obj, R.string.file_added,
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    public static int currentLayout;
    static Activity mainActivity = null;
    private int layouts[];
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] menuTitles;
    private CharSequence mTitle;
    private String userInput;
    private String fileContent = "";

    void createKeysManager() {
        createKeys.start();
        if (NfcAdapter.getDefaultAdapter(this) != null)
            if (!NfcAdapter.getDefaultAdapter(this).isEnabled())
                Toast.makeText(getBaseContext(), R.string.nfc_off,
                        Toast.LENGTH_LONG).show();
            else {
                selectItem(-1, R.layout.wait_nfc_to_write);
                PendingIntent pi = PendingIntent.getActivity(Wmain.this, 0,
                        new Intent(Wmain.this, getClass())
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                IntentFilter tagDetected = new IntentFilter(
                        NfcAdapter.ACTION_TAG_DISCOVERED);
                IntentFilter[] filters = new IntentFilter[]{tagDetected};
                NfcAdapter
                        .getDefaultAdapter(Wmain.this)
                        .enableForegroundDispatch(Wmain.this, pi, filters, null);
            }
        else {
            findViewById(R.id.drawer_layout).animate().setDuration(1000)
                    .alpha(0);
            saveKeys.start(this);
            while (saveKeys.isAlive())
                synchronized (this) {
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            Intent intent = new Intent(Wmain.this, Splash.class);
            startActivity(intent);
            onPause();
            finish();
        }
    }

    void decryptManager(String contents) {
        if (contents != null && contents.length() > 5) {
            String data = CryptMethods.decrypt(contents);
            Fragment fragment = new FragmentManagment();
            Bundle args = new Bundle();
            args.putString("data", data);
            args.putInt("layout", R.layout.decrypt_show);
            fragment.setArguments(args);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
        } else
            Toast.makeText(getBaseContext(), R.string.failed, Toast.LENGTH_LONG)
                    .show();
    }

    void encryptManager(final QRPublicKey qrp) {
        Contact contact = Contact.giveMeContact(this, qrp);
        final QRMessage msg = new QRMessage(fileContent, userInput,
                contact.getSession());

        // TODO debug note
        Log.e("jj", "" + System.currentTimeMillis());
        Fragment fragment = new FragmentManagment();
        Bundle args = new Bundle();
        args.putInt("layout", R.layout.encrypt_show);
        args.putString("email", qrp.getEmail());
        args.putString("session", msg.getSession());
        args.putString("hash", msg.getHash());
        args.putString("userInput", userInput);
        args.putString("sentTime", msg.getSentTime());
        args.putBoolean("qr", (userInput.length() + fileContent.length()) < 141);
        fragment.setArguments(args);
        android.app.FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment).commit();
        CryptMethods.encrypt(msg.getFormatedMsg().getBytes(),
                qrp.getPublicKey());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 5) {
                final Uri uri = intent.getData();
                if (uri != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String data;
                            ContentResolver cr = getBaseContext()
                                    .getContentResolver();
                            InputStream is = null;
                            try {
                                is = cr.openInputStream(uri);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            StringBuilder buf = new StringBuilder();
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(is));
                            String str;
                            try {
                                while ((str = reader.readLine()) != null) {
                                    buf.append(str).append("\n");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            buf.deleteCharAt(buf.length() - 1);
                            data = buf.toString();
                            if (data != null) {
                                String w[] = uri.getEncodedPath().split("/");
                                fileContent = w[w.length - 1] + "\n" + data;
                                Message msg = hndl.obtainMessage(1,
                                        getBaseContext());
                                hndl.sendMessage(msg);
                            } else {
                                Message msg = hndl.obtainMessage(0,
                                        getBaseContext());
                                hndl.sendMessage(msg);
                            }
                        }
                    }).start();
                }
            } else {
                String result = intent.getStringExtra("barcode");
                if (result != null) {
                    // String contents = result;
                    switch (currentLayout) {
                        case R.layout.decrypt:
                            decryptManager(result);
                            break;
                        case R.layout.encrypt:
                            encryptManager(new QRPublicKey(this, result));
                            break;
                        case R.layout.contacts:
                            QRPublicKey qrpbk = new QRPublicKey(this, result);
                            if (qrpbk.getPublicKey() != null) {
                                new Contact(this, qrpbk.getName(),
                                        qrpbk.getEmail(), qrpbk.getPublicKey());
                            } else
                                Toast.makeText(getBaseContext(), R.string.bad_data,
                                        Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        }
    }

    public void onClick(View v) {
        switch (currentLayout) {
            case R.layout.setup:
                selectItem(-1, R.layout.create_new_keys);
                break;
            case R.layout.create_new_keys:
                CryptMethods.myEmail = ((EditText) findViewById(R.id.email))
                        .getText().toString();
                CryptMethods.myName = ((EditText) findViewById(R.id.name))
                        .getText().toString();
                if (CryptMethods.myEmail == null
                        || !CryptMethods.myEmail.contains("@")
                        || CryptMethods.myName == null)
                    Toast.makeText(getBaseContext(), R.string.fill_all,
                            Toast.LENGTH_LONG).show();
                else
                    createKeysManager();
                break;
            case R.layout.wait_nfc_to_write:
                NfcAdapter.getDefaultAdapter(getApplicationContext())
                        .disableForegroundDispatch(Wmain.this);
                saveKeys.start(this);
                synchronized (this) {
                    while (createKeys.isAlive()) {
                        try {
                            wait(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                synchronized (this) {
                    while (createKeys.isAlive() || saveKeys.isAlive()) {
                        try {
                            wait(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setUpViews();
                break;
            case R.layout.encrypt:
                if (v.getId() == R.id.add_file) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    Intent i = Intent.createChooser(intent, "file");
                    startActivityForResult(i, 5);
                } else {
                    startEncrypt();
                }
                break;
            case R.layout.decrypt:
                Intent intent = new Intent(Wmain.this, StartScan.class);
                startActivityForResult(intent, 18);
                break;
            case R.layout.contacts:
                break;
            case R.layout.edit_contact:
                ContactsDataSource cds = new ContactsDataSource(this);
                cds.open();
                Contact contact = cds.findContact(Long
                        .valueOf(((TextView) findViewById(R.id.contact_id))
                                .getText().toString()));
                cds.close();
                switch (v.getId()) {
                    case R.id.save:
                        if (((EditText) findViewById(R.id.contact_name)).getText()
                                .toString().length() != 0)
                            contact.update(this,
                                    ((EditText) findViewById(R.id.contact_name))
                                            .getText().toString(), null, null, null, -1);
                        else
                            Toast.makeText(getBaseContext(), R.string.fill_all,
                                    Toast.LENGTH_LONG).show();
                        break;
                    case R.id.delete:
                        cds.open();
                        cds.deleteContact(contact);
                        cds.close();
                        break;
                }
                selectItem(-1, R.layout.contacts);
                break;
            default:
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the main toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.main);
        findViewById(R.id.drawer_layout).animate().setDuration(1000).alpha(1);
        setUpViews();
    }

    @Override
    public void onNewIntent(Intent i) {
        if (currentLayout == R.layout.wait_nfc_to_write) {
            Tag tag = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                while (createKeys.isAlive()) {
                    try {
                        wait(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String tmp = CryptMethods.myPrivateKey;
                CryptMethods.myPrivateKey = null;
                saveKeys.start(this);
                String rslt = writeTag(tag, Visual.hex2bin(tmp));
                Toast.makeText(getBaseContext(), rslt, Toast.LENGTH_LONG).show();
                findViewById(R.id.drawer_layout).animate().setDuration(1000)
                        .alpha(0);
                while (saveKeys.isAlive()) {
                    try {
                        wait(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                onPause();
                finish();
                Intent intent = new Intent(Wmain.this, Splash.class);
                startActivity(intent);
            }
        } else if (currentLayout == R.layout.wait_nfc_decrypt) {
            Parcelable raw[] = getIntent().getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (raw != null) {
                NdefMessage msg = (NdefMessage) raw[0];
                NdefRecord pvk = msg.getRecords()[0];
                CryptMethods.myPrivateKey = Visual.bin2hex(pvk
                        .getPayload());
                setUpViews();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the main.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        /*
         * switch(item.getItemId()) { case R.id.action_websearch: // create
		 * intent to perform web search for this planet Intent intent = new
		 * Intent(Intent.ACTION_WEB_SEARCH);
		 * intent.putExtra(SearchManager.QUERY, getActionBar().getTitle()); //
		 * catch event that there's no activity to handle intent if
		 * (intent.resolveActivity(getPackageManager()) != null) {
		 * startActivity(intent); } else { Toast.makeText(this,
		 * R.string.app_not_available, Toast.LENGTH_LONG).show(); } return true;
		 * default:}
		 */
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onPause() {
        super.onPause();
        CryptMethods.mPtK = null;
        CryptMethods.myPrivateKey = "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj";
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav main is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private void selectItem(int position, int layout_screen) {
        // update the main content by replacing fragments
        int layout = 0;
        int menu = -1;
        if (layout_screen == 0 && position != -1) {
            layout = layouts[position];
            menu = position;
        } else if (layout_screen != 0 && position == -1) {
            layout = layout_screen;
            for (int a = 0; a < layouts.length; a++) {
                if (layout == layouts[a]) {
                    menu = a;
                    break;
                }
            }
            if (menu < 0) {
                switch (layout) {
                    case R.layout.wait_nfc_to_write:
                        menu = menuTitles.length - 1;
                        break;
                    case R.layout.create_new_keys:
                        menu = menuTitles.length - 1;
                        break;
                    case R.layout.wait_nfc_decrypt:
                        menu = menuTitles.length - 1;
                        break;
                }
            }
        } else {
            Toast.makeText(getBaseContext(), "shouldnt be here",
                    Toast.LENGTH_LONG).show();
        }
        // update selected item and title, then close the main
        mDrawerList.setItemChecked(menu, true);
        setTitle(menuTitles[menu]);
        mDrawerLayout.closeDrawer(mDrawerList);
        Fragment fragment = new FragmentManagment();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        fragment.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (getActionBar() != null)
            getActionBar().setTitle(mTitle);
    }

    private void setUpViews() {
        int defaultScreen = 0;
        boolean privateKey = false, publicKey = false;
        if (CryptMethods.myPrivateKey != null)
            privateKey = true;
        if (CryptMethods.myPublicKey != null)
            publicKey = true;
        final int allLayouts[] = {R.layout.encrypt, R.layout.decrypt,
                R.layout.share, R.layout.contacts, R.layout.help,
                R.layout.setup};
        final String[] allMenus = {"Encrypt", "Decrypt", "Share", "Contacts",
                "Learn", "Manage"};
        final int ENCRYPT = 0, DECRYPT = 1, SHARE = 2, CONTACTS = 3, LEARN = 4, SETUP = 5;
        if (privateKey && publicKey) {
            layouts = allLayouts;
            menuTitles = allMenus;
            defaultScreen = ENCRYPT;
        } else if (publicKey) {
            layouts = new int[]{allLayouts[ENCRYPT], allLayouts[SHARE],
                    allLayouts[CONTACTS], allLayouts[LEARN], allLayouts[SETUP]};
            menuTitles = new String[]{allMenus[ENCRYPT], allMenus[SHARE],
                    allMenus[CONTACTS], allMenus[LEARN], allMenus[SETUP]};
            defaultScreen = 1;
        } else if (privateKey) {
            layouts = new int[]{allLayouts[DECRYPT], allLayouts[CONTACTS],
                    allLayouts[LEARN], allLayouts[SETUP]};
            menuTitles = new String[]{allMenus[DECRYPT], allMenus[CONTACTS],
                    allMenus[LEARN], allMenus[SETUP]};
            defaultScreen = 0;
        } else {
            layouts = new int[]{allLayouts[LEARN], allLayouts[SETUP]};
            menuTitles = new String[]{allMenus[LEARN], allMenus[SETUP]};
            defaultScreen = 1;
        }
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the main
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        // set up the main's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, menuTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav main
        ActionBar ab = getActionBar();
        if (ab != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding main and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav main image to replace 'Up' caret */
                R.string.drawer_open, /* "open main" description for accessibility */
                R.string.drawer_close /* "close main" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        String msg = getIntent().getStringExtra("message");
        if (msg != null && publicKey) {
            getIntent().removeExtra("message");
            decryptManager(msg);
        } else {
            if (!privateKey && publicKey)
                selectItem(-1, R.layout.wait_nfc_decrypt);
            else {
                boolean exist = false;
                for (int a = 0; a < layouts.length; a++)
                    if (currentLayout == layouts[a]) {
                        selectItem(-1, currentLayout);
                        exist = true;
                        break;
                    }
                if (!exist)
                    selectItem(defaultScreen, 0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        boolean atHome = false;
        for (int a = 0; a < layouts.length; a++)
            if (currentLayout == layouts[a])
                atHome = true;
        if (atHome)
            super.onBackPressed();
        else{
            setUpViews();
        }
    }

    public void share(View v) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_SUBJECT,
                    getResources().getString(R.string.subject_share));
            intent.putExtra(Intent.EXTRA_TEXT,
                    getResources().getString(R.string.content_share));
            ArrayList<Uri> files = new FilesManegmant(this).getFilesToShare();
            if (files == null)
                Toast.makeText(this, R.string.attachment_error,
                        Toast.LENGTH_SHORT).show();
            else {
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                startActivity(Intent.createChooser(intent, getResources()
                        .getString(R.string.share_dialog)));
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), R.string.failed, Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void shareWeb(View v) {

    }

    // starts the encryption
    public void startEncrypt() {
        EditText et = (EditText) findViewById(R.id.message);
        userInput = et.getText().toString();
        if (userInput.length() == 0)
            Toast.makeText(getBaseContext(), R.string.no_msg,
                    Toast.LENGTH_SHORT).show();
        else {
            ListView lv = (ListView) findViewById(R.id.en_list_contact);
            lv.setVisibility(View.VISIBLE);
            ContactsDataSource cds = new ContactsDataSource(Wmain.this);
            cds.open();
            List<Contact> alc = cds.getAllContacts();
            cds.close();
            final ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(
                    Wmain.this, android.R.layout.simple_list_item_1, alc);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> p1, View p2, int p3,
                                        long p4) {
                    encryptManager(new QRPublicKey(Wmain.this, adapter
                            .getItem(p3)));
                }
            });
            // hides the keyboard when the user starts the encryption process
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

        }
    }

    String writeTag(Tag tag, byte[] binText) {
        // record to launch Play Store if app is not installed
        NdefRecord appRecord = NdefRecord
                .createApplicationRecord("specular.systems");
        byte[] mimeBytes = "application/*"
                .getBytes(Charset.forName("US-ASCII"));
        NdefRecord cardRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                mimeBytes, new byte[0], binText);
        NdefMessage message = new NdefMessage(new NdefRecord[]{cardRecord,
                appRecord});
        try {
            // see if tag is already NDEF formatted
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return "Read-only tag.";
                }
                // work out how much space we need for the data
                int size = message.toByteArray().length;
                if (ndef.getMaxSize() < size) {
                    return "Tag doesn't have enough free space.";
                }
                ndef.writeNdefMessage(message);
                return "Tag written successfully.";
            } else {
                // attempt to format tag
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return "Tag written successfully!\nClose this app and scan tag.";
                    } catch (IOException e) {
                        return "Unable to format tag to NDEF.";
                    }
                } else {
                    return "Tag doesn't appear to support NDEF format.";
                }
            }
        } catch (Exception e) {
            return "Failed to write tag";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CryptMethods.myPrivateKey == null
                || CryptMethods.myPublicKey == null) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    new FilesManegmant(Wmain.this).get();
                }
            });
            t.start();
            synchronized (this) {
                while (t.isAlive())
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                setUpViews();
            }
        }
    }

    static class createKeys {
        static Thread t;

        public static void start() {
            if (t == null || !t.isAlive()) {
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CryptMethods.createKeys();
                    }
                });
                t.start();
            }
        }

        public static boolean isAlive() {
            return t.isAlive();
        }
    }

    static class saveKeys {
        static Thread t;

        public static void start(final Activity a) {
            if (t == null || !t.isAlive()) {
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new FilesManegmant(a).save();
                    }
                });
                t.start();
            }
        }

        public static boolean isAlive() {
            return t.isAlive();
        }
    }

    /* The click listner for ListView in the navigation main */
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position, 0);
        }
    }
}
