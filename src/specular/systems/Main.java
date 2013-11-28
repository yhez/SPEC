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
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import specular.systems.Dialogs.AddContactDlg;
import specular.systems.Dialogs.ContactQR;
import specular.systems.Dialogs.DeleteContactDialog;
import specular.systems.Dialogs.DeleteDataDialog;
import specular.systems.Dialogs.ExplainDialog;
import specular.systems.Dialogs.NotImplemented;
import specular.systems.Dialogs.ProgressDlg;
import specular.systems.Dialogs.Response;
import specular.systems.Dialogs.SendMsgDialog;
import specular.systems.Dialogs.ShareContactDlg;
import specular.systems.Dialogs.ShareCustomDialog;
import specular.systems.Dialogs.TurnNFCOn;


public class Main extends Activity {
    private final static int FAILED = 0, REPLACE_PHOTO = 1, CANT_DECRYPT = 2, DECRYPT_SCREEN = 3, CHANGE_HINT = 4;
    private static int currentKeys = 0;
    private final Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case FAILED:
                    break;
                case REPLACE_PHOTO:
                    ((TextView) findViewById(R.id.file_content_length)).setText(PublicStaticVariables.fileContent.length + "");
                    ImageButton imageButton = (ImageButton) findViewById(R.id.add_file);
                    imageButton.clearAnimation();
                    imageButton.setClickable(true);
                    imageButton.setImageResource(R.drawable.after_attach);
                    break;
                case CANT_DECRYPT:
                    String s = msg.obj != null ? (String) msg.obj : getString(R.string.cant_decrypt);
                    ((TextView) findViewById(R.id.decrypted_msg)).setText(s);
                    break;
                case DECRYPT_SCREEN:
                    if (PublicStaticVariables.decryptedMsg != null && PublicStaticVariables.decryptedMsg.getFileContent() != null)
                        if (!FilesManagement.createFileToOpen(Main.this))
                            Toast.makeText(Main.this, R.string.failed_to_create_file_to_open, Toast.LENGTH_SHORT).show();
                    selectItem(1, R.layout.decrypted_msg);
                    break;
                case PublicStaticVariables.RESULT_ADD_FILE_TO_BIG:
                    Toast t = Toast.makeText(getBaseContext(), R.string.file_to_big, Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    t.show();
                    break;
                case PublicStaticVariables.RESULT_ADD_FILE_FAILED:
                    Toast.makeText(getBaseContext(), R.string.failed,
                            Toast.LENGTH_LONG).show();
                    break;
                case PublicStaticVariables.RESULT_ADD_FILE_EMPTY:
                    Toast.makeText(getBaseContext(), R.string.file_is_empty, Toast.LENGTH_SHORT).show();
                    break;
                case CHANGE_HINT:
                    ((TextView) findViewById(R.id.message)).setHint(R.string.send_another_msg);
                    break;
            }
        }
    };
    private final int ATTACH_FILE = 0, SCAN_QR = 1;
    public Handler handler;
    boolean exit = false;
    Toast t;
    boolean msgSended = false;
    private int layouts[];
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] menuTitles;
    private int[] menuDrawables;
    private CharSequence mTitle;
    private String userInput;
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
        }
    }

    void encryptManager() {

        PublicStaticVariables.luc.change(contact);
        final ProgressDlg prgd = new ProgressDlg(this, "Encrypting...");
        prgd.setCancelable(false);
        prgd.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MessageFormat msg = new MessageFormat(PublicStaticVariables.fileContent, fileName, userInput,
                        contact.getSession());
                CryptMethods.encrypt(msg.getFormatedMsg(),
                        contact.getPublicKey());
                sendMessage();
                prgd.cancel();
                msgSended = true;
            }
        }).start();
    }

    public void notImp(View v) {
        NotImplemented ni3 = new NotImplemented();
        ni3.show(getFragmentManager(), "aaaa");
    }

    private String getFileName(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null)
            return contentURI.getLastPathSegment();
        cursor.moveToFirst();
        String rs = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
        cursor.close();
        return rs;
    }

    public void decryptedMsgClick(View v) {

        switch (v.getId()) {
            case R.id.send:
                findViewById(R.id.answer).setVisibility(View.GONE);
                EditText et = (EditText) findViewById(R.id.message);
                userInput = et.getText().toString();
                // hides the keyboard when the user starts the encryption process
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                contact = PublicStaticVariables.contactsDataSource.findContact(PublicStaticVariables.decryptedMsg.getPublicKey());
                encryptManager();
                break;
            case R.id.open_file:
                String name = PublicStaticVariables.file_name;
                File f = new File(Environment.getExternalStorageDirectory(), name);
                String ext = f.getName().substring(f.getName().indexOf(".") + 1);
                MimeTypeMap mtm = MimeTypeMap.getSingleton();
                String type = mtm.getMimeTypeFromExtension(ext);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(f), type);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.cant_find_an_app_to_open_file, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.answer:
                if (((TextView) findViewById(R.id.flag_contact_exist)).getText().toString().equals(false + ""))
                    Toast.makeText(getBaseContext(), R.string.add_contact_first, Toast.LENGTH_LONG).show();
                else {
                    Response r = new Response();
                    r.show(getFragmentManager(), "r");
                }
                break;
            case R.id.hash:
                ExplainDialog edlg = new ExplainDialog(ExplainDialog.HASH, PublicStaticVariables.hash);
                edlg.show(getFragmentManager(), "hash");
                break;
            case R.id.session:
                ExplainDialog edl = new ExplainDialog(ExplainDialog.SESSION, PublicStaticVariables.session);
                edl.show(getFragmentManager(), "session");
                break;
            case R.id.replay:
                ExplainDialog ed = new ExplainDialog(ExplainDialog.REPLAY, PublicStaticVariables.timeStamp);
                ed.show(getFragmentManager(), "replay");
                break;
        }
    }

    private void attachFile(final Uri uri) {

        if (uri != null) {
            ImageButton aniView = (ImageButton) findViewById(R.id.add_file);
            aniView.setImageResource(R.drawable.ic_attachment_universal_small);
            Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.rotate);
            animation1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            aniView.startAnimation(animation1);
            aniView.setClickable(false);
            addFile = new Thread(new Runnable() {
                @Override
                public void run() {
                    int r = FilesManagement.addFile(Main.this, uri);
                    if (r == PublicStaticVariables.RESULT_ADD_FILE_OK) {
                        fileName = getFileName(uri);
                        Message msg = hndl.obtainMessage(REPLACE_PHOTO);
                        hndl.sendMessage(msg);

                    } else {
                        Message msg = hndl.obtainMessage(r);
                        hndl.sendMessage(msg);
                    }
                }
            });
            addFile.start();
        }
    }

    Thread addFile;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        FilesManagement.getKeysFromSDCard(this);
        PublicStaticVariables.handleByOnActivityResult = true;
        if (resultCode == RESULT_OK) {
            if (requestCode == ATTACH_FILE) {
                attachFile(intent.getData());
            } else {
                String result = intent.getStringExtra("barcode");
                if (result != null) {
                    switch (PublicStaticVariables.currentLayout) {
                        case R.layout.decrypt:
                            getIntent().putExtra("message", result);
                            setUpViews();
                            break;
                        case R.layout.encrypt:
                            PublicStaticVariables.fileContactCard = new PublicContactCard(this, result);
                            if (PublicStaticVariables.fileContactCard.getPublicKey() != null) {
                                Contact contact1 = PublicStaticVariables.contactsDataSource.findContact(PublicStaticVariables.fileContactCard.getPublicKey());
                                if (contact1 != null) {
                                    t = Toast.makeText(getBaseContext(), R.string.contact_exist,
                                            Toast.LENGTH_LONG);
                                    t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                    t.show();
                                    PublicStaticVariables.fileContactCard = null;
                                    PublicStaticVariables.fragmentManagement.contactChosen(contact1.getId());
                                } else {
                                    AddContactDlg acd = new AddContactDlg();
                                    acd.show(getFragmentManager(), "acd2");
                                }
                            } else
                                Toast.makeText(getBaseContext(), R.string.bad_data,
                                        Toast.LENGTH_LONG).show();
                            break;
                        case R.layout.contacts:
                            PublicStaticVariables.fileContactCard = new PublicContactCard(this, result);
                            if (PublicStaticVariables.fileContactCard.getPublicKey() != null) {
                                if (PublicStaticVariables.contactsDataSource.findContact(PublicStaticVariables.fileContactCard.getPublicKey()) != null) {
                                    t = Toast.makeText(getBaseContext(), R.string.contact_exist,
                                            Toast.LENGTH_LONG);
                                    t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                    t.show();
                                    PublicStaticVariables.fileContactCard = null;
                                } else {
                                    AddContactDlg acd = new AddContactDlg();
                                    acd.show(getFragmentManager(), "acd2");
                                }
                            } else
                                Toast.makeText(getBaseContext(), R.string.bad_data,
                                        Toast.LENGTH_LONG).show();
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
                if (PublicStaticVariables.readyToSend) {
                    EditText et = (EditText) findViewById(R.id.message);
                    userInput = et.getText().toString();
                    // hides the keyboard when the user starts the encryption process
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                    long id = Long.parseLong(((TextView) findViewById(R.id.contact_id_to_send)).getText().toString());
                    contact = PublicStaticVariables.contactsDataSource.findContact(id);
                    encryptManager();
                } else {
                    Toast t = Toast.makeText(this, R.string.send_orders, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    t.show();
                }
                break;
        }
    }

    public void onClick(final View v) {

        switch (PublicStaticVariables.currentLayout) {
            case R.layout.wait_nfc_decrypt:
                Intent i = new Intent(Settings.ACTION_NFC_SETTINGS);
                startActivity(i);
                break;
            case R.layout.decrypt:
                Intent intent = new Intent(Main.this, StartScan.class);
                intent.putExtra("decrypt", true);
                startActivityForResult(intent, SCAN_QR);
                break;
        }
    }

    public void onClickFilter(View v) {
        Intent intt = new Intent(this, StartScan.class);
        startActivityForResult(intt, SCAN_QR);
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
        PublicStaticVariables.contactsDataSource = new ContactsDataSource(this);
        PublicStaticVariables.currentList = PublicStaticVariables.contactsDataSource.getAllContacts();
        PublicStaticVariables.main = this;
        Collections.sort(PublicStaticVariables.currentList, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact contact2) {
                return contact.getEmail().compareTo(contact2.getEmail());
            }
        });
        PublicStaticVariables.fullList = new ArrayList<Contact>();
        PublicStaticVariables.fullList.addAll(PublicStaticVariables.currentList);
        PublicStaticVariables.adapter = new MySimpleArrayAdapter(this, PublicStaticVariables.currentList);
        setContentView(R.layout.main);
        findViewById(R.id.drawer_layout).animate().setDuration(1000).alpha(1).start();
        setUpViews();
    }

    @Override
    public void onNewIntent(Intent i) {
        //TODO find a better solution to deleting keys while on new intent
        handleByOnNewIntent = false;
        if (PublicStaticVariables.currentLayout == R.layout.wait_nfc_to_write) {
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
                    PublicStaticVariables.NFCMode = true;
                    saveKeys.start(this);
                    setUpViews();
                } else
                    handleByOnNewIntent = true;
            }
        } else if (PublicStaticVariables.currentLayout == R.layout.wait_nfc_decrypt) {
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
        if (PublicStaticVariables.currentLayout == R.layout.encrypt || PublicStaticVariables.currentLayout == R.layout.contacts) {
            View b = findViewById(R.id.filter_ll);
            if (PublicStaticVariables.fullList != null && PublicStaticVariables.fullList.size() > 0) {
                if (b.getVisibility() == View.GONE) {
                    b.setVisibility(View.VISIBLE);
                    if (PublicStaticVariables.currentLayout == R.layout.encrypt)
                        PublicStaticVariables.luc.showIfNeeded(null);
                } else {
                    PublicStaticVariables.adapter.refreshList();
                    ((EditText) findViewById(R.id.filter)).setText("");
                    b.setVisibility(View.GONE);
                    if (PublicStaticVariables.currentLayout == R.layout.encrypt)
                        PublicStaticVariables.luc.showIfNeeded(null);
                }
            } else {
                Intent i = new Intent(this, StartScan.class);
                startActivityForResult(i, SCAN_QR);
            }

        } else if (PublicStaticVariables.currentLayout == R.layout.edit_contact) {
            ShareContactDlg sd = new ShareContactDlg();
            sd.show(getFragmentManager(), ((EditText) findViewById(R.id.contact_name)
                    .findViewById(R.id.edit_text)).getText()
                    + ": " + ((EditText) findViewById(R.id.contact_email)
                    .findViewById(R.id.edit_text)).getText());
        } else if (PublicStaticVariables.currentLayout == R.layout.decrypted_msg) {
            PublicStaticVariables.fileContactCard = new PublicContactCard(this
                    , PublicStaticVariables.decryptedMsg.getPublicKey()
                    , PublicStaticVariables.decryptedMsg.getEmail(), PublicStaticVariables.decryptedMsg.getName());
            AddContactDlg acd = new AddContactDlg();
            acd.show(getFragmentManager(), "acd3");
        } else if (PublicStaticVariables.currentLayout == R.layout.share) {
            //ShareDialog dlg = new ShareDialog();
            //dlg.show(getFragmentManager(), "share");
            ShareCustomDialog scd = new ShareCustomDialog();
            scd.show(getFragmentManager(), "scd");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (PublicStaticVariables.currentLayout == R.layout.contacts ||
                PublicStaticVariables.currentLayout == R.layout.encrypt ||
                PublicStaticVariables.currentLayout == R.layout.edit_contact ||
                PublicStaticVariables.currentLayout == R.layout.decrypted_msg ||
                PublicStaticVariables.currentLayout == R.layout.share) {
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
        if (PublicStaticVariables.currentLayout == R.layout.contacts || PublicStaticVariables.currentLayout == R.layout.encrypt) {
            if (PublicStaticVariables.fullList == null || PublicStaticVariables.fullList.size() == 0)
                mi.setIcon(R.drawable.sun);
            else
                mi.setIcon(R.drawable.search);
            TextView textView = (TextView) findViewById(R.id.contact_id_to_send);
            if (textView != null && textView.getText().toString().length() > 0)
                mi.setVisible(false);
            else
                mi.setVisible(!drawerOpen);
            return super.onPrepareOptionsMenu(menu);
        } else if (PublicStaticVariables.currentLayout == R.layout.edit_contact || PublicStaticVariables.currentLayout == R.layout.share) {
            mi.setVisible(!drawerOpen);
            mi.setIcon(android.R.drawable.ic_menu_share);
            return super.onPrepareOptionsMenu(menu);
        } else if (PublicStaticVariables.currentLayout == R.layout.decrypted_msg) {
            TextView tv = (TextView) findViewById(R.id.flag_contact_exist);
            if (tv == null || tv.getText().toString().equals(true + ""))
                return false;
            mi.setVisible(!drawerOpen);
            mi.setIcon(android.R.drawable.ic_menu_add);
            return super.onPrepareOptionsMenu(menu);
        }

        return false;
    }

    @Override
    public void onPause() {
        if (!handleByOnNewIntent) {
            currentKeys = CryptMethods.privateExist() && CryptMethods.publicExist() ? 0 : CryptMethods.publicExist() ? 1 : CryptMethods.privateExist() ? 2 : 3;
            FilesManagement.saveTempDecryptedMSG(this);
            //todo delete view content
            CryptMethods.deleteKeys();
        }
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
            if (menuTitles[menu].equals("Decrypt") && PublicStaticVariables.flag_msg != null && PublicStaticVariables.flag_msg)
                layout = R.layout.decrypted_msg;
            else
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
                    case R.layout.profile:
                        menu = 2;
                        break;
                    default:
                        menu = 0;
                        break;
                }
            }
        }
        // update selected item and title, then close the main
        PublicStaticVariables.currentLayout = layout;
        mDrawerList.setItemChecked(menu, true);
        setTitle(menuTitles[menu]);
        View v = findViewById(PublicStaticVariables.currentLayout);
        if (v != null) v.animate().setDuration(100).alpha(0).start();
        final Fragment fragment = new FragmentManagement();
        final FragmentManager fragmentManager = getFragmentManager();
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment).commit();
                }
            }, 300);
        } else
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
        if (PublicStaticVariables.message != null || msg != null) {
            final ProgressDlg prgd = new ProgressDlg(this, "Decrypting...");
            prgd.setCancelable(false);
            prgd.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CryptMethods.decrypt(msg != null ? msg : PublicStaticVariables.message);
                    getIntent().removeExtra("message");
                    FilesManagement.deleteTempDecryptedMSG(Main.this);
                    PublicStaticVariables.message = null;
                    Message msg = hndl.obtainMessage(DECRYPT_SCREEN);
                    hndl.sendMessage(msg);
                    prgd.cancel();
                }
            }).start();
            getIntent().setData(null);
            return true;
        } else if (PublicStaticVariables.fileContactCard != null) {
            selectItem(-1, R.layout.contacts);
            //TODO search also in names and emails
            Contact c = PublicStaticVariables.contactsDataSource.findContact(PublicStaticVariables.fileContactCard.getPublicKey());
            if (c == null) {
                AddContactDlg acd = new AddContactDlg();
                acd.show(getFragmentManager(), "acd");
            } else {
                //TODO what if some of the details are not exist
                PublicStaticVariables.fileContactCard = null;
                t = Toast.makeText(getBaseContext(),
                        R.string.contact_exist, Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                t.show();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        class prepareToExit {
            final Thread prepareExit = new Thread(new Runnable() {
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
                t = Toast.makeText(getBaseContext(), R.string.exit_by_back_notify, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                t.show();
                prepareExit.start();
            }
        }
        if (exit) {
            FilesManagement.deleteTempDecryptedMSG(this);
            CryptMethods.deleteKeys();
            t.cancel();
            finish();
        } else {
            switch (PublicStaticVariables.currentLayout) {
                case R.layout.encrypt:
                    TextView contactChosen = (TextView) findViewById(R.id.contact_id_to_send);
                    View filter = findViewById(R.id.filter_ll);
                    EditText etMessage = (EditText) findViewById(R.id.message);
                    ImageButton ibFile = (ImageButton) findViewById(R.id.add_file);
                    boolean clearedSomething = false;
                    if (filter.getVisibility() == View.VISIBLE) {
                        filter.setVisibility(View.GONE);
                        ((EditText) filter.findViewById(R.id.filter)).setText("");
                        PublicStaticVariables.adapter.refreshList();
                        clearedSomething = true;
                    }
                    if (etMessage.getText().length() > 0) {
                        clearedSomething = true;
                        etMessage.setText("");
                        PublicStaticVariables.currentText = "";
                    }
                    if (contactChosen.getText().length() > 0) {
                        clearedSomething = true;
                        findViewById(R.id.en_contact).setVisibility(View.GONE);
                        contactChosen.setText("");
                        findViewById(R.id.list).setVisibility(View.VISIBLE);
                        PublicStaticVariables.luc.showIfNeeded(null);
                        invalidateOptionsMenu();
                    }
                    if (PublicStaticVariables.fileContent != null) {
                        clearedSomething = true;
                        PublicStaticVariables.fileContent = null;
                        ibFile.setImageResource(R.drawable.ic_attachment_universal_small);
                        ibFile.clearAnimation();
                    }
                    if (addFile != null && addFile.isAlive()) {
                        addFile.interrupt();
                        addFile = null;
                        clearedSomething = true;
                        ibFile.setClickable(true);
                        PublicStaticVariables.fileContent = null;
                        ibFile.clearAnimation();
                    }
                    if (!clearedSomething)
                        new prepareToExit();
                    break;
                case R.layout.decrypted_msg:
                    if (PublicStaticVariables.flag_msg) {
                        Toast.makeText(this, R.string.notify_msg_deleted, Toast.LENGTH_SHORT).show();
                        PublicStaticVariables.decryptedMsg = null;
                        FilesManagement.deleteTempDecryptedMSG(this);
                    }
                    setUpViews();
                    break;
                case R.layout.decrypt:
                    setUpViews();
                    break;
                case R.layout.share:
                    setUpViews();
                    break;
                case R.layout.contacts:
                    clearedSomething = false;
                    filter = findViewById(R.id.filter_ll);
                    if (filter.getVisibility() == View.VISIBLE) {
                        clearedSomething = true;
                        PublicStaticVariables.adapter.refreshList();
                        ((TextView) filter.findViewById(R.id.filter)).setText("");
                        filter.setVisibility(View.GONE);
                    }
                    if (!clearedSomething)
                        setUpViews();
                    break;
                case R.layout.help:
                    setUpViews();
                    break;
                case R.layout.setup:
                    setUpViews();
                    break;
                case R.layout.edit_contact:
                    //String name = ((TextView) findViewById(R.id.orig_name)).getText().toString();
                    //String email = ((TextView) findViewById(R.id.orig_eamil)).getText().toString();
                    //String newName = ((EditText) findViewById(R.id.contact_name)).getText().toString();
                    //String newEmail = ((EditText) findViewById(R.id.contact_email)).getText().toString();
                    //if (name.equals(newName) && email.equals(newEmail))
                    selectItem(-1, R.layout.contacts);
                    //else {
                    //    ExitWithoutSave dlg = new ExitWithoutSave();
                    //    dlg.show(getFragmentManager(), "exit");
                    //}
                    break;
                case R.layout.profile:
                    selectItem(-1, R.layout.share);
                    break;
                case R.layout.create_new_keys:
                    if (CryptMethods.publicExist())
                        setUpViews();
                    else
                        new prepareToExit();
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

    void sendMessage() {
        boolean success = FilesManagement.createFilesToSend(this, (userInput.length() +
                (PublicStaticVariables.fileContent != null ?
                        PublicStaticVariables.fileContent.length : 0)) <
                PublicStaticVariables.MSG_LIMIT_FOR_QR);
        if (success) {
            ArrayList<Uri> files = FilesManagement.getFilesToSend(this);
            if (files == null)
                Toast.makeText(this, R.string.failed_attach_files, Toast.LENGTH_LONG).show();
            else {
                SendMsgDialog smd = new SendMsgDialog(files, contact.getEmail());
                smd.show(getFragmentManager(), "smd");
            }
        } else {
            Toast.makeText(this, R.string.failed_to_create_files_to_send, Toast.LENGTH_LONG).show();
        }
    }

    int writeTag(Tag tag, byte[] binText) {
        // record to launch Play Store if app is not installed
        NdefRecord appRecord = NdefRecord
                .createApplicationRecord(this.getPackageName());
        byte[] mimeBytes = ("application/" + this.getPackageName())
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

    public void onClickShare(View v) {
        selectItem(-1, R.layout.profile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FilesManagement.getKeysFromSDCard(this);
        int newkeys = CryptMethods.privateExist() && CryptMethods.publicExist() ? 0 : CryptMethods.publicExist() ? 1 : CryptMethods.privateExist() ? 2 : 3;
        if (newkeys != currentKeys) {
            setUpViews();
        } else if (msgSended) {
            onBackPressed();
            msgSended = false;
        }
        //this is for when coming to the app with share
        if (PublicStaticVariables.currentLayout == R.layout.encrypt) {
            attachFile((Uri) getIntent().getParcelableExtra("attach"));
        }
        if (PublicStaticVariables.flag_msg != null && PublicStaticVariables.flag_msg) {
            FilesManagement.getTempDecryptedMSG(this);
        }
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

    public void onClickEditContact(View v) {
        switch (v.getId()) {
            case R.id.delete:
                DeleteContactDialog dlg = new DeleteContactDialog();
                dlg.show(getFragmentManager(), "delete");
                break;
            case R.id.answer:
                Response r = new Response();
                r.show(getFragmentManager(), "n");
                break;
            case R.id.contact_picture:
                ContactQR cqr = new ContactQR();
                cqr.show(getFragmentManager(), "cqr");
                break;
        }
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