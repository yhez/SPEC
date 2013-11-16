package specular.systems;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

class FragmentManagement extends Fragment {
    //public static List<Contact> alc;
    private static Main w;
    public static LastUsedContacts luc;
    final int TURN_TEXT_TRIGGER = 0;
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
            }
        }
    };
    private float startPoint;
    private float width;

    public FragmentManagement(Main w) {
        this.w = w;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().invalidateOptionsMenu();
        View rootView = inflater.inflate(Main.currentLayout,
                container, false);
        rootView.animate().setDuration(1000).alpha(1).start();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Contact contact;
        switch (Main.currentLayout) {
            case create_new_keys:
                setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.create_new_keys));
                getActivity().findViewById(R.id.gesture).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
                            startPoint = motionEvent.getX();
                        else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP)
                            if (motionEvent.getX() < startPoint) {
                                String myEmail = ((EditText) getActivity().findViewById(R.id.email))
                                        .getText().toString();
                                String myName = ((EditText) getActivity().findViewById(R.id.name))
                                        .getText().toString();
                                if (!validateEmail(myEmail))
                                    Toast.makeText(getActivity(), R.string.fill_all,
                                            Toast.LENGTH_LONG).show();
                                else {
                                    CryptMethods.setDetails(myName, myEmail);
                                    w.createKeysManager();
                                }
                            }
                        return true;
                    }
                });
                break;
            case contacts:
                Main.changed = false;
                ListView lv = (ListView) getActivity().findViewById(R.id.list);
                w.refreshList();
                if (Main.fullList!=null&&Main.fullList.size()>0) {
                    getActivity().findViewById(R.id.no_contacts).setVisibility(View.GONE);
                    lv.setAdapter(w.adapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> p1, View p2, int p3,
                                                long p4) {
                            Fragment fragment = new FragmentManagement(w);
                            Bundle args = new Bundle();
                            Main.currentLayout = edit_contact;
                            args.putLong("contactId", Long.parseLong(((TextView) p2
                                    .findViewById(R.id.id_contact)).getText()
                                    .toString()));
                            fragment.setArguments(args);
                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.content_frame, fragment).commit();
                        }
                    });
                } else getActivity().findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
                EditText filterCOnt = (EditText)getActivity().findViewById(R.id.filter);
                filterCOnt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                        w.adapter.getFilter().filter(charSequence.toString());
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
                contact = Main.contactsDataSource.findContact(id);
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
                setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.edit_contact));
                TextView tvt = (TextView) getActivity().findViewById(R.id.contact_pb);
                tvt.setText(contact.getPublicKey());
                tvt.setTypeface(FilesManagement.getOld(getActivity()));
                break;
            case share:
                setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.share_fl));
                ((TextView) getActivity().findViewById(R.id.me_public)).setTypeface(FilesManagement.getOld(getActivity()));
                if (FilesManagement.getMyQRPublicKey(getActivity()) != null)
                    ((ImageView) getActivity().findViewById(R.id.qr_image))
                            .setImageBitmap(FilesManagement.getMyQRPublicKey(getActivity()));
                ((TextView) getActivity().findViewById(R.id.me_public))
                        .setText(CryptMethods.getPublic());
                getActivity().findViewById(R.id.touch).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                            startPoint = motionEvent.getX();
                        else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            if (motionEvent.getX() < startPoint) {
                                View mePublic = getActivity().findViewById(R.id.me_public), qrImage = getActivity().findViewById(R.id.qr_image);
                                if (mePublic.getAlpha() == 0) {
                                    qrImage.animate().setDuration(500).alpha(0).start();
                                    mePublic.animate().setDuration(500).alpha(1).start();
                                } else {
                                    mePublic.animate().setDuration(500).alpha(0).start();
                                    qrImage.animate().setDuration(500).alpha(1).start();
                                }
                            }
                        }
                        return true;
                    }
                });
                break;
            case encrypt:
                Main.changed = false;
                lv = (ListView) getActivity().findViewById(R.id.en_list_contact);
                if (Main.fullList!=null&&Main.fullList.size()>0) {
                    getActivity().findViewById(R.id.no_contacts).setVisibility(View.GONE);
                    lv.setAdapter(w.adapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> p1, View p2, int p3,
                                                long p4) {
                            contactChosen(Long.parseLong(((TextView) p2
                                    .findViewById(R.id.id_contact)).getText()
                                    .toString()));
                        }
                    });
                    getActivity().findViewById(R.id.send).setEnabled(false);
                    ((EditText) getActivity().findViewById(R.id.filter)).addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                            w.adapter.getFilter().filter(charSequence.toString());
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
                            if(editable.length()!=0||Main.currentText.length()==1)
                                Main.currentText=editable.toString();
                            String len = ((TextView) getActivity().findViewById(R.id.file_content_length)).getText().toString();
                            int num = editable.toString().length() + (len.length() > 0 ?
                                    Integer.parseInt(len) : 0);
                            TextView tv = (TextView) getActivity().findViewById(R.id.text_counter);
                            ImageButton bt = (ImageButton) getActivity().findViewById(R.id.send);
                            boolean choosedContact = ((TextView) getActivity().findViewById(R.id.contact_id_to_send)).getText().toString().length() > 0;
                            bt.setEnabled(choosedContact);
                            bt.setImageResource(choosedContact ? R.drawable.ic_send_holo_light : R.drawable.ic_send_disabled_holo_light);
                            if (num == 0) {
                                Main.changed = choosedContact;
                                tv.setVisibility(View.GONE);
                                bt.setImageResource(R.drawable.ic_send_disabled_holo_light);
                                bt.setEnabled(false);
                            } else {
                                Main.changed = true;
                                tv.setVisibility(View.VISIBLE);
                                if (num > 0) {
                                    if (choosedContact) {
                                        bt.setEnabled(true);
                                        bt.setImageResource(R.drawable.ic_send_holo_light);
                                    }
                                    if (num == 1) {
                                        tv.setVisibility(View.VISIBLE);
                                        tv.setText(Main.MSG_LIMIT_FOR_QR - num + "");
                                    } else if (num <= Main.MSG_LIMIT_FOR_QR) {
                                        tv.setText(Main.MSG_LIMIT_FOR_QR - num + "");
                                    } else
                                        tv.setText(getActivity().getString(R.string.no_qr));
                                }
                            }
                        }
                    });
                    //todo why is he entering here twice in start???
                    et.setText(Main.currentText);
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
                luc=new LastUsedContacts(getActivity());
                luc.show();
                if(luc.showed()){
                    ViewGroup vg = (ViewGroup)getActivity().findViewById(R.id.grid_lasts);
                    for(int af=0;af<vg.getChildCount();af++){
                        final ViewGroup vg2=(ViewGroup)vg.getChildAt(af);
                        vg2.getChildAt(0).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                contactChosen(Long.parseLong(((TextView)vg2.getChildAt(2)).getText().toString()));
                            }
                        });
                    }
                }
                break;
            case decrypt:
                ((TextView) getActivity().findViewById(R.id.text_decrypt)).setTypeface(FilesManagement.getOs(getActivity()));
                break;
            case wait_nfc_to_write:
                setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.wait_nfc_to_write));
                break;
            case wait_nfc_decrypt:
                if (NfcAdapter.getDefaultAdapter(getActivity()) == null)
                    Toast.makeText(getActivity(), "something goes wrong", Toast.LENGTH_LONG).show();
                else if (!NfcAdapter.getDefaultAdapter(getActivity()).isEnabled())
                    getActivity().findViewById(R.id.ll_wait).setVisibility(View.VISIBLE);
                setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.wait_nfc_decrypt));
                break;
            case setup:
                setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.setup));
                break;
            case decrypted_msg:
                TextView tv = (TextView) getActivity().findViewById(R.id.decrypted_msg);
                if (CryptMethods.decryptedMsg == null) {
                    getActivity().findViewById(R.id.top_pannel).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.from).setVisibility(View.GONE);
                    tv.setText(getActivity().getString(R.string.cant_decrypt));
                } else {
                    ((TextView) getActivity().findViewById(R.id.general_details))
                            .setText("From:\t" + CryptMethods.decryptedMsg.getName() + " , " + CryptMethods.decryptedMsg.getEmail());
                    Contact c = Main.contactsDataSource.findContact(CryptMethods.decryptedMsg.getPublicKey());
                    if (c != null) {
                        getActivity().findViewById(R.id.add_contact_decrypt).setVisibility(View.GONE);
                    }
                    if (CryptMethods.decryptedMsg.getFileContent() == null)
                        getActivity().findViewById(R.id.open_file).setVisibility(View.GONE);
                    tv.setText(CryptMethods.decryptedMsg.getMsgContent());
                    if (CryptMethods.decryptedMsg.checkHash())
                        ((ImageView) getActivity().findViewById(R.id.hash_check)).setImageResource(R.drawable.ic_ok);
                    else
                        ((ImageView) getActivity().findViewById(R.id.hash_check)).setImageResource(R.drawable.ic_bad);
                    if (CryptMethods.decryptedMsg.checkReplay())
                        ((ImageView) getActivity().findViewById(R.id.replay_check)).setImageResource(R.drawable.ic_ok);
                    else
                        ((ImageView) getActivity().findViewById(R.id.replay_check)).setImageResource(R.drawable.ic_bad);
                    if (CryptMethods.decryptedMsg.checkHash())
                        ((ImageView) getActivity().findViewById(R.id.session_check)).setImageResource(R.drawable.ic_ok);
                    else
                        ((ImageView) getActivity().findViewById(R.id.session_check)).setImageResource(R.drawable.ic_bad);
                }
                setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.decrypted_msg_ll));

                break;
        }
    }

    void setAllFonts(Activity act, ViewGroup v) {
        for (int a = 0; a < v.getChildCount(); a++)
            try {
                setAllFonts(act, (ViewGroup) v.getChildAt(a));
            } catch (Exception e) {
                try {
                    ((TextView) v.getChildAt(a)).setTypeface(FilesManagement.getOs(act));
                } catch (Exception ee) {
                }
            }
    }

    private boolean validateEmail(String email) {
        if (email == null) return false;
        String[] parse = email.split("@");
        if (parse.length < 2 || parse.length > 2 || parse[0].length() < 2 || parse[1].length() < 4)
            return false;
        String[] parse2 = parse[1].split("\\.");
        if (parse2.length < 2 || parse2[0].length() < 2 || parse2[1].length() < 2) return false;
        return true;
    }
    private void contactChosen(long l){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().findViewById(R.id.filter).getWindowToken(), 0);
        getActivity().findViewById(R.id.filter_ll).setVisibility(View.GONE);
        getActivity().findViewById(R.id.en_list_contact).setVisibility(View.GONE);
        Contact cvc = Main.contactsDataSource.findContact(l);
        ((TextView) getActivity().findViewById(R.id.contact_id_to_send)).setText(l + "");
        ((TextView) getActivity().findViewById(R.id.chosen_name)).setText(cvc.getContactName());
        ((TextView) getActivity().findViewById(R.id.chosen_email)).setText(cvc.getEmail());
        ((ImageView) getActivity().findViewById(R.id.chosen_icon)).setImageBitmap(Contact.getPhoto(cvc.getPublicKey()));
        final View cont = getActivity().findViewById(R.id.en_contact);
        cont.setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.grid_lasts).setVisibility(View.GONE);
        cont.setAlpha(1);
        cont.setX(0);
        cont.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    startPoint = motionEvent.getRawX();
                    width = cont.getWidth();
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    if (cont.getAlpha() < 0.2) {
                        cont.setVisibility(View.GONE);
                        ((TextView) getActivity().findViewById(R.id.contact_id_to_send)).setText("");
                        getActivity().findViewById(R.id.en_list_contact).setVisibility(View.VISIBLE);
                    } else {
                        cont.setAlpha(1);
                        cont.setX(0);
                    }
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
                    cont.setX(motionEvent.getRawX() - startPoint);
                    float f = Math.abs(motionEvent.getRawX() - startPoint) * 2 / width;
                    cont.setAlpha(1 - f);
                }
                return true;
            }
        });
    }
}
