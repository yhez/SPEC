package specular.systems;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import specular.systems.Dialogs.TurnNFCOn;
import specular.systems.activities.Main;
import zxing.QRCodeEncoder;
import zxing.WriterException;

import static specular.systems.R.layout.create_new_keys;
import static specular.systems.R.layout.decrypted_msg;
import static specular.systems.R.layout.edit_contact;
import static specular.systems.R.layout.encrypt;
import static specular.systems.R.layout.me;
import static specular.systems.R.layout.setup;
import static specular.systems.R.layout.wait_nfc_decrypt;
import static specular.systems.R.layout.wait_nfc_to_write;

public class FragmentManagement extends Fragment {
    public static int currentLayout = -1;
    private final Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            EditText et = (EditText) rootView.findViewById(R.id.message);
            String ss = et.getText() + "";
            et.setText(" " + ss);
            et.setText(ss);
        }
    };
    View rootView;
    //for touch response
    private float startPointX, startPointY, width, height, x = 0, y = 0;

    public FragmentManagement() {
    }

    public void updateDecryptedScreen() {
        TextView tv = (TextView) rootView.findViewById(R.id.decrypted_msg);
        TextView contactExist = (TextView) rootView.findViewById(R.id.flag_contact_exist);
        TextView sender = (TextView) rootView.findViewById(R.id.general_details);
        View fileAttach = rootView.findViewById(R.id.open_file_rlt);
        View saveAttach = rootView.findViewById(R.id.save_attachment);
        ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.open_file);
        TextView fileName = (TextView) rootView.findViewById(R.id.file_name);
        ImageView hs = (ImageView) rootView.findViewById(R.id.hash_check);
        ImageView ss = (ImageView) rootView.findViewById(R.id.session_check);
        ImageView rp = (ImageView) rootView.findViewById(R.id.replay_check);
        Contact c = ContactsDataSource.contactsDataSource.findContactByKey(StaticVariables.friendsPublicKey);
        if (c != null) {
            contactExist.setText(true + "");
            sender.setText("From:\t" + c.getContactName() + " , " + c.getEmail());
            StaticVariables.flag_session = Session.checkAndUpdate(getActivity(), c, StaticVariables.session);
            StaticVariables.flag_replay = MessageFormat.checkReplay(c, StaticVariables.timeStamp);
        } else {
            sender.setText("From:\t"
                    + StaticVariables.name
                    + " , " + StaticVariables.email);
            contactExist.setText(false + "");
            StaticVariables.flag_session = Session.UNKNOWN;
            StaticVariables.flag_replay = MessageFormat.NOT_RELEVANT;
        }
        if (StaticVariables.file_name == null || StaticVariables.file_name.length() == 0) {
            fileAttach.setVisibility(View.GONE);
            saveAttach.setVisibility(View.GONE);
        } else {
            fileAttach.setVisibility(View.VISIBLE);
            saveAttach.setVisibility(View.VISIBLE);
            String ext = StaticVariables.file_name.substring(StaticVariables.file_name.lastIndexOf('.') + 1);
            MimeTypeMap mtm = MimeTypeMap.getSingleton();
            String type = mtm.getMimeTypeFromExtension(ext);
            if (type == null)
                imageButton.setImageResource(R.drawable.unknown2);
            else if (type.startsWith("audio") || type.equals("application/ogg") || type.equals("video/3gpp"))
                imageButton.setImageResource(R.drawable.music);
            else if (type.startsWith("video"))
                imageButton.setImageResource(R.drawable.movie);
            else if (type.startsWith("image"))
                imageButton.setImageResource(R.drawable.image);
            else if (type.contains("zip"))
                imageButton.setImageResource(R.drawable.compressed);
            else if (type.contains("text"))
                imageButton.setImageResource(R.drawable.text);
            else if (type.equals("application/vnd.android.package-archive"))
                imageButton.setImageResource(R.drawable.apk);
            else if (type.endsWith("pdf"))
                imageButton.setImageResource(R.drawable.pdf);
            else if (ext.equals("doc") || ext.equals("docx"))
                imageButton.setImageResource(R.drawable.word);
            else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType(type);
                List<ResolveInfo> matches = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                if (matches.size() > 0)
                    imageButton.setImageDrawable(matches.get(0).loadIcon(getActivity().getPackageManager()));
                else
                    imageButton.setImageResource(R.drawable.unknown2);
            }
        }
        fileName.setText(StaticVariables.file_name);
        tv.setText(StaticVariables.msg_content);
        int ok = R.drawable.ic_ok, notOk = R.drawable.ic_bad,
                /*unknown = R.drawable.ic_unknown,*/ starting = R.drawable.ic_what;
        hs.setImageResource(StaticVariables.flag_hash ? ok : notOk);
        if (StaticVariables.flag_session == Session.KNOWN
                || StaticVariables.flag_session == Session.JUST_KNOWN
                || StaticVariables.flag_session == Session.UPDATED)
            ss.setImageResource(ok);
        else if (StaticVariables.flag_session == Session.DONT_TRUST)
            ss.setImageResource(notOk);
        else if (StaticVariables.flag_session == Session.UNKNOWN)
            ss.setVisibility(View.GONE);
        else if (StaticVariables.flag_session == Session.RESET_SESSION
                || StaticVariables.flag_session == Session.AGAIN)
            ss.setImageResource(starting);
        if (StaticVariables.flag_replay == MessageFormat.NOT_RELEVANT)
            rp.setVisibility(View.GONE);
        else if (StaticVariables.flag_replay == MessageFormat.OK)
            rp.setImageResource(ok);
        else
            rp.setImageResource(notOk);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(currentLayout,
                container, false);
        if (StaticVariables.luc == null)
            StaticVariables.luc = new LastUsedContacts(getActivity());
        switch (currentLayout) {
            case create_new_keys:
                addSocialLogin();
                final EditText etEmail1 = (EditText) rootView.findViewById(R.id.email), etName1 = (EditText) rootView.findViewById(R.id.name);
                etEmail1.setFilters(Visual.filters());
                etName1.setFilters(Visual.filters());
                final ImageView iv = (ImageView) rootView.findViewById(R.id.gesture);
                final Animation animation1 = AnimationUtils.loadAnimation(getActivity(), R.anim.up_down);
                iv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                            startPointX = motionEvent.getRawX();
                            iv.startAnimation(animation1);
                        } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                            iv.clearAnimation();
                            if (motionEvent.getX() < startPointX) {
                                String myEmail = etEmail1.getText().toString();
                                String myName = etName1.getText().toString();
                                if (myName.length() == 0) {
                                    myName = myEmail.split("@")[0];
                                }
                                if (myName.length() < 3 && myName.length() > 0) {
                                    Visual.toast(getActivity(), R.string.too_short_name);
                                } else {
                                    if (!validateEmail(myEmail)) {
                                        Visual.toast(getActivity(), R.string.fill_all);
                                    } else {
                                        CryptMethods.setDetails(myName, myEmail);
                                        Main.main.startCreateKeys();
                                    }
                                }
                            }
                        }
                        return true;
                    }
                });
                break;
            case edit_contact:
                boolean group = getArguments().getBoolean("groups", false);
                int index = getArguments().getInt("index");
                if (group) {
                    LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.group_details);
                    ll.setVisibility(View.VISIBLE);
                    final Group currContact = GroupsAdapter.getAdapter().getItem(index);
                    String[] ownDet = currContact.getOwnerDetails();
                    ((TextView) ll.findViewById(R.id.name)).setText(ownDet[0]);
                    ((TextView) ll.findViewById(R.id.email)).setText(ownDet[1]);
                    ((CheckBox) ll.findViewById(R.id.reinvite)).setChecked(currContact.getLimitInvite());
                    if (currContact.getLimitInvite() && !currContact.getOwnerDetails()[2].equals(CryptMethods.getPublic()))
                        ll.findViewById(R.id.invite).setVisibility(View.GONE);
                    ((CheckBox) ll.findViewById(R.id.nfc)).setChecked(currContact.getLimitNFC());
                    ((TextView) rootView.findViewById(R.id.contact_id)).setText(""
                            + currContact.getId());
                    ((TextView) rootView.findViewById(R.id.contact_name).
                            findViewById(R.id.text_view)).setText(getString(R.string.edit_name) + Visual.strings.TAB);
                    ((TextView) rootView.findViewById(R.id.contact_email).
                            findViewById(R.id.text_view)).setText("address" + Visual.strings.TAB);
                    if (currContact.getDefaultApp() != null) {
                        Intent i = new Intent();
                        i.setComponent(currContact.getDefaultApp());
                        ResolveInfo rs = getActivity().getPackageManager().resolveActivity(i, 0);
                        final ImageButton ibbb = (ImageButton) rootView.findViewById(R.id.default_app_send);
                        ibbb.setImageDrawable(rs.loadIcon(getActivity().getPackageManager()));
                        rootView.findViewById(R.id.default_app_send_ll).setVisibility(View.VISIBLE);
                        ibbb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                currContact.update(null, getActivity());
                                rootView.findViewById(R.id.default_app_send_ll).setVisibility(View.GONE);
                            }
                        });
                    }
                    final EditText etName = (EditText) rootView.findViewById(R.id.contact_name).findViewById(R.id.edit_text);
                    if (StaticVariables.edit == null)
                        StaticVariables.edit = etName.getKeyListener();
                    etName.setText(currContact.getGroupName());
                    etName.setKeyListener(null);
                    etName.setFocusable(false);
                    ((TextView) rootView.findViewById(R.id.orig_name))
                            .setText(currContact.getGroupName());
                    final EditText etEmail = (EditText) rootView.findViewById(R.id.contact_email).findViewById(R.id.edit_text);
                    etEmail.setText(currContact.getEmail());
                    etEmail.setKeyListener(null);
                    etEmail.setFocusable(false);
                    ((TextView) rootView.findViewById(R.id.orig_eamil))
                            .setText(currContact.getEmail());
                    ((TextView) rootView.findViewById(R.id.contact_session))
                            .setText(currContact.getMentor());
                    ImageButton ibb = (ImageButton) rootView.findViewById(R.id.contact_picture);
                    QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(currContact.getPublicKey(), 256);
                    Bitmap bitmap = null;
                    try {
                        bitmap = qrCodeEncoder.encodeAsBitmap();
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    ibb.setImageBitmap(bitmap);
                    TextView tvt = (TextView) rootView.findViewById(R.id.contact_pb);
                    tvt.setText(currContact.getPublicKey());
                    final ImageButton ib = (ImageButton) rootView.findViewById(R.id.contact_email)
                            .findViewById(R.id.image_button);
                    rootView.findViewById(R.id.contact_email)
                            .findViewById(R.id.image_button).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (etEmail.getKeyListener() == null) {
                                        Visual.edit(getActivity(), etEmail, ib);
                                        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                                        etEmail.setSelection(etEmail.length());
                                    } else {
                                        String email = etEmail.getText().toString();
                                        String origEmail = ((TextView) rootView.findViewById(R.id.orig_eamil)).getText()
                                                .toString();
                                        if (!email.equals(origEmail))
                                            if (email.length() > 2) {
                                                currContact.update(getActivity(), null, email, null, null);
                                                ((TextView) rootView.findViewById(R.id.orig_eamil)).setText(email);
                                            } else {
                                                etEmail.setText(origEmail);
                                                Visual.toast(getActivity(), R.string.not_valid_change);
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
                                    ImageButton ib = (ImageButton) getActivity()
                                            .findViewById(R.id.contact_name)
                                            .findViewById(R.id.image_button);
                                    if (etName.getKeyListener() == null) {
                                        Visual.edit(getActivity(), etName, ib);
                                        etName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                                        etName.setSelection(etName.length());
                                    } else {
                                        String origName = ((TextView) rootView.findViewById(R.id.orig_name)).getText()
                                                .toString();
                                        String name = etName.getText().toString();
                                        if (!name.equals(origName))
                                            if (name.length() > 2) {
                                                currContact.update(getActivity(), name, null, null, null);
                                                ((TextView) rootView.findViewById(R.id.orig_name)).setText(name);
                                            } else {
                                                etName.setText(origName);
                                                Visual.toast(getActivity(), R.string.not_valid_change);
                                            }
                                        Visual.edit(getActivity(), etName, ib);
                                    }
                                }
                            }
                    );
                } else {
                    final Contact currContact = MySimpleArrayAdapter.getAdapter().getItem(index);
                    ((TextView) rootView.findViewById(R.id.contact_id)).setText(""
                            + currContact.getId());
                    ((TextView) rootView.findViewById(R.id.contact_name).
                            findViewById(R.id.text_view)).setText(getString(R.string.edit_name) + Visual.strings.TAB);
                    ((TextView) rootView.findViewById(R.id.contact_email).
                            findViewById(R.id.text_view)).setText(getString(R.string.edit_email) + Visual.strings.TAB);
                    if (currContact.getDefaultApp() != null) {
                        Intent i = new Intent();
                        i.setComponent(currContact.getDefaultApp());
                        ResolveInfo rs = getActivity().getPackageManager().resolveActivity(i, 0);
                        final ImageButton ibbb = (ImageButton) rootView.findViewById(R.id.default_app_send);
                        ibbb.setImageDrawable(rs.loadIcon(getActivity().getPackageManager()));
                        rootView.findViewById(R.id.default_app_send_ll).setVisibility(View.VISIBLE);
                        ibbb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                currContact.update(null, getActivity());
                                rootView.findViewById(R.id.default_app_send_ll).setVisibility(View.GONE);
                            }
                        });
                    }
                    final EditText etName = (EditText) rootView.findViewById(R.id.contact_name).findViewById(R.id.edit_text);
                    if (StaticVariables.edit == null)
                        StaticVariables.edit = etName.getKeyListener();
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
                    String sessions;
                    if (CryptMethods.privateExist())
                        sessions = Session.toShow(getActivity(), currContact.getSession());
                    else
                        sessions = Session.toHide(getActivity());
                    ((TextView) rootView.findViewById(R.id.contact_session))
                            .setText(sessions);
                    ImageButton ibb = (ImageButton) rootView.findViewById(R.id.contact_picture);
                    QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(currContact.getPublicKey(), 256);
                    Bitmap bitmap = null;
                    try {
                        bitmap = qrCodeEncoder.encodeAsBitmap();
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    ibb.setImageBitmap(bitmap);
                    TextView tvt = (TextView) rootView.findViewById(R.id.contact_pb);
                    tvt.setText(currContact.getPublicKey());
                    final ImageButton ib = (ImageButton) rootView.findViewById(R.id.contact_email)
                            .findViewById(R.id.image_button);
                    rootView.findViewById(R.id.contact_email)
                            .findViewById(R.id.image_button).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (etEmail.getKeyListener() == null) {
                                        Visual.edit(getActivity(), etEmail, ib);
                                        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                                        etEmail.setSelection(etEmail.length());
                                    } else {
                                        String email = etEmail.getText().toString();
                                        String origEmail = ((TextView) rootView.findViewById(R.id.orig_eamil)).getText()
                                                .toString();
                                        if (!email.equals(origEmail))
                                            if (email.length() > 2) {
                                                currContact.update(getActivity(), null, email, null, null);
                                                ((TextView) rootView.findViewById(R.id.orig_eamil)).setText(email);
                                            } else {
                                                etEmail.setText(origEmail);
                                                Visual.toast(getActivity(), R.string.not_valid_change);
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
                                    ImageButton ib = (ImageButton) getActivity()
                                            .findViewById(R.id.contact_name)
                                            .findViewById(R.id.image_button);
                                    if (etName.getKeyListener() == null) {
                                        Visual.edit(getActivity(), etName, ib);
                                        etName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                                        etName.setSelection(etName.length());
                                    } else {
                                        String origName = ((TextView) rootView.findViewById(R.id.orig_name)).getText()
                                                .toString();
                                        String name = etName.getText().toString();
                                        if (!name.equals(origName))
                                            if (name.length() > 2) {
                                                currContact.update(getActivity(), name, null, null, null);
                                                ((TextView) rootView.findViewById(R.id.orig_name)).setText(name);
                                            } else {
                                                etName.setText(origName);
                                                Visual.toast(getActivity(), R.string.not_valid_change);
                                            }
                                        Visual.edit(getActivity(), etName, ib);
                                    }
                                }
                            }
                    );
                }
                break;
            case me:
                if (FilesManagement.getMyQRPublicKey(getActivity()) != null)
                    ((ImageView) rootView.findViewById(R.id.qr_image))
                            .setImageBitmap(FilesManagement.getMyQRPublicKey(getActivity()));
                final ImageView imageView = (ImageView) rootView.findViewById(R.id.qr_image);
                final TextView textView = (TextView) rootView.findViewById(R.id.me_public);
                textView.setText(CryptMethods.getPublic());
                //textView.
                final FrameLayout frameLayout = (FrameLayout) rootView.findViewById(R.id.touch);
                frameLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                            startPointX = motionEvent.getRawX();
                            startPointY = motionEvent.getRawY();
                            width = frameLayout.getWidth();
                            height = frameLayout.getHeight();
                            x = frameLayout.getX();
                            y = frameLayout.getY();
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
                final ViewPager vp = (ViewPager) rootView.findViewById(R.id.pager);
                vp.post(new Runnable() {
                    @Override
                    public void run() {
                        ContactsGroup cg = new ContactsGroup(Main.main.getSupportFragmentManager());
                        vp.setCurrentItem(ContactsGroup.currentPage);
                        vp.setAdapter(cg);
                        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int i, float v, int i2) {

                            }

                            @Override
                            public void onPageSelected(int i) {
                                ContactsGroup.currentPage = i;
                                getActivity().invalidateOptionsMenu();
                                StaticVariables.luc.showIfNeeded(getActivity(), null);
                            }

                            @Override
                            public void onPageScrollStateChanged(int i) {

                            }
                        });
                    }
                });
                StaticVariables.readyToSend = false;
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
                        if (editable.length() != 0 || StaticVariables.currentText.length() == 1)
                            StaticVariables.currentText = editable.toString();
                        String len = ((TextView) rootView.findViewById(R.id.file_content_length)).getText().toString();
                        int num = editable.toString().length() + (len.length() > 0 ?
                                Integer.parseInt(len) : 0);
                        TextView tv = (TextView) rootView.findViewById(R.id.text_counter);
                        ImageButton bt = (ImageButton) rootView.findViewById(R.id.send);
                        boolean choosedContact = ((TextView) rootView.findViewById(R.id.contact_id_to_send)).length() > 0;
                        StaticVariables.readyToSend = choosedContact;
                        bt.setImageResource(choosedContact ? R.drawable.ic_send_holo_light : R.drawable.ic_send_disabled_holo_light);
                        if (num == 0) {
                            tv.setVisibility(View.GONE);
                            getActivity().invalidateOptionsMenu();
                            bt.setImageResource(R.drawable.ic_send_disabled_holo_light);
                            StaticVariables.readyToSend = false;
                        } else {
                            tv.setVisibility(View.VISIBLE);
                            getActivity().invalidateOptionsMenu();
                            if (num > 0) {
                                if (choosedContact) {
                                    StaticVariables.readyToSend = true;
                                    bt.setImageResource(R.drawable.ic_send_holo_light);
                                }
                                if (num == 1) {
                                    tv.setVisibility(View.VISIBLE);
                                    getActivity().invalidateOptionsMenu();
                                    tv.setText(StaticVariables.MSG_LIMIT_FOR_QR - num + "");
                                } else if (num <= StaticVariables.MSG_LIMIT_FOR_QR) {
                                    tv.setText(StaticVariables.MSG_LIMIT_FOR_QR - num + "");
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
                        hndl.sendEmptyMessage(0);
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
                        hndl.sendEmptyMessage(0);
                    }
                });
                if (StaticVariables.currentText != null)
                    et.setText(StaticVariables.currentText);
                break;
            case R.layout.decrypt:
                break;
            case wait_nfc_to_write:
                QRCodeEncoder qr = new QRCodeEncoder(CryptMethods.getPublicTmp(), 256);
                try {
                    ((ImageView) rootView.findViewById(R.id.image_public)).setImageBitmap(qr.encodeAsBitmap());
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                break;
            case wait_nfc_decrypt:
                if (NfcStuff.nfcIsntAvailable(getActivity())) {
                    Visual.toast(getActivity(), R.string.cant_connect_nfc_adapter);
                } else if (NfcStuff.nfcIsOff(getActivity()))
                    new TurnNFCOn(getActivity().getFragmentManager());
                if(FilesManagement.id_picture.pictureExist(getActivity()))
                    ((ImageView)rootView.findViewById(R.id.sec_pic)).setImageDrawable(FilesManagement.id_picture.getPicture(getActivity()));
                break;
            case setup:
                break;
            case R.layout.learn:
                try {
                    ((TextView) rootView.findViewById(R.id.version)).setText(
                            getString(R.string.version) + " " + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
                } catch (PackageManager.NameNotFoundException ignored) {
                }
                break;
            case decrypted_msg:
                if (MessageFormat.decryptedMsg != null) {
                    StaticVariables.flag_hash = MessageFormat.decryptedMsg.checkHash();
                    StaticVariables.friendsPublicKey = MessageFormat.decryptedMsg.getPublicKey();
                    StaticVariables.hash = MessageFormat.decryptedMsg.getHash();
                    StaticVariables.timeStamp = MessageFormat.decryptedMsg.getSentTime();
                    StaticVariables.name = MessageFormat.decryptedMsg.getName();
                    StaticVariables.email = MessageFormat.decryptedMsg.getEmail();
                    StaticVariables.flag_msg = true;
                    StaticVariables.msg_content = MessageFormat.decryptedMsg.getMsgContent();
                    StaticVariables.file_name = MessageFormat.decryptedMsg.getFileName();
                    StaticVariables.session = MessageFormat.decryptedMsg.getSession();
                    updateDecryptedScreen();
                } else if (StaticVariables.flag_msg == null || !StaticVariables.flag_msg) {
                    rootView.findViewById(R.id.top_pannel).setVisibility(View.GONE);
                    rootView.findViewById(R.id.open_file_rlt).setVisibility(View.GONE);
                    rootView.findViewById(R.id.save_attachment).setVisibility(View.GONE);
                    rootView.findViewById(R.id.from).setVisibility(View.GONE);
                    ((TextView) rootView.findViewById(R.id.flag_contact_exist)).setText(true + "");
                    ((TextView) rootView.findViewById(R.id.decrypted_msg)).setText(R.string.cant_decrypt);
                } else {
                    updateDecryptedScreen();
                }
                break;
            case R.layout.recreating_keys:
                FrameLayout f = (FrameLayout) rootView.findViewById(R.id.camera);
                f.addView(new CameraPreview(getActivity()));
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                rootView.findViewById(R.id.image_public).setAnimation(anim);
                rootView.findViewById(R.id.image_public).setClickable(false);
                break;
            case R.layout.explorer:
                final ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.pager);
                if(!CryptMethods.privateExist()) {
                    if(FilesManagement.id_picture.pictureExist(getActivity()))
                        rootView.setBackground(FilesManagement.id_picture.getPicture(getActivity()));
                }
                Safe safe = new Safe(Main.main);
                viewPager.setAdapter(safe);
                viewPager.setCurrentItem(Safe.currentPage);
                viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int i, float v, int i2) {

                    }

                    @Override
                    public void onPageSelected(int i) {
                        Safe.currentPage = i;
                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {

                    }
                });
                break;
            case R.layout.profile:
                final ImageButton ibMyName = (ImageButton) rootView.findViewById(R.id.test).findViewById(R.id.image_button);
                final ImageButton ibMyEmail = (ImageButton) rootView.findViewById(R.id.test1).findViewById(R.id.image_button);
                final EditText etMyName = (EditText) rootView.findViewById(R.id.test).findViewById(R.id.edit_text);
                final EditText etMyEmail = (EditText) rootView.findViewById(R.id.test1).findViewById(R.id.edit_text);
                final TextView tvMyName = (TextView) rootView.findViewById(R.id.test).findViewById(R.id.text_view);
                final TextView tvMyEmail = (TextView) rootView.findViewById(R.id.test1).findViewById(R.id.text_view);
                final String[] myDetails = CryptMethods.getMyDetails(getActivity());
                tvMyName.setText(R.string.profile_my_name);
                tvMyEmail.setText(R.string.profile_my_email);
                etMyName.setText(myDetails[0]);
                if (StaticVariables.edit == null)
                    StaticVariables.edit = etMyName.getKeyListener();
                etMyEmail.setKeyListener(null);
                etMyName.setKeyListener(null);
                etMyEmail.setText(myDetails[1]);
                ibMyEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etMyEmail.getKeyListener() != null) {
                            String em = etMyEmail.getText().toString();
                            if (!em.equals(myDetails[1])) {
                                CryptMethods.setDetails(null, em);
                                FilesManagement.edit(getActivity());
                            }
                        }
                        Visual.edit(getActivity(), etMyEmail, ibMyEmail);
                    }
                });
                ibMyName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etMyName.getKeyListener() != null) {
                            String en = etMyName.getText().toString();
                            if (!en.equals(myDetails[0])) {
                                CryptMethods.setDetails(en, null);
                                FilesManagement.edit(getActivity());
                            }
                        }
                        Visual.edit(getActivity(), etMyName, ibMyName);
                    }
                });
                ((ImageView) rootView.findViewById(R.id.my_qr_public_key)).setImageBitmap(FilesManagement.getMyQRPublicKey(getActivity()));
                ((TextView) rootView.findViewById(R.id.my_public_key)).setText(myDetails[2]);
                break;
        }
        Visual.setAllFonts(getActivity(), (ViewGroup) rootView);
        rootView.animate().setDuration(500).alpha(1).start();
        Main.main.invalidateOptionsMenu();
        return rootView;
    }

    private void addSocialLogin() {
        final Account[] list = ((AccountManager) getActivity()
                .getSystemService(Activity.ACCOUNT_SERVICE)).getAccounts();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        final List<ResolveInfo> rs = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        for (final Account acc : list) {
            if (acc.name.contains("@")) {
                if (acc.type.equalsIgnoreCase("com.google")) {
                    ImageButton ib;
                    try {
                        ib = Visual.glow(getActivity()
                                .getPackageManager()
                                .getApplicationInfo("com.google.android.gm", PackageManager.GET_META_DATA)
                                .loadIcon(getActivity().getPackageManager()), getActivity());
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        //todo another google symbol
                        ib = Visual.glow(getResources().getDrawable(R.drawable.unknown2), getActivity());
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
                        ImageButton ib;
                        try {
                            ib = Visual.glow(getActivity()
                                    .getPackageManager()
                                    .getApplicationInfo("com.google.android.email", PackageManager.GET_META_DATA)
                                    .loadIcon(getActivity().getPackageManager()), getActivity());
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            ib = Visual.glow(getResources().getDrawable(R.drawable.unknown2), getActivity());
                        }
                        ib.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((EditText) rootView.findViewById(R.id.email)).setText(acc.name);
                                ((EditText) rootView.findViewById(R.id.name)).setText(acc.name.split("@")[0]);
                            }
                        });
                        ((GridLayout) rootView.findViewById(R.id.grid_login)).addView(ib);
                    } /*else {
                        //todo add exchange app account
                    }*/
                } else {
                    String company = acc.type.split("\\.")[1];
                    for (ResolveInfo pi : rs) {
                        if (pi.activityInfo.packageName.contains(company)) {
                            ImageButton ib = Visual.glow(pi.activityInfo.loadIcon(getActivity().getPackageManager()), getActivity());
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
        ImageButton ib = Visual.glow(getResources().getDrawable(R.drawable.clear), getActivity());
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = (EditText) rootView.findViewById(R.id.email);
                if (et != null && et.length() > 0)
                    et.getText().clear();
                et = (EditText) rootView.findViewById(R.id.name);
                if (et != null && et.length() > 0)
                    et.getText().clear();
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
}