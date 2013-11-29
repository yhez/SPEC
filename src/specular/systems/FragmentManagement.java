package specular.systems;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

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
    private final int TURN_TEXT_TRIGGER = 0;
    private final Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TURN_TEXT_TRIGGER:
                    //todo make a better trigger, this one causes problems
                    EditText et = (EditText) getActivity().findViewById(R.id.message);
                    String ss = et.getText() + "";
                    et.setText(" " + ss);
                    et.setText(ss);
                    break;
            }
        }
    };
    //for touch response
    private float startPointX, startPointY, width, height;

    public FragmentManagement() {
        PublicStaticVariables.fragmentManagement = this;
    }

    public static void updateDecryptedScreen(View v, Activity a) {
        TextView tv = (TextView) (v != null ? v.findViewById(R.id.decrypted_msg) : a.findViewById(R.id.decrypted_msg));
        TextView contactExist = (TextView) (v != null ? v.findViewById(R.id.flag_contact_exist) : a.findViewById(R.id.flag_contact_exist));
        TextView sender = (TextView) (v != null ? v.findViewById(R.id.general_details) : a.findViewById(R.id.general_details));
        View fileAttach = (v != null ? v.findViewById(R.id.open_file_rlt) : a.findViewById(R.id.open_file_rlt));
        ImageButton imageButton = (ImageButton) (v != null ? v.findViewById(R.id.open_file) : a.findViewById(R.id.open_file));
        TextView fileName = (TextView) (v != null ? v.findViewById(R.id.file_name) : a.findViewById(R.id.file_name));
        ImageView hs = (ImageView) (v != null ? v.findViewById(R.id.hash_check) : a.findViewById(R.id.hash_check));
        ImageView ss = (ImageView) (v != null ? v.findViewById(R.id.session_check) : a.findViewById(R.id.session_check));
        ImageView rp = (ImageView) (v != null ? v.findViewById(R.id.replay_check) : a.findViewById(R.id.replay_check));
        Contact c = PublicStaticVariables.contactsDataSource.findContact(PublicStaticVariables.friendsPublicKey);
        if (c != null) {
            contactExist.setText(true + "");
            sender.setText("From:\t" + c.getContactName() + " , " + c.getEmail());
        } else {
            sender.setText("From:\t"
                    + PublicStaticVariables.name
                    + " , " + PublicStaticVariables.email);
            contactExist.setText(false + "");
        }
        a.invalidateOptionsMenu();
        if (PublicStaticVariables.file_name == null || PublicStaticVariables.file_name.length() == 0) {
            fileAttach.setVisibility(View.GONE);
        } else {
            fileAttach.setVisibility(View.VISIBLE);
            String ext = PublicStaticVariables.file_name.substring(PublicStaticVariables.file_name.lastIndexOf(".") + 1);
            MimeTypeMap mtm = MimeTypeMap.getSingleton();
            String type = mtm.getMimeTypeFromExtension(ext);
            if (type == null)
                imageButton.setImageResource(R.drawable.unknown2);
            else if (type.startsWith("audio"))
                imageButton.setImageResource(R.drawable.music);
            else if (type.startsWith("video"))
                imageButton.setImageResource(R.drawable.movie);
            else if (type.startsWith("image"))
                imageButton.setImageResource(R.drawable.image);
            else if (type.contains("zip"))
                imageButton.setImageResource(R.drawable.compressed);
            else if (type.contains("text"))
                imageButton.setImageResource(R.drawable.text);
            else if(type.equals("application/vnd.android.package-archive"))
                imageButton.setImageResource(R.drawable.apk);
            else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType(type);
                List<ResolveInfo> matches = a.getPackageManager().queryIntentActivities(intent, 0);
                if (matches.size() > 0)
                    imageButton.setImageDrawable(matches.get(0).loadIcon(a.getPackageManager()));
                else
                    imageButton.setImageResource(R.drawable.unknown2);
            }
        }
        fileName.setText(PublicStaticVariables.file_name);
        tv.setText(PublicStaticVariables.msg_content);
        int ok = R.drawable.ic_ok, notOk = R.drawable.ic_bad;
        hs.setImageResource(PublicStaticVariables.flag_hash ? ok : notOk);
        ss.setImageResource(PublicStaticVariables.flag_session ? ok : notOk);
        rp.setImageResource(PublicStaticVariables.flag_replay ? ok : notOk);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().invalidateOptionsMenu();
        final View rootView = inflater.inflate(PublicStaticVariables.currentLayout,
                container, false);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        if (PublicStaticVariables.luc == null)
            PublicStaticVariables.luc = new LastUsedContacts(getActivity());
        switch (PublicStaticVariables.currentLayout) {
            case create_new_keys:
                addSocialLogin(rootView);
                rootView.findViewById(R.id.gesture).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
                            startPointX = motionEvent.getX();
                        else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP)
                            if (motionEvent.getX() < startPointX) {
                                String myEmail = ((EditText) rootView.findViewById(R.id.email))
                                        .getText().toString();
                                String myName = ((EditText) rootView.findViewById(R.id.name))
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
                ListView lv = (ListView) rootView.findViewById(R.id.list);
                PublicStaticVariables.adapter.refreshList();
                lv.setAdapter(PublicStaticVariables.adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> p1, View p2, int p3,
                                            long p4) {
                        Fragment fragment = new FragmentManagement();
                        Bundle args = new Bundle();
                        PublicStaticVariables.currentLayout = edit_contact;
                        //todo change to index for performance
                        args.putLong("contactId", Long.parseLong(((TextView) p2
                                .findViewById(R.id.id_contact)).getText()
                                .toString()));
                        args.putInt("index", p3);
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, fragment).commit();
                    }
                });
                if (PublicStaticVariables.fullList == null || PublicStaticVariables.fullList.size() == 0) {
                    rootView.findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                } else {
                    rootView.findViewById(R.id.no_contacts).setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
                }
                EditText filterCOnt = (EditText) rootView.findViewById(R.id.filter);
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
                //todo change to index for performance
                Long id = getArguments().getLong("contactId");
                final int index = getArguments().getInt("index");
                ((TextView) rootView.findViewById(R.id.contact_id)).setText(""
                        + id);
                ((TextView) rootView.findViewById(R.id.contact_index)).setText("" + index);
                ((TextView) rootView.findViewById(R.id.contact_name).
                        findViewById(R.id.text_view)).setText(getString(R.string.edit_name) + "\t");
                ((TextView) rootView.findViewById(R.id.contact_email).
                        findViewById(R.id.text_view)).setText(getString(R.string.edit_email) + "\t");
                final Contact currContact = PublicStaticVariables.currentList.get(index);
                final EditText etName = (EditText) rootView.findViewById(R.id.contact_name).findViewById(R.id.edit_text);
                if (PublicStaticVariables.edit == null)
                    PublicStaticVariables.edit = etName.getKeyListener();
                etName.setText(currContact.getContactName());
                etName.setKeyListener(null);
                etName.setFocusable(false);
                ((TextView) rootView.findViewById(R.id.orig_name))
                        .setText(currContact.getContactName());
                final EditText etEmail = (EditText) rootView.findViewById(R.id.contact_email).findViewById(R.id.edit_text);
                etEmail.setText(currContact.getEmail());
                etEmail.setKeyListener(null);
                etEmail.setFocusable(false);
                ((TextView) rootView.findViewById(R.id.orig_eamil))
                        .setText(currContact.getEmail());
                ((TextView) rootView.findViewById(R.id.contact_session))
                        .setText(currContact.getSession());
                ImageButton ibb = (ImageButton) rootView.findViewById(R.id.contact_picture);
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(currContact.getPublicKey(), BarcodeFormat.QR_CODE.toString(), 256);
                Bitmap bitmap = null;
                try {
                    bitmap = qrCodeEncoder.encodeAsBitmap();
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                ibb.setImageBitmap(bitmap);
                TextView tvt = (TextView) rootView.findViewById(R.id.contact_pb);
                tvt.setText(currContact.getPublicKey());
                //todo move it after last line
                //tvt.setTypeface(FilesManagement.getOld(getActivity()));
                final ImageButton ib = (ImageButton) rootView.findViewById(R.id.contact_email)
                        .findViewById(R.id.image_button);
                rootView.findViewById(R.id.contact_email)
                        .findViewById(R.id.image_button).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Contact contact = PublicStaticVariables.contactsDataSource.findContact(Long
                                //        .valueOf(((TextView) rootView.findViewById(R.id.contact_id))
                                //                .getText().toString()));
                                //int index = Integer.parseInt(((TextView) rootView.findViewById(R.id.contact_index)).getText().toString());
                                if (etEmail.getKeyListener() == null) {
                                    Visual.edit(getActivity(), etEmail, ib);
                                    etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                                } else {
                                    String email = etEmail.getText().toString();
                                    String origEmail = ((TextView) rootView.findViewById(R.id.orig_eamil)).getText()
                                            .toString();
                                    if (!email.equals(origEmail))
                                        if (email.length() > 2) {
                                            currContact.update(index, null, email, null, null, -1);
                                            ((TextView) rootView.findViewById(R.id.orig_eamil)).setText(email);
                                        } else {
                                            etEmail.setText(origEmail);
                                            Toast t = Toast.makeText(getActivity(), "change not valid discarded", Toast.LENGTH_LONG);
                                            t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                            t.show();
                                        }
                                    Visual.edit(getActivity(), etEmail, ib);
                                }
                            }
                        }
                );
                rootView.findViewById(R.id.contact_name)
                        .findViewById(R.id.image_button).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Contact contact = PublicStaticVariables.contactsDataSource.findContact(Long
                                //        .valueOf(((TextView) rootView.findViewById(R.id.contact_id))
                                //                .getText().toString()));
                                //int index = Integer.parseInt(((TextView) rootView.findViewById(R.id.contact_index)).getText().toString());

                                ImageButton ib = (ImageButton) getActivity()
                                        .findViewById(R.id.contact_name)
                                        .findViewById(R.id.image_button);
                                if (etName.getKeyListener() == null) {
                                    Visual.edit(getActivity(), etName, ib);
                                    etName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                                } else {
                                    String origName = ((TextView) rootView.findViewById(R.id.orig_name)).getText()
                                            .toString();
                                    String name = etName.getText().toString();
                                    if (!name.equals(origName))
                                        if (name.length() > 2) {
                                            currContact.update(index, name, null, null, null, -1);
                                            ((TextView) rootView.findViewById(R.id.orig_name)).setText(name);
                                        } else {
                                            etName.setText(origName);
                                            Toast t = Toast.makeText(getActivity(), "change not valid discarded",
                                                    Toast.LENGTH_LONG);
                                            t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                            t.show();
                                        }
                                    Visual.edit(getActivity(), etName, ib);
                                }
                            }
                        }
                );
                break;
            case share:
                //todo move it after last line
                //((TextView) rootView.findViewById(R.id.me_public)).setTypeface(FilesManagement.getOld(getActivity()));
                if (FilesManagement.getMyQRPublicKey(getActivity()) != null)
                    ((ImageView) rootView.findViewById(R.id.qr_image))
                            .setImageBitmap(FilesManagement.getMyQRPublicKey(getActivity()));
                ((TextView) rootView.findViewById(R.id.me_public))
                        .setText(CryptMethods.getPublic());
                final ImageView imageView = (ImageView) rootView.findViewById(R.id.qr_image);
                final TextView textView = (TextView) rootView.findViewById(R.id.me_public);
                final FrameLayout frameLayout = (FrameLayout) rootView.findViewById(R.id.touch);
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
                PublicStaticVariables.adapter.refreshList();
                lv = (ListView) rootView.findViewById(R.id.list);
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
                if (PublicStaticVariables.currentList.size() > 0) {
                    rootView.findViewById(R.id.no_contacts).setVisibility(View.GONE);
                    lv.setVisibility(View.VISIBLE);
                } else {
                    rootView.findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }
                PublicStaticVariables.readyToSend = false;
                ((EditText) rootView.findViewById(R.id.filter)).addTextChangedListener(new TextWatcher() {
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
                final EditText et = (EditText) rootView.findViewById(R.id.message);
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
                        String len = ((TextView) rootView.findViewById(R.id.file_content_length)).getText().toString();
                        int num = editable.toString().length() + (len.length() > 0 ?
                                Integer.parseInt(len) : 0);
                        TextView tv = (TextView) rootView.findViewById(R.id.text_counter);
                        ImageButton bt = (ImageButton) rootView.findViewById(R.id.send);
                        boolean choosedContact = ((TextView) rootView.findViewById(R.id.contact_id_to_send)).getText().toString().length() > 0;
                        PublicStaticVariables.readyToSend = choosedContact;
                        bt.setImageResource(choosedContact ? R.drawable.ic_send_holo_light : R.drawable.ic_send_disabled_holo_light);
                        if (num == 0) {
                            tv.setVisibility(View.GONE);
                            bt.setImageResource(R.drawable.ic_send_disabled_holo_light);
                            PublicStaticVariables.readyToSend = false;
                        } else {
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
                TextView tvfl = (TextView) rootView.findViewById(R.id.file_content_length);
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
                TextView tvci = (TextView) rootView.findViewById(R.id.contact_id_to_send);
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
                PublicStaticVariables.luc.showIfNeeded(rootView);
                break;
            case decrypt:
                break;
            case wait_nfc_to_write:
                break;
            case wait_nfc_decrypt:
                if (NfcAdapter.getDefaultAdapter(getActivity()) == null)
                    Toast.makeText(getActivity(), R.string.cant_connect_nfc_adapter, Toast.LENGTH_LONG).show();
                else if (!NfcAdapter.getDefaultAdapter(getActivity()).isEnabled())
                    rootView.findViewById(R.id.ll_wait).setVisibility(View.VISIBLE);
                break;
            case setup:
                break;
            case decrypted_msg:
                if (PublicStaticVariables.decryptedMsg != null) {
                    PublicStaticVariables.flag_hash = PublicStaticVariables.decryptedMsg.checkHash();
                    PublicStaticVariables.flag_replay = PublicStaticVariables.decryptedMsg.checkReplay();
                    PublicStaticVariables.flag_session = PublicStaticVariables.decryptedMsg.checkHash();
                    PublicStaticVariables.friendsPublicKey = PublicStaticVariables.decryptedMsg.getPublicKey();
                    PublicStaticVariables.hash = PublicStaticVariables.decryptedMsg.getHash();
                    PublicStaticVariables.timeStamp = PublicStaticVariables.decryptedMsg.getSentTime();
                    PublicStaticVariables.name = PublicStaticVariables.decryptedMsg.getName();
                    PublicStaticVariables.email = PublicStaticVariables.decryptedMsg.getEmail();
                    PublicStaticVariables.flag_msg = true;
                    PublicStaticVariables.msg_content = PublicStaticVariables.decryptedMsg.getMsgContent();
                    PublicStaticVariables.file_name = PublicStaticVariables.decryptedMsg.getFileName();
                    PublicStaticVariables.session = PublicStaticVariables.decryptedMsg.getSession();
                    updateDecryptedScreen(rootView, getActivity());
                } else if (PublicStaticVariables.flag_msg == null || !PublicStaticVariables.flag_msg) {
                    rootView.findViewById(R.id.top_pannel).setVisibility(View.GONE);
                    rootView.findViewById(R.id.open_file_rlt).setVisibility(View.GONE);
                    rootView.findViewById(R.id.open_file_rlt).setVisibility(View.GONE);
                    rootView.findViewById(R.id.from).setVisibility(View.GONE);
                    ((TextView) rootView.findViewById(R.id.flag_contact_exist)).setText(true + "");
                    ((TextView) rootView.findViewById(R.id.decrypted_msg)).setText(R.string.cant_decrypt);
                    getActivity().invalidateOptionsMenu();
                } else {

                }
                break;
            case R.layout.profile:
                final ImageButton ibMyName = (ImageButton) rootView.findViewById(R.id.test).findViewById(R.id.image_button);
                final ImageButton ibMyEmail = (ImageButton) rootView.findViewById(R.id.test1).findViewById(R.id.image_button);
                final EditText etMyName = (EditText) rootView.findViewById(R.id.test).findViewById(R.id.edit_text);
                final EditText etMyEmail = (EditText) rootView.findViewById(R.id.test1).findViewById(R.id.edit_text);
                final TextView tvMyName = (TextView) rootView.findViewById(R.id.test).findViewById(R.id.text_view);
                final TextView tvMyEmail = (TextView) rootView.findViewById(R.id.test1).findViewById(R.id.text_view);
                tvMyName.setText(R.string.profile_my_name);
                tvMyEmail.setText(R.string.profile_my_email);
                etMyName.setText(CryptMethods.getName());
                if (PublicStaticVariables.edit == null)
                    PublicStaticVariables.edit = etMyName.getKeyListener();
                etMyEmail.setKeyListener(null);
                etMyName.setKeyListener(null);
                etMyEmail.setText(CryptMethods.getEmail());
                ibMyEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etMyEmail.getKeyListener() != null) {
                            Toast.makeText(getActivity(), "do something", Toast.LENGTH_LONG).show();
                        }
                        Visual.edit(getActivity(), etMyEmail, ibMyEmail);
                    }
                });
                ibMyName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etMyName.getKeyListener() != null) {
                            Toast.makeText(getActivity(), "do something", Toast.LENGTH_LONG).show();
                        }
                        Visual.edit(getActivity(), etMyName, ibMyName);
                    }
                });
                break;
        }
        Visual.setAllFonts(getActivity(), (ViewGroup) rootView);

        rootView.animate().setDuration(1000).alpha(1).start();
        return rootView;
    }

    private void addSocialLogin(final View rootView) {
        final Account[] list = ((AccountManager) getActivity()
                .getSystemService(getActivity().ACCOUNT_SERVICE)).getAccounts();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        final List<ResolveInfo> rs = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        for (final Account acc : list) {
            if (acc.name.contains("@")) {
                if (acc.type.equalsIgnoreCase("com.google")) {
                    ImageButton ib = new ImageButton(PublicStaticVariables.main);
                    ib.setBackgroundColor(Color.TRANSPARENT);
                    try {
                        ib.setImageDrawable(PublicStaticVariables.main
                                .getPackageManager()
                                .getApplicationInfo("com.google.android.gm", PackageManager.GET_META_DATA)
                                .loadIcon(getActivity().getPackageManager()));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        //todo another google symbol
                        ib.setImageResource(R.drawable.unknown2);
                    }
                    ib.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((EditText) rootView.findViewById(R.id.email)).setText(acc.name);
                            ((EditText) rootView.findViewById(R.id.name)).setText(acc.name.split("@")[0]);
                        }
                    });
                    ((GridLayout) rootView.findViewById(R.id.grid_login)).addView(ib);

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
                            ib.setImageResource(R.drawable.unknown2);
                        }
                        ib.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((EditText) rootView.findViewById(R.id.email)).setText(acc.name);
                                ((EditText) rootView.findViewById(R.id.name)).setText(acc.name.split("@")[0]);
                            }
                        });
                        ((GridLayout) rootView.findViewById(R.id.grid_login)).addView(ib);
                    } else {
                        //todo add exchange app
                    }
                } else {
                    String company = acc.type.split("\\.")[1];
                    for (ResolveInfo pi : rs) {
                        if (pi.activityInfo.packageName.contains(company)) {
                            ImageButton ib = new ImageButton(getActivity());
                            ib.setImageDrawable(pi.activityInfo.loadIcon(getActivity().getPackageManager()));
                            ib.setBackgroundColor(Color.TRANSPARENT);
                            ib.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ((EditText) rootView.findViewById(R.id.email)).setText(acc.name);
                                    ((EditText) rootView.findViewById(R.id.name)).setText(acc.name.split("@")[0]);
                                }
                            });
                            ((GridLayout) rootView.findViewById(R.id.grid_login)).addView(ib);
                            break;
                        }
                    }
                }
            }
        }
        ImageButton ib = new ImageButton(getActivity());
        ib.setBackgroundColor(Color.TRANSPARENT);
        ib.setImageResource(R.drawable.clear);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditText) rootView.findViewById(R.id.email)).setText("");
                ((EditText) rootView.findViewById(R.id.name)).setText("");
            }
        });
        ((GridLayout) rootView.findViewById(R.id.grid_login)).addView(ib);
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
        PublicStaticVariables.luc.showIfNeeded(null);
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
                        PublicStaticVariables.luc.showIfNeeded(null);
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