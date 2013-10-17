package specular.systems;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FragmentManagment extends Fragment {
    public FragmentManagment() {
    }

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
                ((EditText)getActivity().findViewById(R.id.name)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
                ((EditText)getActivity().findViewById(R.id.email)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
                ((Button)getActivity().findViewById(R.id.button)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
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
                        Fragment fragment = new FragmentManagment();
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
                        while (a < ll.getChildCount() - 1)
                            synchronized (this) {
                                try {
                                    if (ll.getChildAt(a).getAlpha() == 0) {
                                        ll.getChildAt(a).animate().setDuration(700)
                                                .alpha(1);
                                        wait(1000);
                                    } else if (ll.getChildAt(a).getAlpha() == 1)
                                        a++;
                                    else
                                        wait(100);
                                } catch (Exception ignored) {
                                }
                            }
                        while(CryptMethods.isAlive())
                            synchronized (this) {
                                try {
                                    wait(300);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }}
                        //TODO debug note
                        Log.e("jj", ""+System.currentTimeMillis());
                        String FILENAME = "message.SPEC";
                        String QR_NAME = "qr_message.png";
                        if (getArguments().getBoolean("qr")) {
                            int qrCodeDimention = 500;
                            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(
                                    CryptMethods.encryptedMsgToSend, null,
                                    Contents.Type.TEXT, BarcodeFormat.QR_CODE
                                    .toString(), qrCodeDimention);
                            try {
                                Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                                FileOutputStream fos2 = null;
                                try {
                                    fos2 = getActivity().openFileOutput(QR_NAME,
                                            Context.MODE_WORLD_READABLE);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                if (fos2 != null) {
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 90,
                                            fos2);
                                }
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            FileOutputStream fos = getActivity().openFileOutput(
                                    FILENAME, Context.MODE_WORLD_READABLE);
                            fos.write(CryptMethods.encryptedMsgToSend.getBytes());
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity().getBaseContext(),
                                    R.string.failed, Toast.LENGTH_LONG).show();
                        }

                        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_SUBJECT,
                                getResources().getString(R.string.subject_encrypt));
                        intent.putExtra(Intent.EXTRA_TEXT,
                                getResources().getString(R.string.content_msg));
                        ArrayList<Uri> files = new ArrayList<Uri>(2);
                        File root = getActivity().getFilesDir();
                        try{
                            files.add(Uri.parse("file://" +new File(root, FILENAME)));
                            if(getArguments().getBoolean("qr"))
                                files.add(Uri.parse("file://" +new File(root, QR_NAME)));
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                            getActivity().startActivity(Intent.createChooser(intent, getResources()
                                    .getString(R.string.send_dialog)));
                        }catch(Exception e){
                            Toast.makeText(getActivity(), R.string.attachment_error,
                                    Toast.LENGTH_SHORT).show();
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
                            .findViewById(R.id.decrypt_show);
                    new Thread(new Runnable() {
                        public void run() {
                            int a = 0;
                            while (a < mm.getChildCount()) {
                                synchronized (this) {
                                    try {
                                        if (mm.getChildAt(a).getAlpha() == 0) {
                                            mm.getChildAt(a).animate()
                                                    .setDuration(700).alpha(1);
                                            wait(720);
                                        } else if (mm.getChildAt(a).getAlpha() >= 1)
                                            a++;
                                        else
                                            wait(100);
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
