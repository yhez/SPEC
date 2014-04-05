package specular.systems.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.File;
import java.util.Map;

import specular.systems.Backup;
import specular.systems.CameraPreview;
import specular.systems.Contact;
import specular.systems.ContactCard;
import specular.systems.ContactsDataSource;
import specular.systems.ContactsGroup;
import specular.systems.CryptMethods;
import specular.systems.CustomExceptionHandler;
import specular.systems.Dialogs.AddContactDlg;
import specular.systems.Dialogs.BackupDialog;
import specular.systems.Dialogs.ContactQR;
import specular.systems.Dialogs.DeleteContactDialog;
import specular.systems.Dialogs.DialogAddGroup;
import specular.systems.Dialogs.DialogRestore;
import specular.systems.Dialogs.ExplainDialog;
import specular.systems.Dialogs.GenerateKeys;
import specular.systems.Dialogs.GroupCreate;
import specular.systems.Dialogs.InviteToGroup;
import specular.systems.Dialogs.NotImplemented;
import specular.systems.Dialogs.PictureForNfc;
import specular.systems.Dialogs.ProgressDlg;
import specular.systems.Dialogs.Response;
import specular.systems.Dialogs.ShareContactDlg;
import specular.systems.Dialogs.ShareCustomDialog;
import specular.systems.Dialogs.TurnNFCOn;
import specular.systems.FileParser;
import specular.systems.FilesManagement;
import specular.systems.FragmentManagement;
import specular.systems.Group;
import specular.systems.GroupDataSource;
import specular.systems.GroupsAdapter;
import specular.systems.KeysDeleter;
import specular.systems.LeftMenu;
import specular.systems.MessageFormat;
import specular.systems.MySimpleArrayAdapter;
import specular.systems.NfcStuff;
import specular.systems.R;
import specular.systems.Session;
import specular.systems.StaticVariables;
import specular.systems.Visual;
import zxing.QRCodeEncoder;
import zxing.WriterException;

import static android.support.v4.content.FileProvider.getUriForFile;


