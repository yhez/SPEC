package specular.systems;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class FragmentManagement extends Fragment {
    public FragmentManagement() {
    }
    final Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ((TextView)msg.obj).setVisibility(View.VISIBLE);
            ((TextView)msg.obj).setTypeface(FilesManegmant.getOs(getActivity()));
            ((TextView)msg.obj).animate().setDuration(700).alpha(1);
            ((ScrollView)getActivity().findViewById(msg.what)).smoothScrollTo(0, ((View)msg.obj).getTop());
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Wmain.currentLayout = getArguments().getInt("layout");
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
        switch (Wmain.currentLayout) {
            case R.layout.create_new_keys:
                ((TextView) getActivity().findViewById(R.id.welcome)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
                ((EditText) getActivity().findViewById(R.id.name)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
                ((EditText) getActivity().findViewById(R.id.email)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
                ((Button) getActivity().findViewById(R.id.button)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
                break;
            case R.layout.contacts:
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
                        Fragment fragment = new FragmentManagement();
                        Bundle args = new Bundle();
                        args.putInt("layout", R.layout.edit_contact);
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
            case R.layout.edit_contact:
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
                ((TextView) getActivity().findViewById(R.id.contact_pb))
                        .setText(contact.getPublicKey());
                break;
            case R.layout.share:
                ((TextView) getActivity().findViewById(R.id.your_publick_key)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
                ((TextView) getActivity().findViewById(R.id.me_public)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "cour.ttf"));
                ((TextView) getActivity().findViewById(R.id.button_publish)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
                ((TextView) getActivity().findViewById(R.id.button_share)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
                if (FilesManegmant.getMyQRPublicKey(getActivity()) != null)
                    ((ImageView) getActivity().findViewById(R.id.qr_image))
                            .setImageBitmap(FilesManegmant.getMyQRPublicKey(getActivity()));
                ((TextView) getActivity().findViewById(R.id.me_public))
                        .setText(CryptMethods.myPublicKey);
                break;
            case R.layout.encrypt_show:
                String session = getArguments().getString("session");
                String hash = getArguments().getString("hash");
                String userInput = getArguments().getString("userInput");
                String sentTime = getArguments().getString("sentTime");
                ((TextView) getActivity().findViewById(R.id.encryptshow_email))
                        .setText(CryptMethods.myEmail);
                ((TextView) getActivity().findViewById(R.id.encryptshow_mpk))
                        .setText(CryptMethods.myPublicKey);
                ((TextView) getActivity().findViewById(R.id.encryptshow_msg))
                        .setText(userInput);
                ((TextView) getActivity().findViewById(R.id.encryptshow_session))
                        .setText(session);
                ((TextView) getActivity().findViewById(R.id.encryptshow_hash))
                        .setText(hash);
                ((TextView) getActivity().findViewById(R.id.encryptshow_hash))
                        .setText(sentTime);
                final LinearLayout ll = (LinearLayout) getActivity().findViewById(
                        R.id.encrypt_show_ll);
                new Thread(new Runnable() {
                    public void run() {
                        int a = 0;
                        while (a < ll.getChildCount())
                            synchronized (this) {
                                try {
                                    if (ll.getChildAt(a).getVisibility()==View.GONE) {
                                        Message msg = hndl.obtainMessage(R.id.encrypt_show,ll.getChildAt(a));
                                        hndl.sendMessage(msg);
                                        wait(1700);
                                    } else
                                        a++;
                                } catch (Exception ignored) {
                                    ignored.printStackTrace();
                                }
                            }
                    }
                }).start();
                break;
            case R.layout.decrypt_show:
                String data = getArguments().getString("data");
                if (data == null) {
                    ((TextView) getActivity().findViewById(R.id.ds_msg))
                            .setText(R.string.cant_decrypt);
                    getActivity().findViewById(R.id.ds_msg).animate().alpha(1)
                            .start();
                } else {
                    QRMessage msg = new QRMessage(data);
                    if (msg.checkHash())
                        ((TextView) getActivity().findViewById(R.id.ds_hash))
                                .setTextColor(Color.GREEN);
                    else
                        ((TextView) getActivity().findViewById(R.id.ds_hash))
                                .setTextColor(Color.RED);
                    contact = Contact.giveMeContact(getActivity(), msg);
                    int result = Session.check(contact, msg.getSession());
                    if (result == Session.VERIFIED)
                        ((TextView) getActivity().findViewById(R.id.ds_session))
                                .setTextColor(Color.GREEN);
                    else if (result == Session.FAILED)
                        ((TextView) getActivity().findViewById(R.id.ds_session))
                                .setTextColor(Color.RED);
                    ((TextView) getActivity().findViewById(R.id.ds_email))
                            .setText(getResources().getString(R.string.email)
                                    + ":\n" + msg.getEmail());
                    ((TextView) getActivity().findViewById(R.id.ds_msg))
                            .setText(getResources().getString(R.string.message)
                                    + ":\n" + msg.getMsgContent());
                    ((TextView) getActivity().findViewById(R.id.ds_hash))
                            .setText(getResources().getString(R.string.hash)
                                    + ":\n" + msg.getHash());
                    ((TextView) getActivity().findViewById(R.id.ds_pk))
                            .setText(getResources().getString(R.string.public_key)
                                    + ":\n" + msg.getPublicKey());
                    ((TextView) getActivity().findViewById(R.id.ds_session))
                            .setText(getResources().getString(R.string.session)
                                    + ":\n" + msg.getSession());
                    ((TextView) getActivity().findViewById(R.id.ds_sent_time))
                            .setText(getResources().getString(R.string.time_stemp)
                                    + ":\n" + msg.getSentTime());
                    final LinearLayout mm = (LinearLayout) getActivity()
                            .findViewById(R.id.decrypt_show_ll);
                    new Thread(new Runnable() {
                        public void run() {
                            int a = 0;
                            while (a < mm.getChildCount()) {
                                synchronized (this) {
                                    try {
                                        if (mm.getChildAt(a).getVisibility() == View.GONE) {
                                            Message msg = hndl.obtainMessage(R.id.decrypt_show,mm.getChildAt(a));
                                            hndl.sendMessage(msg);
                                            wait(720);
                                        }else
                                            a++;
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }).start();
                }
                break;
        }
    }
}
