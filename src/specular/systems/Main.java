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
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import specular.systems.Dialogs.AddContactDlg;
import specular.systems.Dialogs.DeleteDataDialog;
import specular.systems.Dialogs.DeleteDialog;
import specular.systems.Dialogs.ExitWithoutSave;
import specular.systems.Dialogs.ExplainDialog;
import specular.systems.Dialogs.NotImplemented;
import specular.systems.Dialogs.ProgressDlg;
import specular.systems.Dialogs.Response;
import specular.systems.Dialogs.ShareContactDlg;
import specular.systems.Dialogs.ShareDialog;
import specular.systems.Dialogs.TurnNFCOn;


public class Main extends Activity {
    public final static int MSG_LIMIT_FOR_QR = 141;
    private final static int FAILED = 0, REPLACE_PHOTO = 1, CANT_DECRYPT = 2, DECRYPT_SCREEN = 3;
    public static ContactsDataSource contactsDataSource;
    public static String currentText = "";
    public static int currentLayout;
    public static boolean changed;
    public static boolean comingFromSettings = false;
    //the list that the user see
    public static List<Contact> currentList;
    //the complete list
    public static List<Contact> fullList;
    private final Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FAILED:
                    Toast.makeText(getBaseContext(), R.string.failed,
                            Toast.LENGTH_LONG).show();
                    break;
                case REPLACE_PHOTO:
                    ((TextView) findViewById(R.id.file_content_length)).setText(fileContent.length + "");
                    ((ImageButton) findViewById(R.id.add_file)).setImageResource(R.drawable.after_attach);
                    break;
                case CANT_DECRYPT:
                    String s = msg.obj != null ? (String) msg.obj : getString(R.string.cant_decrypt);
                    ((TextView) findViewById(R.id.decrypted_msg)).setText(s);
                    break;
                case DECRYPT_SCREEN:
                    if (CryptMethods.decryptedMsg != null && CryptMethods.decryptedMsg.getFileContent() != null)
                        if (!FilesManagement.createFileToOpen(Main.this))
                            Toast.makeText(Main.this, R.string.failed_to_create_file_to_open, Toast.LENGTH_SHORT).show();
                    selectItem(1, R.layout.decrypted_msg);
                    break;
            }
        }
    };
    private final int ATTACH_FILE = 0, SCAN_QR = 1;
    public MySimpleArrayAdapter adapter;
    public Handler handler;
    boolean exit = false;
    private boolean handleByOnActivityResult = false;
    private int layouts[];
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] menuTitles;
    private int[] menuDrawables;
    private CharSequence mTitle;
    private String userInput;
    private byte[] fileContent;
    private String fileName = "";
    private Contact contact;
    private boolean handleByOnNewIntent = false;

    public void createKeysManager() {
        createKeys.start();
        if (NfcAdapter.getDefaultAdapter(this) != null)
            if (!NfcAdapter.getDefaultAdapter(this).isEnabled()) {
                TurnNFCOn tno = new TurnNFCOn();
                tno.show(getFragmentManager(), "nfc");
            } else {
                handleByOnNewIntent = true;
                selectItem(-1, R.layout.wait_nfc_to_write);
                PendingIntent pi = PendingIntent.getActivity(Main.this, 0,
                        new Intent(Main.this, getClass())
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                IntentFilter tagDetected = new IntentFilter(
                        NfcAdapter.ACTION_TAG_DISCOVERED);
                IntentFilter[] filters = new IntentFilter[]{tagDetected};
                NfcAdapter
                        .getDefaultAdapter(Main.this)
                        .enableForegroundDispatch(Main.this, pi, filters, null);
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
        FragmentManagement.luc.change(contact);
        final MessageFormat msg = new MessageFormat(fileContent, fileName, userInput,
                contact.getSession());
        final ProgressDlg prgd = new ProgressDlg(this);
        prgd.setCancelable(false);
        prgd.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                CryptMethods.encrypt(msg.getFormatedMsg().getBytes(),
                        contact.getPublicKey());
                sendMessage();
                prgd.cancel();
                currentText = "";
            }
        }).start();
    }

    public void notImp(View v) {
        NotImplemented ni3 = new NotImplemented();
        ni3.show(getFragmentManager(), "aaaa");
    }

    public void decryptedMsgClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                EditText et = (EditText) findViewById(R.id.message);
                userInput = et.getText().toString();
                // hides the keyboard when the user starts the encryption process
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                contact = contactsDataSource.findContact(CryptMethods.decryptedMsg.getPublicKey());
                encryptManager();
                break;
            case R.id.open_file:
                //String name = CryptMethods.decryptedMsg.getFileName();
                //Log.d("name",name);
                //String tmp[] = name.split(".");
                //String extension = tmp[tmp.length - 1];
                notImp(null);

                //Intent intent = new Intent(Intent.ACTION_VIEW);
                //intent.setDataAndType(Uri.parse("file://" + new File(getFilesDir(), "File")), "*/*");
        /*try {
            startActivityForResult(intent, 23);
        } catch (Exception e) {
            Toast.makeText(this, R.string.cand_find_an_app_to_open_file, Toast.LENGTH_SHORT).show();
        }*/
                break;
            case R.id.answer:
                if (findViewById(R.id.add_contact_decrypt).getVisibility() == View.VISIBLE)
                    Toast.makeText(getBaseContext(), R.string.add_contact_first, Toast.LENGTH_LONG).show();
                else {
                    Response r = new Response();
                    r.show(getFragmentManager(), "r");
                }
                break;
            case R.id.hash:
                ExplainDialog edlg = new ExplainDialog(R.string.what_is_hash, R.string.hash_explain);
                edlg.show(getFragmentManager(), "hash");
                break;
            case R.id.session:
                ExplainDialog edl = new ExplainDialog(R.string.what_is_session, R.string.session_explain);
                edl.show(getFragmentManager(), "session");
                break;
            case R.id.replay:
                ExplainDialog ed = new ExplainDialog(R.string.what_is_replay, R.string.replay_explain);
                ed.show(getFragmentManager(), "replay");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        FilesManagement.getKeysFromSDCard(this);
        handleByOnActivityResult = true;
        if (resultCode == RESULT_OK) {
            if (requestCode == ATTACH_FILE) {
                final Uri uri = intent.getData();
                if (uri != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] data = FilesManagement.addFile(Main.this, uri);
                            if (data != null) {
                                if (data.length > 0) {
                                    String w[] = uri.getEncodedPath().split("/");
                                    fileName = w[w.length - 1];
                                    fileContent = data;
                                    Message msg = hndl.obtainMessage(REPLACE_PHOTO);
                                    hndl.sendMessage(msg);
                                }
                                //TODO handle empty file
                            } else {
                                Message msg = hndl.obtainMessage(FAILED);
                                hndl.sendMessage(msg);
                            }
                        }
                    }).start();
                }
            } else if (requestCode == 71) {
                //coming back from turn nfc on
                createKeysManager();
            } else if (requestCode == 23) {
                //nothing to do, it just helping that the call set up views will not called
            } else {
                String result = intent.getStringExtra("barcode");
                if (result != null) {
                    switch (currentLayout) {
                        case R.layout.decrypt:
                            getIntent().putExtra("message", result);
                            setUpViews();
                            break;
                        case R.layout.encrypt:
                            PublicContactCard qrpbk = new PublicContactCard(this, result);
                            if (qrpbk.getPublicKey() != null) {
                                //TODO
                               /* Contact c = Contact.giveMeContact(this, qrpbk);
                                findViewById(R.id.en_list_contact).setVisibility(View.GONE);
                                ((TextView) findViewById(R.id.contact_id_to_send)).setText(c.getId() + "");
                                ((TextView) findViewById(R.id.chosen_name)).setText(c.getContactName());
                                ((TextView) findViewById(R.id.chosen_email)).setText(c.getEmail());
                                ((ImageView) findViewById(R.id.chosen_icon)).setImageBitmap(c.getPhoto());
                                findViewById(R.id.en_contact).setVisibility(View.VISIBLE);*/
                            } else
                                Toast.makeText(getBaseContext(), R.string.bad_data,
                                        Toast.LENGTH_LONG).show();
                            break;
                        case R.layout.contacts:
                            Splash.fileContactCard = new PublicContactCard(this, result);
                            if (Splash.fileContactCard.getPublicKey() != null) {
                                AddContactDlg acd = new AddContactDlg();
                                acd.show(getFragmentManager(), "acd2");
                            } else {
                                Toast.makeText(getBaseContext(), R.string.bad_data,
                                        Toast.LENGTH_LONG).show();
                            }
                            break;
                    }
                }
            }
        }
    }

    public void onClickSkipNFC(View v) {
        NfcAdapter.getDefaultAdapter(getApplicationContext())
                .disableForegroundDispatch(Main.this);
        synchronized (this) {
            while (createKeys.isAlive()) {
                try {
                    wait(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        handleByOnNewIntent = false;
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
    }

    public void onClickEncrypt(View v) {
        switch (v.getId()) {
            case R.id.add_file:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                Intent i = Intent.createChooser(intent, getString(R.string.choose_file_to_attach));
                startActivityForResult(i, ATTACH_FILE);
                break;
            case R.id.send:
                EditText et = (EditText) findViewById(R.id.message);
                userInput = et.getText().toString();
                // hides the keyboard when the user starts the encryption process
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                long id = Long.parseLong(((TextView) findViewById(R.id.contact_id_to_send)).getText().toString());
                contact = contactsDataSource.findContact(id);
                encryptManager();
                break;
            case R.id.add_contact:
                Intent intt = new Intent(this, StartScan.class);
                startActivityForResult(intt, SCAN_QR);
                break;
        }
    }

    public void onClick(final View v) {
        switch (currentLayout) {
            case R.layout.wait_nfc_decrypt:
                Intent i = new Intent(Settings.ACTION_NFC_SETTINGS);
                startActivity(i);
                break;
            case R.layout.decrypt:
                Intent intent = new Intent(Main.this, StartScan.class);
                intent.putExtra("decrypt", true);
                startActivityForResult(intent, SCAN_QR);
                break;
            case R.layout.contacts:
                Intent intt = new Intent(this, StartScan.class);
                startActivityForResult(intt, SCAN_QR);
                break;
            case R.layout.edit_contact:
                Contact contact = contactsDataSource.findContact(Long
                        .valueOf(((TextView) findViewById(R.id.contact_id))
                                .getText().toString()));
                switch (v.getId()) {
                    case R.id.save:
                        String name = ((EditText) findViewById(R.id.contact_name)).getText()
                                .toString();
                        String email = ((EditText) findViewById(R.id.contact_email)).getText()
                                .toString();
                        if (name.length() > 0 && email.length() > 0)
                            contact.update(name, email, null, null, -1);
                        else
                            Toast.makeText(getBaseContext(), R.string.fill_all,
                                    Toast.LENGTH_LONG).show();
                        selectItem(-1, R.layout.contacts);
                        break;
                    case R.id.delete:
                        DeleteDialog dlg = new DeleteDialog();
                        dlg.show(getFragmentManager(), "delete");
                        break;
                    case R.id.answer:
                        Response r = new Response();
                        r.show(getFragmentManager(), "n");
                        break;
                }
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
        handler = new Handler(Looper.getMainLooper());
        contactsDataSource = new ContactsDataSource(this);
        currentList = contactsDataSource.getAllContacts();
        Collections.sort(currentList,new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact contact2) {
                return contact.getEmail().compareTo(contact2.getEmail());
            }
        });
        fullList = new ArrayList<Contact>();
        fullList.addAll(currentList);
        adapter = new MySimpleArrayAdapter(this, currentList);
        setContentView(R.layout.main);
        findViewById(R.id.drawer_layout).animate().setDuration(1000).alpha(1).start();
        setUpViews();
    }

    @Override
    public void onNewIntent(Intent i) {
        //TODO find a better solution to deleting keys while on new intent
        handleByOnNewIntent = false;
        if (currentLayout == R.layout.wait_nfc_to_write) {
            Tag tag = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                synchronized (this) {
                    while (createKeys.isAlive()) {
                        try {
                            wait(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                String rslt = getString(writeTag(tag, Visual.hex2bin(CryptMethods.getPrivateToSave())));
                Toast.makeText(getBaseContext(), rslt, Toast.LENGTH_LONG).show();
                if (rslt.equals(getString(R.string.tag_written))) {
                    CryptMethods.NFCMode = true;
                    saveKeys.start(this);
                    setUpViews();
                } else
                    handleByOnNewIntent = true;
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
        if (currentLayout == R.layout.encrypt || currentLayout == R.layout.contacts) {
            View b = findViewById(R.id.filter_ll);
            View lst = currentLayout == R.layout.encrypt ? findViewById(R.id.en_list_contact) : findViewById(R.id.list);
            if (lst.getVisibility() == View.VISIBLE) {
                if (b.getVisibility() == View.GONE) {
                    b.setVisibility(View.VISIBLE);
                    FragmentManagement.luc.hide(this);
                } else {
                    refreshList();
                    ((EditText) findViewById(R.id.filter)).setText("");
                    b.setVisibility(View.GONE);
                }
            }
        } else if (currentLayout == R.layout.edit_contact) {
            ShareContactDlg sd = new ShareContactDlg();
            sd.show(getFragmentManager(), ((EditText) findViewById(R.id.contact_name)).getText()
                    + ": " + ((EditText) findViewById(R.id.contact_email)).getText());
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentLayout == R.layout.contacts || currentLayout == R.layout.encrypt) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main, menu);
            return super.onCreateOptionsMenu(menu);
        } else if (currentLayout == R.layout.edit_contact) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main, menu);
            return super.onCreateOptionsMenu(menu);
        }
        return false;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav main is open, hide action items related to the content
        // view
        MenuItem mi = menu.findItem(R.id.action_search);
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        if (currentLayout == R.layout.contacts || currentLayout == R.layout.encrypt) {
            mi.setIcon(R.drawable.search);
            mi.setVisible(!drawerOpen);
            return super.onPrepareOptionsMenu(menu);
        } else if (currentLayout == R.layout.edit_contact) {
            mi.setVisible(!drawerOpen);
            mi.setIcon(android.R.drawable.ic_menu_share);
            return super.onPrepareOptionsMenu(menu);
        }
        return false;
    }

    @Override
    public void onPause() {
        if (!handleByOnNewIntent)
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
        currentLayout = layout;
        mDrawerList.setItemChecked(menu, true);
        setTitle(menuTitles[menu]);
        mDrawerLayout.closeDrawer(mDrawerList);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = new FragmentManagement(Main.this);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment).commit();
            }
        }, 100);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (getActionBar() != null)
            getActionBar().setTitle(mTitle);
    }

    private void setUpViews() {
        final int ENCRYPT = 0, DECRYPT = 1, SHARE = 2, CONTACTS = 3, LEARN = 4, SETUP = 5;
        final String[] allMenus = getResources().getStringArray(R.array.menus);
        final int[] allDrb = {R.drawable.encrypt, R.drawable.decrypt, R.drawable.share, R.drawable.contacts, R.drawable.learn, R.drawable.manage};
        final int BOTH = 0, PV = 1, PB = 2, NONE = 3;
        int status = CryptMethods.privateExist() && CryptMethods.publicExist() ? 0 : CryptMethods.privateExist() ? 1 : CryptMethods.publicExist() ? 2 : 3;
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the main
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        //define which menu's will be available
        switch (status) {
            case BOTH:
                menuTitles = allMenus;
                menuDrawables = allDrb;
                break;
            case PB:
                menuTitles = new String[]{allMenus[ENCRYPT], allMenus[SHARE],
                        allMenus[CONTACTS], allMenus[LEARN], allMenus[SETUP]};
                menuDrawables = new int[]{allDrb[ENCRYPT], allDrb[SHARE],
                        allDrb[CONTACTS], allDrb[LEARN], allDrb[SETUP]};
                break;
            case PV:
                menuTitles = new String[]{allMenus[DECRYPT], allMenus[CONTACTS],
                        allMenus[LEARN], allMenus[SETUP]};
                menuDrawables = new int[]{allDrb[DECRYPT],
                        allDrb[CONTACTS], allDrb[LEARN], allDrb[SETUP]};
                break;
            case NONE:
                menuTitles = new String[]{allMenus[LEARN], allMenus[SETUP]};
                menuDrawables = new int[]{allDrb[LEARN], allDrb[SETUP]};
                break;
        }
        // set up the main's list view with items and click listener
        mDrawerList.setAdapter(new LeftMenu(this,
                menuTitles, menuDrawables));
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
        final int allLayouts[] = {R.layout.encrypt, R.layout.decrypt,
                R.layout.share, R.layout.contacts, R.layout.help,
                R.layout.setup};
        switch (status) {
            case BOTH:
                layouts = allLayouts;
                if (!openByFile())
                    selectItem(0, R.layout.encrypt);
                break;
            case PB:
                layouts = new int[]{allLayouts[ENCRYPT], allLayouts[SHARE],
                        allLayouts[CONTACTS], allLayouts[LEARN], allLayouts[SETUP]};
                selectItem(1, R.layout.wait_nfc_decrypt);
                break;
            case PV:
                layouts = new int[]{allLayouts[DECRYPT], allLayouts[CONTACTS],
                        allLayouts[LEARN], allLayouts[SETUP]};
                if (!openByFile())
                    selectItem(0, R.layout.decrypt);
                break;
            case NONE:
                layouts = new int[]{allLayouts[LEARN], allLayouts[SETUP]};
                selectItem(1, R.layout.create_new_keys);
                break;
        }
    }

    private boolean openByFile() {
        final String msg = getIntent().getStringExtra("message");
        if (Splash.message != null || msg != null) {
            final ProgressDlg prgd = new ProgressDlg(this);
            prgd.setCancelable(false);
            prgd.setMessage(getString(R.string.decrypting));
            prgd.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CryptMethods.decrypt(msg != null ? msg : Splash.message);
                    getIntent().removeExtra("message");
                    Splash.message = null;
                    Message msg = hndl.obtainMessage(DECRYPT_SCREEN);
                    hndl.sendMessage(msg);
                    prgd.cancel();
                }
            }).start();
            getIntent().setData(null);
            return true;
        } else if (Splash.fileContactCard != null) {
            selectItem(-1, R.layout.contacts);
            //TODO search also in names and emails
            Contact c = contactsDataSource.findContact(Splash.fileContactCard.getPublicKey());
            if (c == null) {
                AddContactDlg acd = new AddContactDlg();
                acd.show(getFragmentManager(), "acd");
            } else {
                //TODO what if some of the details are not exist
                Splash.fileContactCard = null;
                Toast.makeText(getBaseContext(),
                        R.string.contact_exist, Toast.LENGTH_LONG)
                        .show();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        class prepareToExit {
            Thread prepareExit = new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        try {
                            wait(3000);
                            exit = false;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            public prepareToExit() {
                exit = true;
                Toast.makeText(getBaseContext(), R.string.exit_by_back_notify, Toast.LENGTH_SHORT).show();
                prepareExit.start();
            }
        }
        if (exit) {
            CryptMethods.deleteKeys();
            super.onBackPressed();
        } else {
            switch (currentLayout) {
                case R.layout.encrypt:
                    if (changed) {
                        currentText = "";
                        selectItem(-1, currentLayout);
                    } else
                        new prepareToExit();
                    break;
                case R.layout.decrypt:
                    setUpViews();
                    break;
                case R.layout.share:
                    setUpViews();
                    break;
                case R.layout.contacts:
                    setUpViews();
                    break;
                case R.layout.help:
                    setUpViews();
                    break;
                case R.layout.setup:
                    setUpViews();
                    break;
                case R.layout.edit_contact:
                    String name = ((TextView) findViewById(R.id.orig_name)).getText().toString();
                    String email = ((TextView) findViewById(R.id.orig_eamil)).getText().toString();
                    String newName = ((EditText) findViewById(R.id.contact_name)).getText().toString();
                    String newEmail = ((EditText) findViewById(R.id.contact_email)).getText().toString();
                    if (name.equals(newName) && email.equals(newEmail))
                        selectItem(-1, R.layout.contacts);
                    else {
                        ExitWithoutSave dlg = new ExitWithoutSave();
                        dlg.show(getFragmentManager(), "exit");
                    }
                    break;
                case R.layout.create_new_keys:
                    if (CryptMethods.publicExist())
                        setUpViews();
                    else
                        new prepareToExit();
                    break;
                case R.layout.decrypted_msg:
                    CryptMethods.decryptedMsg = null;
                    setUpViews();
                    break;
                case R.layout.wait_nfc_decrypt:
                    new prepareToExit();
                    break;
                case R.layout.wait_nfc_to_write:
                    new prepareToExit();
                    break;
            }
        }
    }

    public void share(View v) {
        ShareDialog dlg = new ShareDialog();
        dlg.show(getFragmentManager(), "share");
    }

    //TODO
    public void shareWeb(View v) {

    }

    void sendMessage() {
        boolean success = FilesManagement.createFilesToSend(this, (userInput.length() + (fileContent != null ? fileContent.length : 0)) < MSG_LIMIT_FOR_QR);
        if (success) {
            Intent intentShare = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intentShare.putExtra(Intent.EXTRA_EMAIL, new String[]{((TextView) findViewById(R.id.chosen_email)).getText().toString()});
            intentShare.setType("*/*");
            intentShare.putExtra(Intent.EXTRA_SUBJECT,
                    getResources().getString(R.string.subject_encrypt));
            InputStream is;
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
            ArrayList<Uri> files = FilesManagement.getFilesToSend(this);
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

    int writeTag(Tag tag, byte[] binText) {
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
            ndef.connect();
            //todo move this check to the right place
            if (!ndef.isWritable()) {
                return R.string.failed_read_only;
            }
            // work out how much space we need for the data
            int size = message.toByteArray().length;
            if (ndef == null || ndef.getMaxSize() < size) {
                // attempt to format tag
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                    } catch (IOException e) {
                        return R.string.cant_format;
                    }
                } else {
                    return R.string.tag_not_supported;
                }
            }
            ndef.writeNdefMessage(message);
            return R.string.tag_written;
        } catch (Exception e) {
            return R.string.failed_to_write;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!comingFromSettings) {
            comingFromSettings = false;
            FilesManagement.getKeysFromSDCard(this);
            if (handleByOnActivityResult)
                handleByOnActivityResult = false;
            else {
                setUpViews();
            }
        }
    }

    public void addToContacts(View v) {

        Splash.fileContactCard = new PublicContactCard(this
                , CryptMethods.decryptedMsg.getPublicKey()
                , CryptMethods.decryptedMsg.getEmail(), CryptMethods.decryptedMsg.getName());
        AddContactDlg acd = new AddContactDlg();
        acd.show(getFragmentManager(), "acd3");
    }

    public void onClickManage(View v) {
        switch (v.getId()) {
            case R.id.button1:
                selectItem(-1, R.layout.create_new_keys);
                break;
            case R.id.button2:
                notImp(null);
                break;
            case R.id.button3:
                notImp(null);
                break;
            case R.id.button4:
                DeleteDataDialog ddd = new DeleteDataDialog();
                ddd.show(getFragmentManager(), "ddd");
                break;
        }
    }

    public void refreshList() {
        for (int a = 0; a < fullList.size(); a++)
            if (!currentList.contains(fullList.get(a)))
                currentList.add(fullList.get(a));
        if (currentLayout == R.layout.encrypt)
            FragmentManagement.luc.show();
        adapter.notifyDataSetChanged();
    }

    public static class createKeys {
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

    public static class saveKeys {
        static Thread t;

        public static void start(final Activity a) {
            if (t == null || !t.isAlive()) {
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FilesManagement.save(a);
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