public class Main extends FragmentActivity {
    private final static int FAILED = 0, REPLACE_PHOTO = 1, CANT_DECRYPT = 2,
            DECRYPT_SCREEN = 3, CHANGE_HINT = 4, DONE_CREATE_KEYS = 53, PROGRESS = 54, CLEAR_AND_SEND = 76,
            RESTORE = 45, ADD_GROUP = 46;
    public static Main main;
    public static boolean exit = false;
    private boolean loadingFile = false;
    private final Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FAILED:
                    break;
                case REPLACE_PHOTO:
                    TextView tv = (TextView) findViewById(R.id.file_content_length);
                    if (tv != null) tv.setText(StaticVariables.fileContent.length + "");
                    invalidateOptionsMenu();
                    break;
                case CANT_DECRYPT:
                    String s = msg.obj != null ? (String) msg.obj : getString(R.string.cant_decrypt);
                    ((TextView) findViewById(R.id.decrypted_msg)).setText(s);
                    break;
                case DECRYPT_SCREEN:
                    if (MessageFormat.decryptedMsg != null) {
                        byte[] file = MessageFormat.decryptedMsg.getFileContent();
                        FilesManagement.createFileToOpen(Main.this, file, MessageFormat.decryptedMsg.getFileName());
                    }
                    selectItem(1, R.layout.decrypted_msg, null);
                    break;
                case FilesManagement.RESULT_ADD_FILE_TO_BIG:
                    Visual.toast(Main.this,R.string.file_to_big);
                    break;
                case FilesManagement.RESULT_ADD_FILE_FAILED:
                    Visual.toast(Main.this,R.string.failed);
                    break;
                case FilesManagement.RESULT_ADD_FILE_EMPTY:
                    Visual.toast(Main.this,R.string.file_is_empty);
                    break;
                case CHANGE_HINT:
                    ((TextView) findViewById(R.id.message)).setHint(R.string.send_another_msg);
                    break;
                case DONE_CREATE_KEYS:
                    if (FragmentManagement.currentLayout == R.layout.recreating_keys) {
                        if (CryptMethods.getPublicTmp() == null) {
                            new createKeys().start();
                        } else {
                            //todo needs to call only on the first key created
                            findViewById(R.id.image_public).clearAnimation();
                            findViewById(R.id.image_public).setClickable(true);
                            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(CryptMethods.getPublicTmp(), 512);
                            try {
                                ((ImageView) findViewById(R.id.image_public)).setImageBitmap(qrCodeEncoder.encodeAsBitmap());
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                            new createKeys().start();
                        }
                    }
                    break;
                case PROGRESS:
                    if (FragmentManagement.currentLayout == R.layout.recreating_keys) {
                        CameraPreview cp = CameraPreview.getCameraPreview();
                        ((TextView) findViewById(R.id.collecting_data)).setText(getString(R.string.collecting_data) + cp.names[cp.currentSensor]);
                        ((ProgressBar) findViewById(R.id.progress_bar)).setProgress((int) (((double) cp.progress) / 64.0 * 100.0));
                        ((ProgressBar) findViewById(R.id.sec_progress_bar)).setProgress((int) (((double) cp.currentSensor + 1) / (double) cp.names.length * 100.0));
                    }
                    break;
                case CLEAR_AND_SEND:
                    clearFields(msg.arg1, msg.arg2 == 1);
                    break;
                case 551:
                    Visual.toast(Main.this,R.string.bad_data);
                    break;
                case RESTORE:
                    setUpViews();
                    new DialogRestore(getFragmentManager(),Backup.restore());
                    break;
                case ADD_GROUP:
                    ContactsGroup.currentPage = 1;
                    selectItem(1, R.layout.encrypt, null);
                    new DialogAddGroup(getFragmentManager());
                    break;
                case 777:
                    contactChosen(true, (Long) msg.obj);
                    break;
            }
        }
    };
    public final static int ATTACH_FILE = 0, SCAN_MESSAGE = 1, SCAN_FOR_GROUP = 2, SCAN_PRIVATE = 3, SCAN_CONTACT = 4, TAKE_PICTURE = 5, RECORD_AUDIO = 6;
    Thread addFile;
    private int defaultScreen;
    private int layouts[];
    private DrawerLayout mDrawerLayout;
    private LeftMenu leftMenu;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] menuTitles;
    private int[] menuDrawables;
    private CharSequence mTitle;
    private String userInput;
    private String fileName = "";
    private Contact contact;
    private Group group;

    public void startCreateKeys() {
        selectItem(-1, R.layout.recreating_keys, getString(R.string.generator_menu_title));
        hndl.sendEmptyMessage(DONE_CREATE_KEYS);
    }

    public void createKeysManager(View v) {
        CryptMethods.doneCreatingKeys = true;
        selectItem(-1, R.layout.wait_nfc_to_write, getString(R.string.save_keys_menu_title));
        if (NfcStuff.nfcIsntAvailable(this))
            onClickSkipNFC(null);
        else if(NfcStuff.nfcIsOff(this)){
            new TurnNFCOn(getFragmentManager());
        }else{
            new PictureForNfc(getFragmentManager());
        }
    }

    void encryptManager(final int type) {
        if (contact != null)
            StaticVariables.luc.change(this, contact);
        final ProgressDlg prgd = new ProgressDlg(this, R.string.encrypting);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sss = contact != null ? contact.getSession().substring(0, contact.getSession().length() - 2) : group.getMentor();
                MessageFormat msg = new MessageFormat(StaticVariables.fileContent, CryptMethods.getMyDetails(Main.this), fileName, userInput,
                        sss);
                String data = Visual.bin2hex(CryptMethods.encrypt(msg.getFormatedMsg(),
                        contact != null ? contact.getPublicKey() : group.getPublicKey()));
                if (data != null)
                    sendMessage(data.getBytes(), type);
                prgd.cancel();
            }
        }).start();
    }

    public void notImp(View v) {
        new NotImplemented(getFragmentManager());
    }

    public void decryptedMsgClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                EditText et = (EditText) findViewById(R.id.message);
                userInput = et.getText().toString();
                // hides the keyboard when the user starts the encryption process
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                contact = ContactsDataSource.contactsDataSource.findContactByKey(StaticVariables.friendsPublicKey);
                encryptManager(SendMsg.MESSAGE);
                break;
            case R.id.open_file:
                Intent oi = new Intent(this, FilesOpener.class);
                oi.putExtra("file_name", StaticVariables.file_name);
                startActivity(oi);
                break;
            case R.id.answer:
                if (((TextView) findViewById(R.id.flag_contact_exist)).getText().toString().equals(false + "")) {
                    Visual.toast(Main.this,R.string.add_contact_first);
                } else {
                    new Response(getFragmentManager());
                }
                break;
            case R.id.hash:
                boolean lightMsg = ((ViewGroup) findViewById(R.id.top_pannel)).getChildAt(2).getVisibility() == View.VISIBLE;
                String hash = getString(R.string.hash_description_1)+"\t\t" + Visual.getSize(StaticVariables.orig_msg_size) + Visual.strings.NEW_LINE;
                hash += getString(R.string.hash_description_2)+"\t\t" + Visual.getSize(StaticVariables.encrypted_msg_size) + Visual.strings.NEW_LINE;
                String[] parts = getResources().getStringArray(R.array.message_parts);
                hash += getString(R.string.hash_description_title);
                int index = 1;
                if (lightMsg) {
                    hash += index++ + ". " + parts[0] + Visual.strings.NEW_LINE + StaticVariables.name + Visual.strings.NEW_LINE;
                    hash += index++ + ". " + parts[1] + Visual.strings.NEW_LINE + StaticVariables.email + Visual.strings.NEW_LINE;
                    hash += index++ + ". " + parts[2] + Visual.strings.NEW_LINE + StaticVariables.friendsPublicKey + Visual.strings.NEW_LINE;
                }
                String q = getString(R.string.divide_msg) + getString(R.string.quote_msg) + getString(R.string.divide_msg);
                if (StaticVariables.msg_content != null && StaticVariables.msg_content.length() > 0) {
                    hash += index++ + ". " + parts[3] + Visual.strings.NEW_LINE + StaticVariables.msg_content.split(q)[0] + Visual.strings.NEW_LINE;
                    if (StaticVariables.msg_content.split(q).length > 1)
                        hash += index++ + ". " + parts[4] + Visual.strings.NEW_LINE + StaticVariables.msg_content.split(q)[1] + Visual.strings.NEW_LINE;
                }
                //todo if coming back from pause the file is no longer in memory
                if (StaticVariables.fileContent != null) {
                    int length = StaticVariables.fileContent.length > 100 ? 100 : StaticVariables.fileContent.length;
                    hash += index++ + ". " + parts[5] + Visual.strings.NEW_LINE + new String(StaticVariables.fileContent, 0, length);
                    if (StaticVariables.fileContent.length > 100)
                        hash += "...";
                    hash += Visual.strings.NEW_LINE;
                    hash += index++ + ". " + parts[6] + Visual.strings.NEW_LINE + StaticVariables.file_name + Visual.strings.NEW_LINE;
                }
                hash += index++ + ". " + parts[7] + Visual.strings.NEW_LINE + StaticVariables.timeStamp + Visual.strings.NEW_LINE;
                if (lightMsg)
                    hash += index++ + ". " + parts[8] + Visual.strings.NEW_LINE + StaticVariables.session + Visual.strings.NEW_LINE;
                hash += index + ". " + parts[9] + Visual.strings.NEW_LINE + StaticVariables.hash;
                new ExplainDialog(this, lightMsg ? ExplainDialog.HASH : ExplainDialog.HASH_QR, hash);
                break;
            case R.id.session:
                String msg;
                switch (StaticVariables.flag_session) {
                    case Session.KNOWN:
                        msg = getString(R.string.session_ok_explain)
                                + Session.toShow(this, StaticVariables.session);
                        break;
                    case Session.DONT_TRUST:
                        contact = ContactsDataSource.contactsDataSource.findContactByKey(StaticVariables.friendsPublicKey);
                        msg = getString(R.string.dont_trust_session_explain)
                                + Session.toShow(this, contact.getSession()) + Visual.strings.NEW_LINE
                                + "other's session is:\n"
                                + Session.toShow(this, StaticVariables.session);
                        break;
                    case Session.JUST_KNOWN:
                        msg = getString(R.string.new_session_trust_created)
                                + Session.toShow(this, StaticVariables.session);
                        break;
                    case Session.AGAIN:
                        msg = getString(R.string.session_try_again)
                                + Session.toShow(this, StaticVariables.session);
                        break;
                    case Session.RESET_SESSION:
                        msg = getString(R.string.session_reset)
                                + Session.toShow(this, StaticVariables.session);
                        break;
                    case Session.UNKNOWN:
                        msg = getString(R.string.unknown_session_explain)
                                + Session.toShow(this, StaticVariables.session);
                        break;

                    case Session.UPDATED:
                        msg = getString(R.string.session_updated)
                                + Session.toShow(this, StaticVariables.session);
                        break;
                    default:
                        msg = Session.toShow(this, StaticVariables.session);
                }
                new ExplainDialog(this, ExplainDialog.SESSION, msg);
                break;
            case R.id.replay:
                String replay = getString(R.string.time_created) + StaticVariables.timeStamp + Visual.strings.NEW_LINE;
                switch (StaticVariables.flag_replay) {
                    case MessageFormat.NOT_RELEVANT:
                        replay += getString(R.string.replay_not_relevant);
                        break;
                    case MessageFormat.OK:
                        replay += getString(R.string.replay_ok);
                        break;
                    case MessageFormat.FAILED:
                        replay += getString(R.string.replay_check_failed);
                        break;
                    case MessageFormat.OLD:
                        replay += getString(R.string.replay_old);
                        break;
                    case MessageFormat.NOT_LATEST:
                        replay += getString(R.string.replay_older_then_latest);
                        break;
                }
                new ExplainDialog(this, ExplainDialog.REPLAY, replay);
                break;
            case R.id.save_attachment:
                final Dialog dialog = new Dialog(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
                dialog.setContentView(R.layout.add_to_save);
                dialog.setTitle(R.string.save_to_safe);
                dialog.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = ((EditText) dialog.findViewById(R.id.name_of_file)).getText().toString();
                        SharedPreferences sp = getSharedPreferences("saved_files", MODE_PRIVATE);
                        Map m = sp.getAll();
                        if (m != null && m.containsValue(name)) {
                            Visual.toast(Main.this,R.string.name_allready_exist);
                            return;
                        }
                        String ext = StaticVariables.file_name.substring(StaticVariables.file_name.lastIndexOf('.') + 1);
                        FilesManagement.saveToSafe(Main.this, name + "." + ext);
                        dialog.cancel();
                        findViewById(R.id.save_attachment).setVisibility(View.GONE);
                    }
                });
                dialog.findViewById(R.id.no_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
                break;
        }
    }

    private void attachFile(final Uri uri) {
        if (uri != null) {
            loadingFile = true;
            StaticVariables.fileContent = null;
            invalidateOptionsMenu();
            addFile = new Thread(new Runnable() {
                @Override
                public void run() {
                    int r = FilesManagement.addFile(Main.this, uri);
                    loadingFile = false;
                    if (r == FilesManagement.RESULT_ADD_FILE_OK) {
                        fileName = Visual.getFileName(Main.this, uri);
                        if (pic != null) {
                            if (!new File(Environment.getExternalStorageDirectory(), Visual.getFileName(Main.this, pic)).delete())
                                new File(getFilesDir() + FilesManagement.TEMP, Visual.getFileName(Main.this, pic)).delete();
                        }
                        hndl.sendEmptyMessage(REPLACE_PHOTO);
                    } else {
                        hndl.sendEmptyMessage(r);
                    }
                }
            });
            addFile.start();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ATTACH_FILE) {
                attachFile(intent.getData());
                intent.setData(null);
            } else if (requestCode == TAKE_PICTURE) {
                attachFile(pic);
            } else if (requestCode == RECORD_AUDIO) {
                pic = intent.getData();
                attachFile(pic);
                intent.setData(null);
            } else {
                String result = intent.getStringExtra("barcode");
                if (requestCode == SCAN_PRIVATE) {
                    Visual.toast(Main.this,R.string.load_private_from_qr);
                } else {
                    int type = FileParser.getType(result);
                    if (type == FileParser.CONTACT_CARD) {
                        StaticVariables.fileContactCard = new ContactCard(this, result);
                        if (StaticVariables.fileContactCard.getPublicKey() != null) {
                            setUpViews();
                        } else {
                            hndl.sendEmptyMessage(551);
                        }
                    } else {
                        getIntent().putExtra("message", result);
                        if (requestCode == SCAN_FOR_GROUP)
                            getIntent().putExtra("id", intent.getLongExtra("id", -1));
                        setUpViews();
                    }
                }
            }
        }
    }

    public void onClickSkipNFC(View v) {
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
            case R.id.send:
                if (StaticVariables.readyToSend) {
                    EditText et = (EditText) findViewById(R.id.message);
                    userInput = et.getText().toString();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                    long id = Long.parseLong(((TextView) findViewById(R.id.contact_id_to_send)).getText().toString());
                    if (findViewById(ContactsGroup.CONTACTS).findViewById(R.id.list).getVisibility() == View.VISIBLE) {
                        group = GroupDataSource.groupDataSource.findGroup(id);
                        encryptManager(SendMsg.MESSAGE_FOR_GROUP);
                    } else {
                        contact = ContactsDataSource.contactsDataSource.findContact(id);
                        encryptManager(SendMsg.MESSAGE);
                    }
                } else {
                    Visual.toast(Main.this,R.string.send_orders);
                }
                break;
        }
    }

    public void onClick(final View v) {
        switch (FragmentManagement.currentLayout) {
            case R.layout.wait_nfc_decrypt:
                Intent i = new Intent(Settings.ACTION_NFC_SETTINGS);
                startActivity(i);
                break;
            case R.layout.decrypt:
                Intent intent = new Intent(Main.this, StartScan.class);
                intent.putExtra("type", StartScan.MESSAGE);
                startActivityForResult(intent, SCAN_MESSAGE);
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
        main = this;
        FilesManagement.getKeysFromSDCard(this);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(), this));
        }
        fragmentManager = getSupportFragmentManager();
        if (ContactsDataSource.fullList == null) {
            ContactsDataSource.contactsDataSource = new ContactsDataSource(this);
            ContactsDataSource.fullList = ContactsDataSource.contactsDataSource.getAllContacts();
        }
        if (GroupDataSource.fullListG == null) {
            GroupDataSource.groupDataSource = new GroupDataSource(this);
            GroupDataSource.fullListG = GroupDataSource.groupDataSource.getAllGroups();
        }
        if (MySimpleArrayAdapter.getAdapter() == null) {
            new MySimpleArrayAdapter(this);
        }
        File folder = new File(getFilesDir() + FilesManagement.REPORTS);
        if (folder.exists() && folder.list().length > 0) {
            Intent i = new Intent(this, SendReport.class);
            startActivity(i);
        }
        folder = new File(getFilesDir() + FilesManagement.ATTACHMENTS);
        if (folder.exists() && folder.list().length > 0)
            for (String s : folder.list())
                new File(folder, s).delete();
        setContentView(R.layout.main);
        findViewById(R.id.drawer_layout).animate().setDuration(1000).alpha(1).start();
        setUpViews();
        created = true;
    }
    private boolean created;
    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        if (i.getAction() != null && i.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED))
            if (FragmentManagement.currentLayout == R.layout.wait_nfc_to_write) {
                int result = NfcStuff.write(i, CryptMethods.getPrivateTmp());
                Visual.toast(Main.this,result);
                FilesManagement.id_picture.save(this);
                if (result == R.string.tag_written) {
                    StaticVariables.NFCMode = true;
                    CryptMethods.moveKeysFromTmp();
                    onClickSkipNFC(null);
                }
            } else {
                byte[] raw = NfcStuff.getData(i);
                if (raw != null) {
                    if (!CryptMethods.setPrivate(raw)) {
                        Visual.toast(Main.this,R.string.cant_find_private_key);
                    }else{
                        leftMenu.notifyDataSetChanged();
                        Visual.toast(Main.this,R.string.keys_loaded);
                    }
                } else {
                    Visual.toast(Main.this,R.string.cant_find_data);
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
        if (FragmentManagement.currentLayout == R.layout.encrypt) {
            if (item.getTitle().equals("Scan")) {
                Intent i = new Intent(this, StartScan.class);
                i.putExtra("type", StartScan.CONTACT);
                startActivityForResult(i, SCAN_CONTACT);
            } else if (item.getTitle().equals("Add")) {
                if (((ViewPager) findViewById(R.id.pager)).getCurrentItem() == 1) {
                    if (((TextView) findViewById(R.id.contact_id_to_send)).length() == 0
                            || findViewById(ContactsGroup.GROUPS).findViewById(R.id.list).getVisibility() == View.VISIBLE) {
                        new GroupCreate(getFragmentManager());
                    } else {
                        if (loadingFile) {
                            Visual.toast(Main.this,R.string.tring_add_another_file_while_loading);
                        } else {
                            showPictureDialog();
                        }
                    }
                } else {
                    if (loadingFile) {
                        Visual.toast(Main.this,R.string.tring_add_another_file_while_loading);
                    } else {
                        showPictureDialog();
                    }
                }
            }
        } else if (FragmentManagement.currentLayout == R.layout.edit_contact) {
            new ShareContactDlg(getFragmentManager());
        } else if (FragmentManagement.currentLayout == R.layout.decrypted_msg) {
            ContactCard pcc = new ContactCard(this
                    , StaticVariables.friendsPublicKey
                    , StaticVariables.email, StaticVariables.name);
            Contact c = ContactsDataSource.contactsDataSource.findContactByEmail(StaticVariables.email);
            new AddContactDlg(getFragmentManager(),pcc, StaticVariables.session, c != null ? c.getId() : -1);
        } else if (FragmentManagement.currentLayout == R.layout.me
                || FragmentManagement.currentLayout == R.layout.profile) {
            share(null);
        }
        return super.onOptionsItemSelected(item);
    }

    Uri pic = null;

    private void showPictureDialog() {
        final Dialog dialog = new Dialog(this, R.style.menu);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.TOP | Gravity.RIGHT);
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.dlg_attach_file);
        dialog.findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(Visual.strings.MIME_ALL);
                Intent i = Intent.createChooser(intent, getString(R.string.choose_file_to_attach));
                startActivityForResult(i, ATTACH_FILE);
            }
        });
        dialog.findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                File path = new File(getFilesDir() + FilesManagement.TEMP);
                if (!path.exists())
                    path.mkdir();
                File f = new File(path, System.currentTimeMillis() + ".jpg");
                pic = getUriForFile(Main.this, getPackageName(), f);
                ResolveInfo lk = getPackageManager().resolveActivity(i, 0);
                grantUriPermission(lk.activityInfo.packageName, pic, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                i.putExtra(MediaStore.EXTRA_OUTPUT, pic);
                startActivityForResult(i, TAKE_PICTURE);
            }
        });
        dialog.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent i = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                if (getPackageManager().queryIntentActivities(i, 0).size() > 0)
                    startActivityForResult(i, RECORD_AUDIO);
            }
        });
        dialog.findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                notImp(null);
            }
        });
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, getActionBar().getHeight(), 0, 0);
        dialog.findViewById(R.id.dlg_attach).setLayoutParams(lp);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    public void share(View v) {
        new ShareCustomDialog(getFragmentManager());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (FragmentManagement.currentLayout == R.layout.encrypt) {
            final SearchView sv = new SearchView(getActionBar().getThemedContext());
            sv.setQueryHint("Search");
            sv.setIconified(false);
            sv.clearFocus();
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(sv.getWindowToken(), 0);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (((ViewPager) findViewById(R.id.pager)).getCurrentItem() == 0) {
                        MySimpleArrayAdapter.getAdapter().updateViewAfterFilter(Main.this);
                        MySimpleArrayAdapter.getAdapter().getFilter().filter(s);
                    } else {
                        GroupsAdapter.getAdapter().updateViewAfterFilter(Main.this);
                        GroupsAdapter.getAdapter().getFilter().filter(s);
                    }
                    return false;
                }
            });
            menu.add(Menu.NONE, Menu.NONE, 1, "Search")
                    .setIcon(R.drawable.search)
                    .setActionView(sv)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            menu.add(Menu.NONE, Menu.NONE, 1, "Scan")
                    .setIcon(R.drawable.sun)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE, Menu.NONE, 1, "Add")
                    .setIcon(android.R.drawable.ic_menu_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            return super.onCreateOptionsMenu(menu);
        }
        if (FragmentManagement.currentLayout == R.layout.me
                || FragmentManagement.currentLayout == R.layout.profile
                || FragmentManagement.currentLayout == R.layout.edit_contact) {
            menu.add(Menu.NONE, Menu.NONE, 1, "Share")
                    .setIcon(android.R.drawable.ic_menu_share).setTitle("Share").setTitleCondensed("Share")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            return super.onCreateOptionsMenu(menu);
        }
        if (FragmentManagement.currentLayout == R.layout.decrypted_msg) {
            menu.add(Menu.NONE, Menu.NONE, 1, "add")
                    .setIcon(android.R.drawable.ic_menu_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            return super.onCreateOptionsMenu(menu);
        }
        return false;
    }

    /* Called whenever we call invalidateOptionsMenu()*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mi = menu.getItem(0);
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        mi.setVisible(!drawerOpen);
        if (menu.size() > 1) {
            MenuItem mi2 = menu.getItem(1);
            MenuItem mi3 = menu.getItem(2);
            mi2.setVisible(!drawerOpen);
            mi3.setVisible(!drawerOpen);
            if (FragmentManagement.currentLayout == R.layout.encrypt) {
                TextView tv = (TextView) findViewById(R.id.contact_id_to_send);
                ViewPager vp = (ViewPager) findViewById(R.id.pager);
                if (vp.getCurrentItem() == 0) {
                    if (ContactsDataSource.fullList == null || ContactsDataSource.fullList.size() == 0) {
                        mi.setVisible(false);
                        mi3.setVisible(false);
                    } else {
                        if (tv != null && tv.length() != 0) {
                            if (findViewById(ContactsGroup.CONTACTS).findViewById(R.id.list).getVisibility() == View.GONE) {
                                mi.setVisible(false);
                                mi2.setVisible(false);
                                if (loadingFile)
                                    mi3.setIcon(android.R.drawable.ic_menu_upload);
                                else if (StaticVariables.fileContent != null)
                                    mi3.setIcon(R.drawable.after_attached);
                                else
                                    mi3.setIcon(R.drawable.attachment);
                            } else
                                mi3.setVisible(false);
                        } else {
                            if ((StaticVariables.currentText != null && StaticVariables.currentText.length() > 0)
                                    || (StaticVariables.fileContent != null && StaticVariables.fileContent.length > 0)
                                    || loadingFile) {
                                mi.setVisible(false);
                                mi2.setVisible(false);
                                if (loadingFile)
                                    mi3.setIcon(android.R.drawable.ic_menu_upload);
                                else if (StaticVariables.fileContent != null)
                                    mi3.setIcon(R.drawable.after_attached);
                                else
                                    mi3.setIcon(R.drawable.attachment);
                            } else {
                                mi3.setVisible(false);
                            }
                        }
                    }
                } else {
                    if (GroupDataSource.fullListG == null || GroupDataSource.fullListG.size() == 0) {
                        mi.setVisible(false);
                    } else if (ContactsDataSource.fullList == null || ContactsDataSource.fullList.size() == 0) {
                        mi3.setVisible(false);
                    } else {
                        if (tv != null && tv.length() != 0) {
                            if (findViewById(ContactsGroup.GROUPS).findViewById(R.id.list).getVisibility() == View.GONE) {
                                mi.setVisible(false);
                                mi2.setVisible(false);
                                if (loadingFile)
                                    mi3.setIcon(android.R.drawable.ic_menu_upload);
                                else if (StaticVariables.fileContent != null)
                                    mi3.setIcon(R.drawable.after_attached);
                                else
                                    mi3.setIcon(R.drawable.attachment);
                            }
                        } else {
                            if ((StaticVariables.currentText != null && StaticVariables.currentText.length() > 0)
                                    || (StaticVariables.fileContent != null && StaticVariables.fileContent.length > 0)
                                    || loadingFile) {
                                mi.setVisible(false);
                                mi2.setVisible(false);
                                if (loadingFile)
                                    mi3.setIcon(android.R.drawable.ic_menu_upload);
                                else if (StaticVariables.fileContent != null)
                                    mi3.setIcon(R.drawable.after_attached);
                                else
                                    mi3.setIcon(R.drawable.attachment);
                            }
                        }
                    }
                }
            }
        } else if (FragmentManagement.currentLayout == R.layout.decrypted_msg) {
            TextView flag_contact = (TextView) findViewById(R.id.flag_contact_exist);
            if (flag_contact != null && flag_contact.getText().toString().equals(true + ""))
                mi.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPause() {
        new KeysDeleter(this);
        FilesManagement.saveTempDecryptedMSG(this);
        Visual.hideKeyBord(this);
        //todo delete view content
        created=false;
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

    FragmentManager fragmentManager;

    private void selectItem(int position, int layout_screen, String title) {
        // update the main content by replacing fragments
        int layout = layout_screen;
        int menu = position;
        if (layout_screen == 0 && position != -1) {
            if (menuTitles[menu].equals("Decrypt") && StaticVariables.flag_msg != null && StaticVariables.flag_msg)
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
                        menu = 1;
                        break;
                    case R.layout.decrypted_msg:
                        menu = 1;
                        break;
                    case R.layout.profile:
                        menu = 1;
                        break;
                    default:
                        menu = 0;
                        break;
                }
            }
        }
        // update selected item and title, then close the main
        FragmentManagement.currentLayout = layout;
        mDrawerList.setItemChecked(menu, true);
        setTitle(title != null ? title : menuTitles[menu]);
        View v = findViewById(FragmentManagement.currentLayout);
        if (v != null) v.animate().setDuration(100).alpha(0).start();
        final Fragment fragment = new FragmentManagement();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss();
        //if (mDrawerLayout.isDrawerOpen(mDrawerList))
        mDrawerLayout.closeDrawer(mDrawerList);
        exit = false;
        Visual.hideKeyBord(this);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (getActionBar() != null)
            getActionBar().setTitle(mTitle);
    }

    private void setUpViews() {
        menuTitles = getResources().getStringArray(R.array.menus);
        menuDrawables =new int[] {R.drawable.encrypt, R.drawable.share
                , R.drawable.explore, R.drawable.manage, R.drawable.learn};
        final int BOTH = 0, PV = 1, PB = 2, NONE = 3;
        int status = CryptMethods.privateExist() && CryptMethods.publicExist() ? BOTH : CryptMethods.privateExist() ? PV : CryptMethods.publicExist() ? PB : NONE;
        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the main
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        if(status==NONE){
            menuTitles = new String[]{menuTitles[3], menuTitles[4]};
            menuDrawables = new int[]{menuDrawables[3], menuDrawables[4]};
        }
        // set up the main's list view with items and click listener
        leftMenu = new LeftMenu(this,menuTitles, menuDrawables);
        mDrawerList.setAdapter(leftMenu);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav main
        ActionBar ab = getActionBar();
        if (ab != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setBackgroundDrawable(getResources().getDrawable(android.R.color.black));
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
                getActionBar().setTitle("Menu");
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        layouts = new int[]{R.layout.encrypt,
                R.layout.me, R.layout.explorer,
                R.layout.setup, R.layout.learn};
        switch (status) {
            case BOTH:
                if (ContactsDataSource.fullList.size() > 3)
                    defaultScreen = R.layout.encrypt;
                else
                    defaultScreen = R.layout.me;
                if (!openByFile()) {
                    selectItem(-1, defaultScreen, null);
                }
                break;
            case PB:
                //todo disable safe button
                String msg = getIntent().getStringExtra("message");
                if (StaticVariables.message != null || msg != null
                        || StaticVariables.fileContactCard != null) {
                    selectItem(0, R.layout.wait_nfc_decrypt, getString(R.string.tab_nfc_title));
                } else {
                    if (ContactsDataSource.fullList.size() > 3) {
                        defaultScreen = R.layout.encrypt;
                        selectItem(0, defaultScreen, null);
                    } else {
                        defaultScreen = R.layout.me;
                        selectItem(1, defaultScreen, null);
                    }
                }
                break;
            case PV:
                if (!openByFile()) {
                    selectItem(0, R.layout.decrypt, null);
                    defaultScreen = R.layout.decrypt;
                }
                break;
            case NONE:
                layouts = new int[]{R.layout.setup, R.layout.learn};
                selectItem(1, R.layout.create_new_keys, getString(R.string.first_time_create_keys));
                defaultScreen = R.layout.create_new_keys;
                msg = getIntent().getStringExtra("message");
                if (StaticVariables.message != null || msg != null) {
                    Visual.toast(Main.this,R.string.no_keys_open_by_msg);
                }
                break;
        }
    }

    public void addUs(View v) {
        StaticVariables.fileContactCard = new ContactCard(this, getString(R.string.spec_contact_card));
        openByFile();
    }

    private boolean openByFile() {
        final String msg = getIntent().getStringExtra("message");
        if (StaticVariables.message != null || msg != null) {
            final ProgressDlg prgd = new ProgressDlg(this, R.string.decrypting);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FragmentManagement.currentLayout = 0;
                    int result;
                    byte[] key = null;
                    long a = getIntent().getLongExtra("id", -1);
                    if (a != -1) {
                        Group g = GroupDataSource.groupDataSource.findGroup(a);
                        if (g != null) {
                            key = CryptMethods.decrypt(g.getPrivateKey());
                        }
                    }
                    result = CryptMethods.decrypt((msg != null ? msg : StaticVariables.message), key);
                    getIntent().removeExtra("message");
                    FilesManagement.deleteTempDecryptedMSG(Main.this);
                    StaticVariables.message = null;
                    if (result == FileParser.ENCRYPTED_MSG || result == FileParser.ENCRYPTED_QR_MSG || result == -1)
                        hndl.sendEmptyMessage(DECRYPT_SCREEN);
                    else if (result == FileParser.ENCRYPTED_BACKUP) {
                        hndl.sendEmptyMessage(RESTORE);
                    } else if (result == FileParser.ENCRYPTED_GROUP) {
                        hndl.sendEmptyMessage(ADD_GROUP);
                    }
                    prgd.cancel();
                }
            }).start();
            getIntent().setData(null);
            return true;
        }
        if (StaticVariables.fileContactCard != null) {
            if (CryptMethods.publicExist() && CryptMethods.privateExist()) {
                ContactsGroup.currentPage = 0;
                selectItem(0, R.layout.encrypt, null);
                final Contact c = ContactsDataSource.contactsDataSource.findContactByKey(StaticVariables.fileContactCard.getPublicKey());
                if (c == null) {
                    Contact cc = ContactsDataSource.contactsDataSource.findContactByEmail(StaticVariables.fileContactCard.getEmail());
                    long id;
                    if (cc != null)
                        id = cc.getId();
                    else
                        id = -1;
                    new AddContactDlg(getFragmentManager(),StaticVariables.fileContactCard, null, id);
                } else {
                    //contactChosen(true, c.getId());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (this) {
                                try {
                                    ((Object) this).wait(1000);
                                } catch (Exception ignore) {}
                            }
                            Message msg = hndl.obtainMessage(777, c.getId());
                            hndl.sendMessage(msg);
                        }
                    }).start();
                    StaticVariables.fileContactCard = null;
                    Visual.toast(Main.this,R.string.contact_exist);
                }
                return true;
            }
            StaticVariables.fileContactCard = null;
            Visual.toast(Main.this,R.string.no_account_trying_add_contact);
            return true;
        }
        //this is for when coming to the app from share
        if (getIntent().getParcelableExtra("specattach") != null) {
            if (FragmentManagement.currentLayout != R.layout.encrypt)
                selectItem(-1, R.layout.encrypt, null);
            attachFile((Uri) getIntent().getParcelableExtra("specattach"));
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
                            ((Object) this).wait(2000);
                            exit = false;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            public prepareToExit() {
                exit = true;
                Visual.toast(Main.this,R.string.exit_by_back_notify);
                prepareExit.start();
            }
        }
        if (exit) {
            FilesManagement.deleteTempDecryptedMSG(this);
            KeysDeleter.delete();
            finish();
        } else {
            switch (FragmentManagement.currentLayout) {
                case R.layout.encrypt:
                    TextView contactChosen = (TextView) findViewById(R.id.contact_id_to_send);
                    EditText etMessage = (EditText) findViewById(R.id.message);
                    TextView fileLength = (TextView) findViewById(R.id.file_content_length);
                    boolean clearedSomething = false;
                    if (etMessage.length() > 0) {
                        clearedSomething = true;
                        etMessage.getText().clear();
                        StaticVariables.currentText = "";
                    }
                    if (contactChosen.length() > 0) {
                        clearedSomething = true;
                        findViewById(R.id.en_contact).setVisibility(View.GONE);
                        contactChosen.setText("");
                        findViewById(R.id.list).setVisibility(View.VISIBLE);
                        StaticVariables.luc.showIfNeeded(this, null);
                    }
                    if (StaticVariables.fileContent != null) {
                        clearedSomething = true;
                        fileLength.setText("");
                        StaticVariables.fileContent = null;
                    }
                    if (addFile != null && addFile.isAlive()) {
                        addFile.interrupt();
                        addFile = null;
                        clearedSomething = true;
                        loadingFile = false;
                        StaticVariables.fileContent = null;
                    }
                    if (!clearedSomething && FragmentManagement.currentLayout == defaultScreen)
                        new prepareToExit();
                    else if (!clearedSomething)
                        setUpViews();
                    else
                        invalidateOptionsMenu();
                    break;
                case R.layout.decrypted_msg:
                    if (StaticVariables.flag_msg != null && StaticVariables.flag_msg) {
                        Visual.toast(Main.this,R.string.notify_msg_deleted);
                        MessageFormat.decryptedMsg = null;
                        FilesManagement.deleteTempDecryptedMSG(this);
                    }
                    setUpViews();
                    break;
                case R.layout.edit_contact:
                    selectItem(-1, R.layout.encrypt, null);
                    break;
                case R.layout.profile:
                    selectItem(-1, R.layout.me, null);
                    break;
                case R.layout.recreating_keys:
                    CryptMethods.removeTemp();
                    CryptMethods.doneCreatingKeys = true;
                    CameraPreview.getCameraPreview().finish();
                    selectItem(-1, R.layout.create_new_keys, CryptMethods.publicExist() ? null : getString(R.string.first_time_create_keys));
                    break;
                case R.layout.create_new_keys:
                    if (FragmentManagement.currentLayout != defaultScreen)
                        selectItem(-1, R.layout.setup, null);
                    else
                        new prepareToExit();
                    break;
                case R.layout.wait_nfc_to_write:
                    if (CryptMethods.publicExist())
                        selectItem(-1, R.layout.create_new_keys, null);
                    else
                        new prepareToExit();
                    break;
                default:
                    if (FragmentManagement.currentLayout == defaultScreen)
                        new prepareToExit();
                    else
                        setUpViews();
                    break;
            }
        }
    }

    void sendMessage(byte[] data, int type) {
        boolean success = FilesManagement.createFilesToSend(this, (userInput.length() +
                (StaticVariables.fileContent != null ?
                        StaticVariables.fileContent.length : 0)) <
                StaticVariables.MSG_LIMIT_FOR_QR, data);
        Message msg = hndl.obtainMessage(CLEAR_AND_SEND, type, success ? 1 : 0);
        hndl.sendMessage(msg);
    }


    public void onClickShare(View v) {
        selectItem(-1, R.layout.profile, null);
    }

    @Override
    public boolean onKeyDown(int key, KeyEvent event) {
        if (key == KeyEvent.KEYCODE_MENU)
            if (mDrawerLayout.isDrawerOpen(mDrawerList))
                mDrawerLayout.closeDrawer(mDrawerList);
            else mDrawerLayout.openDrawer(mDrawerList);
        return super.onKeyDown(key, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcStuff.listen(this, getClass());
        if (StaticVariables.flag_msg != null && StaticVariables.flag_msg) {
            FilesManagement.getTempDecryptedMSG(this);
        }
        if (KeysDeleter.keysDeleted) {
            FilesManagement.getKeysFromSDCard(this);
            KeysDeleter.keysDeleted = false;
        } else {
            KeysDeleter.stop();
        }
        if(!CryptMethods.privateExist()){
            leftMenu.notifyDataSetChanged();
        }
        if(Splash.file){
            Splash.file = false;
            if(created)
                created=false;
            else
                setUpViews();
        }else{
            int newkeys = CryptMethods.privateExist() && CryptMethods.publicExist() ? 0 : CryptMethods.publicExist() ? 1 : CryptMethods.privateExist() ? 2 : 3;
            if (newkeys != KeysDeleter.oldStatus) {
                if(FragmentManagement.currentLayout==R.layout.explorer)
                    selectItem(-1,FragmentManagement.currentLayout,null);
                else
                    setUpViews();
            }
        }
    }

    private void clearFields(int type, boolean success) {
        if (FragmentManagement.currentLayout == R.layout.encrypt) {
            findViewById(R.id.message).clearFocus();
            ((TextView) findViewById(R.id.file_content_length)).setText("");
            ((TextView) findViewById(R.id.contact_id_to_send)).setText("");
            ((TextView) findViewById(R.id.message)).setText("");
            StaticVariables.currentText = "";
            StaticVariables.fileContent = null;
            findViewById(R.id.list).setVisibility(View.VISIBLE);
            findViewById(R.id.en_contact).setVisibility(View.GONE);
            StaticVariables.luc.showIfNeeded(this, null);
            invalidateOptionsMenu();
            if (success) {
                Intent intent = new Intent(this, SendMsg.class);
                intent.putExtra("contactId", contact != null ? contact.getId() : group.getId());
                intent.putExtra("type", type);
                startActivity(intent);
            } else {
                Visual.toast(this, R.string.failed_to_create_files_to_send);
            }
        }
    }

    public void onClickManage(View v) {
        switch (v.getId()) {
            case R.id.button1:
                if (CryptMethods.privateExist()) {
                    new GenerateKeys(getFragmentManager());
                } else {
                    Visual.toast(Main.this,R.string.reject_changes);
                }
                break;
            case R.id.button2:
                int message;
                if (!CryptMethods.publicExist()) {
                    message = R.string.backup_no_public;
                } else if (!CryptMethods.privateExist()) {
                    message = R.string.prevent_backup_no_private;
                } else {
                    message = R.string.backup_explain;
                }
                new BackupDialog(this, message);
                break;
            case R.id.button3:
                Intent intent = new Intent(this, PrivateKeyManager.class);
                startActivity(intent);
                break;
        }
    }

    public void onClickAbout(View v) {
        String[] links = getResources().getStringArray(R.array.links);
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        switch (v.getId()) {
            case R.id.button1:
                i.setData(Uri.parse(links[0]));
                break;
            case R.id.button2:
                i.setData(Uri.parse(links[1]));
                break;
            case R.id.button3:
                i.setData(Uri.parse(links[2]));
                break;
            //case R.id.button4:
            //    i.setData(Uri.parse(links[3]));
            //    break;
            case R.id.button5:
                i.setData(Uri.parse(links[4]));
                break;
        }
        startActivity(i);
    }

    public void onClickEditContact(View v) {
        switch (v.getId()) {
            case R.id.delete:
                if (CryptMethods.privateExist()) {
                    new DeleteContactDialog(getFragmentManager());
                } else {
                    Visual.toast(Main.this,R.string.reject_changes);
                }
                break;
            case R.id.answer:
                new Response(getFragmentManager());
                break;
            case R.id.contact_picture:
                new ContactQR(getFragmentManager());
                break;
            case R.id.invite:
                Group grp = GroupDataSource
                        .groupDataSource
                        .findGroup(Long.parseLong(((TextView) findViewById(R.id.contact_id))
                                .getText().toString()));
                new InviteToGroup(getFragmentManager(),grp);
                break;
            case R.id.add_to_contact:
                grp = GroupDataSource
                        .groupDataSource
                        .findGroup(Long.parseLong(((TextView) findViewById(R.id.contact_id))
                                .getText().toString()));
                String[] det = grp.getOwnerDetails();
                String pbk = det[2];
                String name = det[1];
                String email = det[0];
                StaticVariables.fileContactCard = new ContactCard(this, pbk, email, name);
                openByFile();
                break;
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

    public class createKeys {
        Thread t, p;

        public void start() {
            CryptMethods.doneCreatingKeys = false;
            t = new Thread(new Runnable() {
                @Override
                public void run() {
                    CryptMethods.createKeys();
                    if (FragmentManagement.currentLayout != R.layout.recreating_keys)
                        CryptMethods.doneCreatingKeys = true;
                    else
                        hndl.sendEmptyMessage(DONE_CREATE_KEYS);
                }
            });
            t.start();
            p = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (CryptMethods.doneCreatingKeys)
                            break;
                        hndl.sendEmptyMessage(PROGRESS);
                        synchronized (this) {
                            try {
                                ((Object) this).wait(25);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            p.start();
        }
    }

    /* The click listner for ListView in the navigation main */
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if(1+position<parent.getChildCount())
                selectItem(position, 0, null);
        }
    }

    public void contactChosen(boolean contact, long contactID) {
        invalidateOptionsMenu();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.message).getWindowToken(), 0);
        TextView id = (TextView) findViewById(R.id.contact_id_to_send);
        if (id.length() > 0) {
            if (contact) {
                findViewById(ContactsGroup.GROUPS).findViewById(R.id.en_contact).setVisibility(View.GONE);
                findViewById(ContactsGroup.GROUPS).findViewById(R.id.list).setVisibility(View.VISIBLE);
            } else {
                findViewById(ContactsGroup.CONTACTS).findViewById(R.id.en_contact).setVisibility(View.GONE);
                findViewById(ContactsGroup.CONTACTS).findViewById(R.id.list).setVisibility(View.VISIBLE);
            }
        }
        ViewPager vp = (ViewPager) findViewById(R.id.pager);
        if (vp.getCurrentItem() == 0 && !contact)
            vp.setCurrentItem(1, true);
        else if (vp.getCurrentItem() == 1 && contact)
            vp.setCurrentItem(0, true);
        final View root = findViewById(contact ? ContactsGroup.CONTACTS : ContactsGroup.GROUPS);
        root.findViewById(R.id.list).setVisibility(View.GONE);
        root.findViewById(R.id.no_contacts).setVisibility(View.GONE);
        final View cont = root.findViewById(R.id.en_contact);
        cont.setVisibility(View.VISIBLE);
        id.setText(contactID + "");
        TextView name = (TextView) root.findViewById(R.id.chosen_name);
        TextView email = (TextView) root.findViewById(R.id.chosen_email);
        ImageView icon = (ImageView) root.findViewById(R.id.chosen_icon);
        StaticVariables.luc.showIfNeeded(this, null);
        if (contact) {
            Contact cvc = ContactsDataSource.contactsDataSource.findContact(contactID);
            name.setText(cvc.getContactName());
            email.setText(cvc.getEmail());
            icon.setImageBitmap(cvc.getPhoto());
        } else {
            Group cvc = GroupDataSource.groupDataSource.findGroup(contactID);
            name.setText(cvc.getGroupName());
            email.setText(cvc.getEmail());
            icon.setImageBitmap(cvc.getPhoto());
        }
        EditText myEditText = (EditText) findViewById(R.id.message);
        myEditText.requestFocus();
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(myEditText, InputMethodManager.SHOW_FORCED);
        cont.setAlpha(1);
        root.findViewById(R.id.x).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cont.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.contact_id_to_send)).setText("");
                EditText myEditText = (EditText) findViewById(R.id.message);
                findViewById(R.id.message).clearFocus();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
                root.findViewById(R.id.list).setVisibility(View.VISIBLE);
                StaticVariables.luc.showIfNeeded(Main.this, null);
                invalidateOptionsMenu();
            }
        });
    }
}