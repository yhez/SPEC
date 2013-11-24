package specular.systems;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static specular.systems.R.layout.contacts;
import static specular.systems.R.layout.create_new_keys;
import static specular.systems.R.layout.decrypt;
import static specular.systems.R.layout.decrypted_msg;
import static specular.systems.R.layout.edit_contact;
import static specular.systems.R.layout.encrypt;
import static specular.systems.R.layout.setup;
import static specular.systems.R.layout.share;
import static specular.systems.R.layout.wait_nfc_decrypt;
import static specular.systems.R.layout.wait_nfc_to_write;

public class FragmentManagement extends Fragment {
    //private static Main w;
    private final int TURN_TEXT_TRIGGER = 0,ADD_VIEW=1;
    private final Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TURN_TEXT_TRIGGER:
                    EditText et = (EditText) getActivity().findViewById(R.id.message);
                    String ss = et.getText() + "";
                    et.setText(" " + ss);
                    et.setText(ss);
                    break;
                case ADD_VIEW:
                    ((GridLayout)getActivity().findViewById(R.id.grid_login)).addView((ImageButton)msg.obj);
                    break;
            }
        }
    };
    //for touch response
    private float startPointX, startPointY, width, height;

    public FragmentManagement() {
        PublicStaticVariables.fragmentManagement = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().invalidateOptionsMenu();
        View rootView = inflater.inflate(PublicStaticVariables.currentLayout,
                container, false);
        rootView.animate().setDuration(1000).alpha(1).start();
        return rootView;
    }
