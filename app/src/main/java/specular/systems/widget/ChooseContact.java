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
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import zxing.BarcodeFormat;
import zxing.WriterException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.CustomExceptionHandler;
import specular.systems.QRCodeEncoder;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.Visual;


public class ChooseContact extends Activity {
    public void onCreate(Bundle b) {
        super.onCreate(b);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(), this));
        }
        setResult(RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Toast.makeText(this, "something is wrong", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            final int mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            ListView lv = new ListView(this);
            SimpleList sl = new SimpleList(this);
            //todo add filter
            if (StaticVariables.fullList == null) {
                ContactsDataSource.contactsDataSource = new ContactsDataSource(this);
                StaticVariables.fullList = ContactsDataSource.contactsDataSource.getAllContacts();
            }
            if (StaticVariables.fullList.isEmpty()) {
                Toast t = Toast.makeText(this, R.string.widget_add_contact_list_empty, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                t.show();
                finish();
            } else {
                lv.setAdapter(sl);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ChooseContact.this);
                        RemoteViews views = new RemoteViews(ChooseContact.this.getPackageName(),
                                R.layout.widget_contact);
                        Contact c = ContactsDataSource.contactsDataSource.findContact(Long.parseLong(((TextView) view.findViewById(R.id.id_contact)).getText().toString()));
                        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(c.getPublicKey(), BarcodeFormat.QR_CODE.toString(), 200);
                        try {
                            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                            try {
                                FileOutputStream fos2 = openFileOutput(WidgetContact.getSRPName(mAppWidgetId),
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
                        intent.putExtra("widget", WidgetContact.getSRPName(mAppWidgetId));
                        PendingIntent pendingIntent = PendingIntent.getActivity(ChooseContact.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        views.setOnClickPendingIntent(R.id.widget_ll, pendingIntent);

                        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(ChooseContact.this);
                        SharedPreferences.Editor edt = srp.edit();
                        edt.putString(WidgetContact.getSRPName(mAppWidgetId), WidgetContact.saveDetails(c.getContactName(), c.getId()));
                        edt.commit();
                        appWidgetManager.updateAppWidget(mAppWidgetId, views);
                        Intent resultValue = new Intent();
                        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                        setResult(RESULT_OK, resultValue);
                        //todo if i'm calling update why do i need all the above? seems that the above alone not enough??
                        updateWidget(mAppWidgetId);
                        finish();
                    }
                });
                setContentView(lv);
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
