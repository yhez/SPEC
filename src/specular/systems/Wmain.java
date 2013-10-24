package specular.systems;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
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
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class Wmain extends Activity {
    public final static int MSG_LIMIT_FOR_QR = 141;
    public static int currentLayout;
    final Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(getBaseContext(), R.string.failed,
                            Toast.LENGTH_LONG).show();
                    break;
                case 15:
                    ((TextView) findViewById(R.id.file_content_length)).setText(fileContent.length() + "");
                    ((ImageButton) findViewById(R.id.add_file)).setImageResource(R.drawable.after_attach);
                    break;
                case 1:
                    String s = msg.obj != null ? (String) msg.obj : getString(R.string.cant_decrypt);
                    Log.e("msg", s);
                    ((TextView) findViewById(R.id.decrypted_msg)).setText(s);
                    break;
            }
        }
    };
    boolean handleByOnActivityResult = false;
    private int layouts[];
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] menuTitles;
    private CharSequence mTitle;
    private String userInput;
    private String fileContent = "";
    private Contact contact;

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
                    .alpha(0).start();
            while (createKeys.isAlive())
                synchronized (this) {
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            saveKeys.start(this);
            while (saveKeys.isAlive())
                synchronized (this) {
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            setUpViews();
        }
    }

    void encryptManager() {
        final QRMessage msg = new QRMessage(fileContent, userInput,
                contact.getSession());
        long x = System.currentTimeMillis();
        CryptMethods.encrypt(msg.getFormatedMsg().getBytes(),
                contact.getPublicKey());
        Log.d("time to encrypt", "" + (System.currentTimeMillis() - x) / 1000);
        sendMessage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        handleByOnActivityResult = true;
        if (resultCode == RESULT_OK) {
            if (requestCode == 5) {
                final Uri uri = intent.getData();
                if (uri != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String data = FilesManegmant.addFile(Wmain.this, uri);
                            if (data != null) {
                                if (data.length() > 0) {
                                    String w[] = uri.getEncodedPath().split("/");
                                    fileContent = w[w.length - 1] + "\n" + data;
                                    Message msg = hndl.obtainMessage(15);
                                    hndl.sendMessage(msg);
                                }
                                //TODO handle empty file
                            } else {
                                Message msg = hndl.obtainMessage(0);
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
                            getIntent().putExtra("message", result);
                            setUpViews();
                            //decryptManager(result);
                            break;
                        case R.layout.encrypt:
                            Log.e("bad data", result);
                            QRPublicKey qrpbk = new QRPublicKey(this, result);
                            if (qrpbk.getPublicKey() != null) {
                                Contact c = Contact.giveMeContact(this, qrpbk);
                                findViewById(R.id.en_list_contact).setVisibility(View.GONE);
                                ((TextView) findViewById(R.id.en_contact)).setText(c + "");
                            } else
                                Toast.makeText(getBaseContext(), R.string.bad_data,
                                        Toast.LENGTH_LONG).show();
                            break;
                        case R.layout.contacts:
                            qrpbk = new QRPublicKey(this, result);
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

    public void onClick(final View v) {
        switch (currentLayout) {
            case R.layout.setup:
                selectItem(-1, R.layout.create_new_keys);
                break;
            case R.layout.create_new_keys:
                String myEmail = ((EditText) findViewById(R.id.email))
                        .getText().toString();
                String myName = ((EditText) findViewById(R.id.name))
                        .getText().toString();
                if (myEmail == null
                        || !myEmail.contains("@")
                        || myName == null)
                    Toast.makeText(getBaseContext(), R.string.fill_all,
                            Toast.LENGTH_LONG).show();
                else {
                    CryptMethods.setDetails(myName, myEmail);
                    createKeysManager();
                }
                break;
            case R.layout.wait_nfc_to_write:
                NfcAdapter.getDefaultAdapter(getApplicationContext())
                        .disableForegroundDispatch(Wmain.this);
                synchronized (this) {
                    while (createKeys.isAlive()) {
                        try {
                            wait(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                saveKeys.start(this);
                synchronized (this) {
                    while (saveKeys.isAlive()) {
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
                switch (v.getId()) {
                    case R.id.add_file:
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        Intent i = Intent.createChooser(intent, getString(R.string.choose_file_to_attach));
                        startActivityForResult(i, 5);
                        break;
                    case R.id.send:
                        EditText et = (EditText) findViewById(R.id.message);
                        userInput = et.getText().toString();
                        // hides the keyboard when the user starts the encryption process
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                        long id = Long.parseLong(((TextView) findViewById(R.id.contact_id_to_send)).getText().toString());
                        ContactsDataSource cds = new ContactsDataSource(this);
                        cds.open();
                        contact = cds.findContact(id);
                        cds.close();
                        encryptManager();
                        break;
                    case R.id.add_contact:
                        Intent intt = new Intent(this, StartScan.class);
                        startActivityForResult(intt, 43);
                        break;
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
        setContentView(R.layout.main);
        findViewById(R.id.drawer_layout).animate().setDuration(1000).alpha(1).start();
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
                if (!CryptMethods.privateExist()) {
                    Toast.makeText(getBaseContext(), getString(R.string.problem_create_keys), Toast.LENGTH_LONG).show();
                    selectItem(layouts.length - 1, R.layout.create_new_keys);
                } else {
                    saveKeys.start(this);
                    String rslt = writeTag(tag, Visual.hex2bin(CryptMethods.getPrivateToSave()));
                    Toast.makeText(getBaseContext(), rslt, Toast.LENGTH_LONG).show();
                    findViewById(R.id.drawer_layout).animate().setDuration(1000)
                            .alpha(0);
                    while (saveKeys.isAlive())
                        try {
                            wait(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    setUpViews();
                }
            }
        } else if (currentLayout == R.layout.wait_nfc_decrypt) {
            Parcelable raw[] = getIntent().getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (raw != null) {
                NdefMessage msg = (NdefMessage) raw[0];
                NdefRecord pvk = msg.getRecords()[0];
                CryptMethods.setPrivate(Visual.bin2hex(pvk
                        .getPayload()));
                setUpViews();
            }
        } else
            setUpViews();
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
        CryptMethods.deleteKeys();
        super.onPause();
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
        int layout = layout_screen;
        int menu = position;
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
                    case R.layout.decrypted_msg:
                        menu = 1;
                        break;
                    default:
                        menu = 0;
                        break;
                }
            }
        }
        // update selected item and title, then close the main
        mDrawerList.setItemChecked(menu, true);
        setTitle(menuTitles[menu]);
        mDrawerLayout.closeDrawer(mDrawerList);
        Fragment fragment = new FragmentManagement();
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
        boolean privateKey = CryptMethods.privateExist(), publicKey = CryptMethods.publicExist();
        final int allLayouts[] = {R.layout.encrypt, R.layout.decrypt,
                R.layout.share, R.layout.contacts, R.layout.help,
                R.layout.setup};
        final String[] allMenus = getResources().getStringArray(R.array.menus);
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
            defaultScreen = 0;
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
        final String msg = getIntent().getStringExtra("message");
        if (msg != null && privateKey) {
            //getIntent().removeExtra("message");
            if (msg.length() > 5) {
                selectItem(1, R.layout.decrypted_msg);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String data = CryptMethods.decrypt(msg);
                        Message msg = hndl.obtainMessage(1, data);
                        hndl.sendMessage(msg);
                    }
                }).start();
            } else
                Toast.makeText(getBaseContext(), R.string.failed, Toast.LENGTH_LONG)
                        .show();

        } else {
            if (!privateKey && publicKey)
                selectItem(-1, R.layout.wait_nfc_decrypt);
            else if (!privateKey && !publicKey)
                selectItem(-1, R.layout.create_new_keys);
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
        if (atHome) {
            CryptMethods.deleteKeys();
            super.onBackPressed();
        } else {
            setUpViews();
        }
    }

    public void share(View v) {
        ShareDialog dlg = new ShareDialog();
        dlg.show(getFragmentManager(), "share");
    }

    public void shareWeb(View v) {

    }

    void sendMessage() {
        boolean success = FilesManegmant.createFilesToSend(this, (userInput.length() + fileContent.length()) < MSG_LIMIT_FOR_QR);
        if (success) {
            Intent intentShare = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intentShare.setType("*/*");
            intentShare.putExtra(Intent.EXTRA_SUBJECT,
                    getResources().getString(R.string.subject_encrypt));
            InputStream is = null;
            try {
                is = getAssets().open("spec_tmp_msg.html");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                intentShare.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new String(buffer)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ArrayList<Uri> files = FilesManegmant.getFilesToSend(this);
            if (files == null)
                Toast.makeText(this, R.string.failed_attach_files, Toast.LENGTH_LONG).show();
            else {
                //TODO add intentShare.putExtra(Intent.EXTRA_EMAIL,)
                intentShare.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                startActivity(Intent.createChooser(intentShare, getResources()
                        .getString(R.string.send_dialog)));
            }
        } else {
            Toast.makeText(this, R.string.failed_to_create_files_to_send, Toast.LENGTH_LONG).show();
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
                    return getString(R.string.failed_read_only);
                }
                // work out how much space we need for the data
                int size = message.toByteArray().length;
                if (ndef.getMaxSize() < size) {
                    return getString(R.string.tag_needs_format);
                }
                ndef.writeNdefMessage(message);
                return getString(R.string.tag_written);
            } else {
                // attempt to format tag
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return getString(R.string.nfc_written_successfully);
                    } catch (IOException e) {
                        return getString(R.string.cant_format);
                    }
                } else {
                    return getString(R.string.tag_not_supported);
                }
            }
        } catch (Exception e) {
            return getString(R.string.failed_to_write);
        }
    }

    @Override
    protected void onResume() {
        FilesManegmant.getKeysFromSdcard(this);
        if (handleByOnActivityResult)
            handleByOnActivityResult = false;
        else {
            setUpViews();
        }
        super.onResume();
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
            return t != null && t.isAlive();
        }
    }

    static class saveKeys {
        static Thread t;

        public static void start(final Activity a) {
            if (t == null || !t.isAlive()) {
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FilesManegmant.save(a);
                    }
                });
                t.start();
            }
        }

        public static boolean isAlive() {
            return t != null && t.isAlive();
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
