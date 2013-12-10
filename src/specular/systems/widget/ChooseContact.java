package specular.systems.widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.CustomExceptionHandler;
import specular.systems.MySimpleArrayAdapter;
import specular.systems.PublicStaticVariables;
import specular.systems.QRCodeEncoder;
import specular.systems.R;
import specular.systems.Visual;

/**
 * Created by yehezkelk on 12/9/13.
 */
public class ChooseContact extends Activity {
    public void onCreate(Bundle b) {
        super.onCreate(b);
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt()));
        }
        setResult(RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            final int mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            setContentView(R.layout.contacts);
            findViewById(R.id.contacts).setAlpha(1);
            //todo add filter
            //findViewById(R.id.filter_ll).setVisibility(View.VISIBLE);
            ListView lv = (ListView) findViewById(R.id.list);
            MySimpleArrayAdapter adapter;
            if (PublicStaticVariables.adapter == null) {
                ContactsDataSource cdc = new ContactsDataSource(this);
                final List<Contact> cfc = cdc.getAllContacts();
                Collections.sort(cfc, new Comparator<Contact>() {
                    @Override
                    public int compare(Contact contact, Contact contact2) {
                        return contact.getEmail().compareTo(contact2.getEmail());
                    }
                });
                adapter = new MySimpleArrayAdapter(this, cfc);
            } else
                adapter = PublicStaticVariables.adapter;
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ChooseContact.this);
                    RemoteViews views = new RemoteViews(ChooseContact.this.getPackageName(),
                            R.layout.widget_contact);
                    Contact c =PublicStaticVariables.contactsDataSource.findContact(Long.parseLong(((TextView) view.findViewById(R.id.id_contact)).getText().toString()));
                    QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(c.getPublicKey(), BarcodeFormat.QR_CODE.toString(), 200);
                    try {
                        Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                        try {
                            FileOutputStream fos2 = openFileOutput("widget-id-" + mAppWidgetId,
                                    Context.MODE_PRIVATE);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90,
                                    fos2);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        views.setImageViewBitmap(R.id.image_widget, bitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    views.setTextViewText(R.id.text_widget, ((TextView) view.findViewById(R.id.first_line)).getText().toString());
                    Intent intent = new Intent(ChooseContact.this, QuickMsg.class);
                    intent.putExtra("contact_id",c.getId());
                    PendingIntent pendingIntent = PendingIntent.getActivity(ChooseContact.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    views.setOnClickPendingIntent(R.id.widget_ll,pendingIntent);
                    SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(ChooseContact.this);
                    SharedPreferences.Editor edt =srp.edit();
                    edt.putString("widget-id-"+mAppWidgetId,c.getContactName()+"-"+c.getId());
                    edt.commit();
                    appWidgetManager.updateAppWidget(mAppWidgetId, views);
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                }
            });
        }else{
            Toast.makeText(this,"something is wrong",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
