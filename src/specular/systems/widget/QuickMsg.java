package specular.systems.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.CryptMethods;
import specular.systems.CustomExceptionHandler;
import specular.systems.Dialogs.ProgressDlg;
import specular.systems.Dialogs.SendMsgDialog;
import specular.systems.FilesManagement;
import specular.systems.MessageFormat;
import specular.systems.PublicStaticVariables;
import specular.systems.R;
import specular.systems.Visual;

/**
 * Created by yehezkelk on 12/9/13.
 */
public class QuickMsg extends Activity {
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(),this));
        }
        String widget = getIntent().getStringExtra("widget");
        if (widget == null) {
            Toast.makeText(this, "something goes wrong code 0", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(this);
            String widgetDetails = srp.getString(widget, null);
            if (widgetDetails == null) {
                Toast.makeText(this, "something goes wrong code 1", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                long id = WidgetContact.getId(widgetDetails);
                final Contact contact = new ContactsDataSource(this).findContact(id);
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
                        edt.putString(widget, WidgetContact.saveDetails(contact.getContactName(),contact.getId()));
                        edt.commit();
                        updateWidget(WidgetContact.getWidgetId(widget));
                    }
                    setContentView(R.layout.response);
                    findViewById(R.id.response_title).setVisibility(View.GONE);
                    findViewById(R.id.divider).setVisibility(View.GONE);
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
                                tv.setText(PublicStaticVariables.MSG_LIMIT_FOR_QR - editable.length() > 0 ? PublicStaticVariables.MSG_LIMIT_FOR_QR - editable.length() + "" : getString(R.string.no_qr));
                            }
                        }
                    });
                    bt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final String userInput = et.getText().toString();
                            final MessageFormat msg = new MessageFormat(null, "", userInput
                                    , contact.getSession());
                            final ProgressDlg prgd = new ProgressDlg(QuickMsg.this, R.string.encrypting);
                            prgd.setCancelable(false);
                            prgd.show();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    CryptMethods.encrypt(msg.getFormatedMsg(),
                                            contact.getPublicKey());
                                    boolean success = FilesManagement.createFilesToSend(QuickMsg.this, userInput.length() < PublicStaticVariables.MSG_LIMIT_FOR_QR);
                                    if (success) {
                                        ArrayList<Uri> files = FilesManagement.getFilesToSend(QuickMsg.this);
                                        if (files == null)
                                            Toast.makeText(QuickMsg.this, R.string.failed_attach_files, Toast.LENGTH_LONG).show();
                                        else {
                                            prgd.cancel();
                                            SendMsgDialog sendMsgDialog = new SendMsgDialog(files, contact);
                                            sendMsgDialog.show(getFragmentManager(), "widget");
                                        }
                                    } else {
                                        Toast.makeText(QuickMsg.this, R.string.failed_to_create_files_to_send, Toast.LENGTH_LONG).show();
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
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int[] ids = {widgetId};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}