private void addSocialLogin(){
    new Thread(new Runnable() {
        @Override
        public void run() {
            AccountManager manager = (AccountManager) getActivity().getSystemService(getActivity().ACCOUNT_SERVICE);
            Account[] list = manager.getAccounts();
            for (final Account acc : list) {
                Log.d("account", acc.toString());
                List<PackageInfo> rs = getActivity().getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES);
                if (acc.type.equalsIgnoreCase("com.google")) {
                    ImageButton ib = new ImageButton(getActivity());
                    ib.setBackgroundColor(Color.TRANSPARENT);
                    try {
                        ib.setImageDrawable(getActivity()
                                .getPackageManager()
                                .getApplicationInfo("com.google.android.gm", PackageManager.GET_META_DATA)
                                .loadIcon(getActivity().getPackageManager()));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        //todo another google symbol
                        ib.setImageResource(R.drawable.unknown);
                    }
                    ib.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((EditText) getActivity().findViewById(R.id.email)).setText(acc.name);
                            ((EditText) getActivity().findViewById(R.id.name)).setText(acc.name.split("@")[0]);
                        }
                    });
                    Message msg = hndl.obtainMessage(ADD_VIEW,ib);
                    hndl.sendMessage(msg);
                } else if (acc.type.startsWith("com.google")) {
                    if (acc.type.contains("pop3")) {
                        ImageButton ib = new ImageButton(getActivity());
                        ib.setBackgroundColor(Color.TRANSPARENT);
                        try {
                            ib.setImageDrawable(getActivity()
                                    .getPackageManager()
                                    .getApplicationInfo("com.google.android.email", PackageManager.GET_META_DATA)
                                    .loadIcon(getActivity().getPackageManager()));
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            ib.setImageResource(R.drawable.unknown);
                        }
                        ib.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((EditText) getActivity().findViewById(R.id.email)).setText(acc.name);
                                ((EditText) getActivity().findViewById(R.id.name)).setText(acc.name.split("@")[0]);
                            }
                        });
                        Message msg = hndl.obtainMessage(ADD_VIEW,ib);
                        hndl.sendMessage(msg);
                    } else {
                        //todo add exchange app
                    }
                } else {
                    String company = acc.type.split("\\.")[1];
                    for (PackageInfo pi : rs) {
                        if (pi.packageName.contains(company)) {
                            ImageButton ib = new ImageButton(getActivity());
                            ib.setImageDrawable(pi.applicationInfo.loadIcon(getActivity().getPackageManager()));
                            ib.setBackgroundColor(Color.TRANSPARENT);
                            ib.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (acc.name.contains("@")) {
                                        ((EditText) getActivity().findViewById(R.id.email)).setText(acc.name);
                                        ((EditText) getActivity().findViewById(R.id.name)).setText(acc.name.split("@")[0]);
                                    } else {
                                        ((EditText) getActivity().findViewById(R.id.name)).setText(acc.name);
                                        ((EditText) getActivity().findViewById(R.id.email)).setText("");
                                    }
                                }
                            });
                            Message msg = hndl.obtainMessage(ADD_VIEW,ib);
                            hndl.sendMessage(msg);
                            break;
                        }
                    }
                }
            }
            ImageButton ib = new ImageButton(getActivity());
            ib.setBackgroundColor(Color.TRANSPARENT);
            ib.setImageResource(R.drawable.clear);
            ib.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((EditText)getActivity().findViewById(R.id.email)).setText("");
                    ((EditText)getActivity().findViewById(R.id.name)).setText("");
                }
            });
            Message msg = hndl.obtainMessage(ADD_VIEW,ib);
            hndl.sendMessage(msg);
        }
    }).start();
}
    @Override
    public void onStart() {
        super.onStart();
        if (PublicStaticVariables.luc == null)
            PublicStaticVariables.luc = new LastUsedContacts(getActivity());
        Contact contact;
        switch (PublicStaticVariables.currentLayout) {
            case create_new_keys:
                addSocialLogin();
                Visual.setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.create_new_keys));
                getActivity().findViewById(R.id.gesture).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
                            startPointX = motionEvent.getX();
                        else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP)
                            if (motionEvent.getX() < startPointX) {
                                String myEmail = ((EditText) getActivity().findViewById(R.id.email))
                                        .getText().toString();
                                String myName = ((EditText) getActivity().findViewById(R.id.name))
                                        .getText().toString();
                                if (!validateEmail(myEmail)) {
                                    Toast t = Toast.makeText(getActivity(), R.string.fill_all,
                                            Toast.LENGTH_LONG);
                                    t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                    t.show();
                                } else {
                                    CryptMethods.setDetails(myName, myEmail);
                                    PublicStaticVariables.main.createKeysManager();
                                }
                            }
                        return true;
                    }
                });
                break;
            case contacts:
                PublicStaticVariables.changed = false;
                ListView lv = (ListView) getActivity().findViewById(R.id.list);
                PublicStaticVariables.adapter.refreshList();
                lv.setAdapter(PublicStaticVariables.adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> p1, View p2, int p3,
                                            long p4) {
                        Fragment fragment = new FragmentManagement();
                        Bundle args = new Bundle();
                        PublicStaticVariables.currentLayout = edit_contact;
                        args.putLong("contactId", Long.parseLong(((TextView) p2
                                .findViewById(R.id.id_contact)).getText()
                                .toString()));
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, fragment).commit();
                    }
                });
                if (PublicStaticVariables.fullList == null || PublicStaticVariables.fullList.size() == 0) {
                    getActivity().findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                } else {
                    getActivity().findViewById(R.id.no_contacts).setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
                }
                EditText filterCOnt = (EditText) getActivity().findViewById(R.id.filter);
                filterCOnt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                        PublicStaticVariables.adapter.getFilter().filter(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                break;
            case edit_contact:
                Long id = getArguments().getLong("contactId");
                ((TextView) getActivity().findViewById(R.id.contact_id)).setText(""
                        + id);
                contact = PublicStaticVariables.contactsDataSource.findContact(id);
                EditText etName = (EditText) getActivity().findViewById(R.id.contact_name);
                etName.setText(contact.getContactName());
                ((TextView) getActivity().findViewById(R.id.orig_name))
                        .setText(contact.getContactName());
                ((EditText) getActivity().findViewById(R.id.contact_email))
                        .setText(contact.getEmail());
                ((TextView) getActivity().findViewById(R.id.orig_eamil))
                        .setText(contact.getEmail());
                ((TextView) getActivity().findViewById(R.id.contact_session))
                        .setText(contact.getSession());
                ((ImageView) getActivity().findViewById(R.id.contact_picture)).setImageBitmap(Contact.getPhoto(contact.getPublicKey()));
                Visual.setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.edit_contact));
                TextView tvt = (TextView) getActivity().findViewById(R.id.contact_pb);
                tvt.setText(contact.getPublicKey());
                tvt.setTypeface(FilesManagement.getOld(getActivity()));
                break;
            case share:
                Visual.setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.share_fl));
                ((TextView) getActivity().findViewById(R.id.me_public)).setTypeface(FilesManagement.getOld(getActivity()));
                if (FilesManagement.getMyQRPublicKey(getActivity()) != null)
                    ((ImageView) getActivity().findViewById(R.id.qr_image))
                            .setImageBitmap(FilesManagement.getMyQRPublicKey(getActivity()));
                ((TextView) getActivity().findViewById(R.id.me_public))
                        .setText(CryptMethods.getPublic());
                final ImageView imageView = (ImageView) getActivity().findViewById(R.id.qr_image);
                final TextView textView = (TextView) getActivity().findViewById(R.id.me_public);
                final FrameLayout frameLayout = (FrameLayout) getActivity().findViewById(R.id.touch);
                final int[] coordination = new int[2];
                frameLayout.getLocationInWindow(coordination);
                //todo find a better way to implement that
                final float x = coordination[0] + frameLayout.getPaddingLeft();
                final float y = coordination[1] - frameLayout.getPaddingTop();
                frameLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                            startPointX = motionEvent.getRawX();
                            startPointY = motionEvent.getRawY();
                            width = frameLayout.getWidth();
                            height = frameLayout.getHeight();
                        } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                            if (frameLayout.getAlpha() < 0.2) {
                                View curr, hidden;
                                if (imageView.getVisibility() == View.VISIBLE) {
                                    curr = imageView;
                                    hidden = textView;
                                } else {
                                    curr = textView;
                                    hidden = imageView;
                                }
                                curr.setVisibility(View.INVISIBLE);
                                hidden.setVisibility(View.VISIBLE);
                            }
                            frameLayout.setX(x);
                            frameLayout.setY(y);
                            frameLayout.setAlpha(1);
                        } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
                            frameLayout.setX(motionEvent.getRawX() - startPointX);
                            frameLayout.setY(motionEvent.getRawY() - startPointY);
                            float fx = Math.abs(motionEvent.getRawX() - startPointX) * 2 / width;
                            float fy = Math.abs(motionEvent.getRawY() - startPointY) * 2 / height;
                            float rslt = (float) Math.sqrt(fx * fx + fy * fy);
                            frameLayout.setAlpha(1 - rslt);
                        }
                        return true;
                    }
                });
                break;
            case encrypt:
                PublicStaticVariables.changed = false;
                if (((TextView) getActivity().findViewById(R.id.contact_id_to_send)).getText().length() == 0) {
                    PublicStaticVariables.adapter.refreshList();
                    lv = (ListView) getActivity().findViewById(R.id.list);
                    if (PublicStaticVariables.fullList != null && PublicStaticVariables.fullList.size() > 0) {
                        View v = getActivity().findViewById(R.id.no_contacts);
                        v.setVisibility(View.GONE);
                        lv.setAdapter(PublicStaticVariables.adapter);
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> p1, View p2, int p3,
                                                    long p4) {
                                contactChosen(Long.parseLong(((TextView) p2
                                        .findViewById(R.id.id_contact)).getText()
                                        .toString()));
                            }
                        });
                        PublicStaticVariables.readyToSend = false;
                        ((EditText) getActivity().findViewById(R.id.filter)).addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                                PublicStaticVariables.adapter.getFilter().filter(charSequence.toString());
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });
                        final EditText et = (EditText) getActivity().findViewById(R.id.message);
                        et.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                if (editable.length() != 0 || PublicStaticVariables.currentText.length() == 1)
                                    PublicStaticVariables.currentText = editable.toString();
                                String len = ((TextView) getActivity().findViewById(R.id.file_content_length)).getText().toString();
                                int num = editable.toString().length() + (len.length() > 0 ?
                                        Integer.parseInt(len) : 0);
                                TextView tv = (TextView) getActivity().findViewById(R.id.text_counter);
                                ImageButton bt = (ImageButton) getActivity().findViewById(R.id.send);
                                boolean choosedContact = ((TextView) getActivity().findViewById(R.id.contact_id_to_send)).getText().toString().length() > 0;
                                PublicStaticVariables.readyToSend = choosedContact;
                                bt.setImageResource(choosedContact ? R.drawable.ic_send_holo_light : R.drawable.ic_send_disabled_holo_light);
                                if (num == 0) {
                                    PublicStaticVariables.changed = choosedContact;
                                    tv.setVisibility(View.GONE);
                                    bt.setImageResource(R.drawable.ic_send_disabled_holo_light);
                                    PublicStaticVariables.readyToSend = false;
                                } else {
                                    PublicStaticVariables.changed = true;
                                    tv.setVisibility(View.VISIBLE);
                                    if (num > 0) {
                                        if (choosedContact) {
                                            PublicStaticVariables.readyToSend = true;
                                            bt.setImageResource(R.drawable.ic_send_holo_light);
                                        }
                                        if (num == 1) {
                                            tv.setVisibility(View.VISIBLE);
                                            tv.setText(PublicStaticVariables.MSG_LIMIT_FOR_QR - num + "");
                                        } else if (num <= PublicStaticVariables.MSG_LIMIT_FOR_QR) {
                                            tv.setText(PublicStaticVariables.MSG_LIMIT_FOR_QR - num + "");
                                        } else
                                            tv.setText(getActivity().getString(R.string.no_qr));
                                    }
                                }
                            }
                        });
                        //todo why is he entering here twice in start???
                        et.setText(PublicStaticVariables.currentText);
                        TextView tvfl = (TextView) getActivity().findViewById(R.id.file_content_length);
                        tvfl.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                Message msg = hndl.obtainMessage(TURN_TEXT_TRIGGER);
                                hndl.sendMessage(msg);
                            }
                        });
                        TextView tvci = (TextView) getActivity().findViewById(R.id.contact_id_to_send);
                        tvci.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                Message msg = hndl.obtainMessage(TURN_TEXT_TRIGGER);
                                hndl.sendMessage(msg);
                            }
                        });
                    } else getActivity().findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
                    TextView tvvv = (TextView) getActivity().findViewById(R.id.contact_id_to_send);
                    if ((tvvv == null || tvvv.getText().toString().length() == 0) &&
                            PublicStaticVariables.fullList.size() > PublicStaticVariables.minContactSize) {
                        if (PublicStaticVariables.luc.show()) {
                            ViewGroup vg = (ViewGroup) getActivity().findViewById(R.id.grid_lasts);
                            for (int af = 0; af < vg.getChildCount(); af++) {
                                final ViewGroup vg2 = (ViewGroup) vg.getChildAt(af);
                                vg2.getChildAt(0).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        contactChosen(Long.parseLong(((TextView) vg2.getChildAt(2)).getText().toString()));
                                    }
                                });
                            }
                        }
                    }
                }
                break;
            case decrypt:
                ((TextView) getActivity().findViewById(R.id.text_decrypt)).setTypeface(FilesManagement.getOs(getActivity()));
                break;
            case wait_nfc_to_write:
                Visual.setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.wait_nfc_to_write));
                break;
            case wait_nfc_decrypt:
                if (NfcAdapter.getDefaultAdapter(getActivity()) == null)
                    Toast.makeText(getActivity(), R.string.cant_connect_nfc_adapter, Toast.LENGTH_LONG).show();
                else if (!NfcAdapter.getDefaultAdapter(getActivity()).isEnabled())
                    getActivity().findViewById(R.id.ll_wait).setVisibility(View.VISIBLE);
                Visual.setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.wait_nfc_decrypt));
                break;
            case setup:
                Visual.setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.setup));
                break;
            case decrypted_msg:
                TextView tv = (TextView) getActivity().findViewById(R.id.decrypted_msg);
                if (PublicStaticVariables.decryptedMsg == null && PublicStaticVariables.flag_hash != null) {
                    if (FilesManagement.getTempDecryptedMSG(getActivity())) {
                        MessageFormat mf = new MessageFormat();
                        PublicStaticVariables.decryptedMsg = mf;
                        //PublicStaticVariables.flag_hash=mf.checkHash();
                        //todo actually check session
                        //PublicStaticVariables.flag_session=mf.checkReplay();
                        //PublicStaticVariables.flag_replay=mf.checkReplay();
                    }
                } else {
                    PublicStaticVariables.flag_hash = null;
                    PublicStaticVariables.flag_session = null;
                    PublicStaticVariables.flag_replay = null;
                }
                if (PublicStaticVariables.decryptedMsg == null) {
                    getActivity().findViewById(R.id.top_pannel).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.from).setVisibility(View.GONE);
                    ((TextView) getActivity().findViewById(R.id.flag_contact_exist)).setText(true + "");
                    tv.setText(getActivity().getString(R.string.cant_decrypt));
                } else {
                    ((TextView) getActivity().findViewById(R.id.general_details))
                            .setText("From:\t" + PublicStaticVariables.decryptedMsg.getName() + " , " + PublicStaticVariables.decryptedMsg.getEmail());
                    Contact c = PublicStaticVariables.contactsDataSource.findContact(PublicStaticVariables.decryptedMsg.getPublicKey());
                    if (c != null)
                        ((TextView) getActivity().findViewById(R.id.flag_contact_exist)).setText(true + "");
                    else
                        ((TextView) getActivity().findViewById(R.id.flag_contact_exist)).setText(false + "");
                    getActivity().invalidateOptionsMenu();
                    if (PublicStaticVariables.decryptedMsg.getFileContent() == null) {
                        if (PublicStaticVariables.decryptedMsg.getFileName().length() > 0)
                            getActivity().findViewById(R.id.open_file_rlt).setVisibility(View.VISIBLE);
                        else
                            getActivity().findViewById(R.id.open_file_rlt).setVisibility(View.GONE);
                    } else {
                        getActivity().findViewById(R.id.open_file_rlt).setVisibility(View.VISIBLE);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String name = PublicStaticVariables.decryptedMsg.getFileName();
                        String ext = name.substring(name.indexOf(".") + 1);
                        MimeTypeMap mtm = MimeTypeMap.getSingleton();
                        String type = mtm.getMimeTypeFromExtension(ext);
                        intent.setType(type);
                        List<ResolveInfo> matches = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                        if (matches.size() > 0)
                            ((ImageButton) getActivity().findViewById(R.id.open_file))
                                    .setImageDrawable(matches.get(0).loadIcon(getActivity().getPackageManager()));
                        else
                            ((ImageButton) getActivity().findViewById(R.id.open_file)).setImageResource(R.drawable.unknown);
                        ((TextView) getActivity().findViewById(R.id.file_name)).setText(name);
                    }
                    tv.setText(PublicStaticVariables.decryptedMsg.getMsgContent());
                    if (PublicStaticVariables.decryptedMsg.checkHash() || (PublicStaticVariables.flag_hash != null && PublicStaticVariables.flag_hash)) {
                        ((ImageView) getActivity().findViewById(R.id.hash_check)).setImageResource(R.drawable.ic_ok);
                        PublicStaticVariables.flag_hash = true;
                    } else {
                        ((ImageView) getActivity().findViewById(R.id.hash_check)).setImageResource(R.drawable.ic_bad);
                        PublicStaticVariables.flag_hash = false;
                    }
                    if (PublicStaticVariables.decryptedMsg.checkReplay() || PublicStaticVariables.flag_replay) {
                        ((ImageView) getActivity().findViewById(R.id.replay_check)).setImageResource(R.drawable.ic_ok);
                        PublicStaticVariables.flag_replay = true;
                    } else {
                        PublicStaticVariables.flag_replay = false;
                        ((ImageView) getActivity().findViewById(R.id.replay_check)).setImageResource(R.drawable.ic_bad);
                    }
                    //todo check session
                    if (PublicStaticVariables.decryptedMsg.checkHash() || PublicStaticVariables.flag_session) {
                        ((ImageView) getActivity().findViewById(R.id.session_check)).setImageResource(R.drawable.ic_ok);
                        PublicStaticVariables.flag_session = true;
                    } else {
                        ((ImageView) getActivity().findViewById(R.id.session_check)).setImageResource(R.drawable.ic_bad);
                        PublicStaticVariables.flag_session = false;
                    }
                }
                Visual.setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.decrypted_msg_ll));

                break;
        }
    }

    private boolean validateEmail(String email) {
        if (email == null) return false;
        String[] parse = email.split("@");
        if (parse.length < 2 || parse.length > 2 || parse[0].length() < 2 || parse[1].length() < 4)
            return false;
        String[] parse2 = parse[1].split("\\.");
        return !(parse2.length < 2 || parse2[0].length() < 2 || parse2[1].length() < 2);
    }

    public void contactChosen(long contactID) {
        getActivity().invalidateOptionsMenu();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().findViewById(R.id.filter).getWindowToken(), 0);
        getActivity().findViewById(R.id.filter_ll).setVisibility(View.GONE);
        getActivity().findViewById(R.id.list).setVisibility(View.GONE);
        PublicStaticVariables.luc.hide();
        Contact cvc = PublicStaticVariables.contactsDataSource.findContact(contactID);
        ((TextView) getActivity().findViewById(R.id.contact_id_to_send)).setText(contactID + "");
        ((TextView) getActivity().findViewById(R.id.chosen_name)).setText(cvc.getContactName());
        ((TextView) getActivity().findViewById(R.id.chosen_email)).setText(cvc.getEmail());
        ((ImageView) getActivity().findViewById(R.id.chosen_icon)).setImageBitmap(Contact.getPhoto(cvc.getPublicKey()));
        final View cont = getActivity().findViewById(R.id.en_contact);
        cont.setVisibility(View.VISIBLE);
        cont.setAlpha(1);
        cont.setX(0);
        cont.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    startPointX = motionEvent.getRawX();
                    width = cont.getWidth();
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    if (cont.getAlpha() < 0.2) {
                        cont.setVisibility(View.GONE);
                        ((TextView) getActivity().findViewById(R.id.contact_id_to_send)).setText("");
                        getActivity().findViewById(R.id.list).setVisibility(View.VISIBLE);
                        PublicStaticVariables.luc.show();
                        getActivity().invalidateOptionsMenu();
                    } else {
                        cont.setAlpha(1);
                        cont.setX(0);
                    }
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
                    cont.setX(motionEvent.getRawX() - startPointX);
                    float f = Math.abs(motionEvent.getRawX() - startPointX) * 2 / width;
                    cont.setAlpha(1 - f);
                }
                return true;
            }
        });
    }
}