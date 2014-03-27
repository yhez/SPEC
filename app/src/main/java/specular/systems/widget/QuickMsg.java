package specular.systems.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.CryptMethods;
import specular.systems.CustomExceptionHandler;
import specular.systems.Dialogs.ProgressDlg;
import specular.systems.FilesManagement;
import specular.systems.MessageFormat;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.Visual;
import specular.systems.activities.SendMsg;


public class QuickMsg extends Activity {
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(), this));
        }
        String widget = getIntent().getStringExtra("widget");
        if (widget == null) {
            Visual.toast(this,R.string.unexpexted_error);
            finish();
        } else {
            SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(this);
            String widgetDetails = srp.getString(widget, null);
            if (widgetDetails == null) {
                Visual.toast(this,R.string.unexpexted_error);
                finish();
            } else {
                long id = WidgetContact.getId(widgetDetails);
                if (ContactsDataSource.contactsDataSource == null)
                    ContactsDataSource.contactsDataSource = new ContactsDataSource(this);
                final Contact contact = ContactsDataSource.contactsDataSource.findContact(id);
                if (contact == null) {
                    SharedPreferences.Editor edt = srp.edit();
                    edt.remove(widget);
                    edt.commit();
                    new File(getFilesDir() + "/", widget).delete();
                    updateWidget(WidgetContact.getWidgetId(widget));
                    finish();
                } else {
                    if (!contact.getContactName().equals(WidgetContact.getContactName(widgetDetails))) {
                        SharedPreferences.Editor edt = srp.edit();
                        edt.putString(widget, WidgetContact.saveDetails(contact.getContactName(), contact.getId()));
                        edt.commit();
                        updateWidget(WidgetContact.getWidgetId(widget));
                    }
                    setContentView(R.layout.response);
                    final TextView tv = (TextView) findViewById(R.id.text_counter);
                    final ImageButton bt = (ImageButton) findViewById(R.id.send);
                    bt.setEnabled(false);
                    final EditText et = (EditText) findViewById(R.id.message);
                    et.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            if (editable.length() == 0) {
                                bt.setEnabled(false);
                                bt.setImageResource(R.drawable.ic_send_disabled_holo_light);
                                tv.setVisibility(View.GONE);
                            } else {
                                bt.setEnabled(true);
                                bt.setImageResource(R.drawable.ic_send_holo_light);
                                tv.setVisibility(View.VISIBLE);
                                tv.setText(StaticVariables.MSG_LIMIT_FOR_QR - editable.length() > 0 ? StaticVariables.MSG_LIMIT_FOR_QR - editable.length() + "" : getString(R.string.no_qr));
                            }
                        }
                    });
                    bt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final String userInput = et.getText().toString();
                            final MessageFormat msg = new MessageFormat(null, CryptMethods.getMyDetails(QuickMsg.this), "", userInput
                                    , contact.getSession().substring(0,contact.getSession().length()-2));
                            final ProgressDlg prgd = new ProgressDlg(QuickMsg.this, R.string.encrypting);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    byte[] data = Visual.bin2hex(CryptMethods.encrypt(msg.getFormatedMsg(),
                                            contact.getPublicKey())).getBytes();
                                    boolean success = FilesManagement.createFilesToSend(QuickMsg.this, userInput.length() < StaticVariables.MSG_LIMIT_FOR_QR, data);
                                    if (success) {
                                        prgd.cancel();
                                        Intent intent = new Intent(QuickMsg.this, SendMsg.class);
                                        intent.putExtra("type",SendMsg.MESSAGE);
                                        intent.putExtra("contactId", contact.getId());
                                        startActivity(intent);
                                    } else {
                                       Visual.toast(QuickMsg.this, R.string.failed_to_create_files_to_send);
                                    }
                                }
                            }).start();
                        }
                    });
                }
            }
        }
    }

    private void updateWidget(int widgetId) {
        Intent intent = new Intent(this, WidgetContact.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = {widgetId};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}
