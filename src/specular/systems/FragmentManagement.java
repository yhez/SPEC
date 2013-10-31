package specular.systems;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
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

import java.util.List;

import static specular.systems.R.layout.contacts;
import static specular.systems.R.layout.create_new_keys;
import static specular.systems.R.layout.decrypt;
import static specular.systems.R.layout.decrypted_msg;
import static specular.systems.R.layout.edit_contact;
import static specular.systems.R.layout.encrypt;
import static specular.systems.R.layout.setup;
import static specular.systems.R.layout.share;
import static specular.systems.R.layout.wait_nfc_to_write;

class FragmentManagement extends Fragment {
    private static Main w;
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

    public FragmentManagement(Main w) {
        this.w = w;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Main.currentLayout = getArguments().getInt("layout");
        View rootView = inflater.inflate(getArguments().getInt("layout"),
                container, false);
        rootView.animate().setDuration(1000).alpha(1).start();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Contact contact;
        ContactsDataSource cds = new ContactsDataSource(getActivity());
        switch (Main.currentLayout) {
            case create_new_keys:
                setAllFonts(getActivity(),(ViewGroup)getActivity().findViewById(R.id.create_new_keys));
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
                                if (myEmail == null
                                        || !myEmail.contains("@")
                                        || myName == null)
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
                ListView lv = (ListView) getActivity().findViewById(R.id.list);
                cds = new ContactsDataSource(getActivity());
                cds.open();
                List<Contact> alc = cds.getAllContacts();
                cds.close();
                String[] secRowText = new String[alc.size()];

                final MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(
                        getActivity().getBaseContext(), secRowText, alc);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> p1, View p2, int p3,
                                            long p4) {
                        Fragment fragment = new FragmentManagement(w);
                        Bundle args = new Bundle();
                        args.putInt("layout", edit_contact);
                        args.putLong("contactId", Long.parseLong(((TextView) p2
                                .findViewById(R.id.id_contact)).getText()
                                .toString()));
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, fragment).commit();
                    }
                });
                break;
            case edit_contact:
                Long id = getArguments().getLong("contactId");
                ((TextView) getActivity().findViewById(R.id.contact_id)).setText(""
                        + id);
                cds.open();
                contact = cds.findContact(id);
                cds.close();
                ((EditText) getActivity().findViewById(R.id.contact_name))
                        .setText(contact.getContactName());
                ((TextView) getActivity().findViewById(R.id.contact_email))
                        .setText(contact.getEmail());
                ((TextView) getActivity().findViewById(R.id.contact_session))
                        .setText(contact.getSession());
                setAllFonts(getActivity(), (ViewGroup) getActivity().findViewById(R.id.edit_contact));
                TextView tvt = (TextView) getActivity().findViewById(R.id.contact_pb);
                tvt.setText(contact.getPublicKey());
                tvt.setTypeface(FilesManegmant.getOld(getActivity()));
                break;
            case share:
                setAllFonts(getActivity(),(ViewGroup)getActivity().findViewById(R.id.share_fl));
                ((TextView) getActivity().findViewById(R.id.me_public)).setTypeface(FilesManegmant.getOld(getActivity()));
                if (FilesManegmant.getMyQRPublicKey(getActivity()) != null)
                    ((ImageView) getActivity().findViewById(R.id.qr_image))
                            .setImageBitmap(FilesManegmant.getMyQRPublicKey(getActivity()));
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
                lv = (ListView) getActivity().findViewById(R.id.en_list_contact);
                lv.setVisibility(View.VISIBLE);
                cds = new ContactsDataSource(getActivity());
                cds.open();
                alc = cds.getAllContacts();
                cds.close();
                secRowText = new String[alc.size()];
                final MySimpleArrayAdapter adapter2 = new MySimpleArrayAdapter(
                        getActivity().getBaseContext(), secRowText, alc);
                lv.setAdapter(adapter2);
                lv.setAdapter(adapter2);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> p1, View p2, int p3,
                                            long p4) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getActivity().findViewById(R.id.filter).getWindowToken(), 0);
                        getActivity().findViewById(R.id.search).setVisibility(View.GONE);
                        getActivity().findViewById(R.id.en_list_contact).setVisibility(View.GONE);
                        getActivity().findViewById(R.id.en_contact).setVisibility(View.VISIBLE);
                        ContactsDataSource cdsss = new ContactsDataSource(getActivity());
                        cdsss.open();
                        long l = Long.parseLong(((TextView) p2
                                .findViewById(R.id.id_contact)).getText()
                                .toString());
                        Contact cvc = cdsss.findContact(l);
                        cdsss.close();
                        getActivity().findViewById(R.id.filter_ll).setVisibility(View.GONE);
                        ((TextView) getActivity().findViewById(R.id.contact_id_to_send)).setText(l + "");
                        ((TextView) getActivity().findViewById(R.id.en_contact)).setText(cvc + "");
                    }
                });
                getActivity().findViewById(R.id.send).setEnabled(false);
                ((EditText) getActivity().findViewById(R.id.filter)).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        //TODO update list view by query to sqlite
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
                        String len = ((TextView) getActivity().findViewById(R.id.file_content_length)).getText().toString();
                        int num = editable.toString().length() + (len.length() > 0 ?
                                Integer.parseInt(len) : 0);
                        TextView tv = (TextView) getActivity().findViewById(R.id.text_counter);
                        ImageButton bt = (ImageButton) getActivity().findViewById(R.id.send);
                        boolean choosedContact = ((TextView) getActivity().findViewById(R.id.contact_id_to_send)).getText().toString().length() > 0;
                        if (num == 0) {
                            tv.setVisibility(View.GONE);
                            bt.setImageResource(R.drawable.ic_send_disabled_holo_light);
                            bt.setEnabled(false);
                        } else {
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
                break;
            case decrypt:
                ((TextView) getActivity().findViewById(R.id.text_decrypt)).setTypeface(FilesManegmant.getOs(getActivity()));
                break;
            case wait_nfc_to_write:
                setAllFonts(getActivity(),(ViewGroup)getActivity().findViewById(R.id.wait_nfc_to_write));
                break;
            case setup:
                setAllFonts(getActivity(),(ViewGroup)getActivity().findViewById(R.id.setup));
                break;
            case decrypted_msg:
                QRMessage qrm = CryptMethods.decryptedMsg;
                TextView tv = (TextView) getActivity().findViewById(R.id.decrypted_msg);
                tv.setText(qrm != null ? qrm.getMsgContent() : getActivity().getString(R.string.cant_decrypt));
                tv.setTypeface(FilesManegmant.getOs(getActivity()));
                break;
        }
    }

    void setAllFonts(Activity act, ViewGroup v) {
        for (int a = 0; a < v.getChildCount(); a++)
            try {
                setAllFonts(act, (ViewGroup) v.getChildAt(a));
            } catch (Exception e) {
                try {
                    ((TextView) v.getChildAt(a)).setTypeface(FilesManegmant.getOs(act));
                } catch (Exception ee) {
                }
            }
    }
}
