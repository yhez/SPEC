package specular.systems;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yehezkelk on 12/9/13.
 */
public class ChooseContact extends Activity {
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setResult(RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        //int mAppWidgetId = -1;
        if (extras != null) {
            int mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            setContentView(R.layout.contacts);
            findViewById(R.id.contacts).setAlpha(1);
            //todo add filter
            //findViewById(R.id.filter_ll).setVisibility(View.VISIBLE);
            ListView lv = (ListView) findViewById(R.id.list);
            final int widgetID = mAppWidgetId;
            MySimpleArrayAdapter adapter;
            if (PublicStaticVariables.adapter == null) {
                ContactsDataSource cdc = new ContactsDataSource(this);
                List<Contact> cfc = cdc.getAllContacts();
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
                    Contact c = PublicStaticVariables.contactsDataSource.findContact(Long.parseLong(((TextView) view.findViewById(R.id.id_contact)).getText().toString()));
                    QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(c.getPublicKey(), BarcodeFormat.QR_CODE.toString(), 100);
                    try {
                        Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                        views.setImageViewBitmap(R.id.image_widget, bitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    views.setTextViewText(R.id.text_widget, ((TextView) view.findViewById(R.id.first_line)).getText().toString());
                    views.setTextViewText(R.id.id, c.getId() + "");
                    appWidgetManager.updateAppWidget(widgetID, views);
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
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
