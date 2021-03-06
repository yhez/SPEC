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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.CustomExceptionHandler;
import specular.systems.MySimpleArrayAdapter;
import specular.systems.R;
import specular.systems.Visual;
import zxing.QRCodeEncoder;
import zxing.WriterException;


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
           Visual.toast(this, R.string.unexpexted_error);
            finish();
        } else {
            final int mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            //todo add filter
            if(ContactsDataSource.fullList==null){
                ContactsDataSource.contactsDataSource = new ContactsDataSource(this);
                ContactsDataSource.fullList = ContactsDataSource.contactsDataSource.getAllContacts();
            }
            if (ContactsDataSource.fullList.isEmpty()) {
                Visual.toast(this,R.string.widget_add_contact_list_empty);
                finish();
            } else {
                MySimpleArrayAdapter my = MySimpleArrayAdapter.getAdapter();
                if(my!=null){
                    my.setFlag(MySimpleArrayAdapter.SIMPLE);
                }else{
                    my = new MySimpleArrayAdapter(this);
                    my.setFlag(MySimpleArrayAdapter.SIMPLE);
                }
                final MySimpleArrayAdapter sl = my;
                ListView lv = new ListView(this);
                lv.setAdapter(sl);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ChooseContact.this);
                        RemoteViews views = new RemoteViews(ChooseContact.this.getPackageName(),
                                R.layout.widget_contact);
                        Contact c = ContactsDataSource.contactsDataSource.findContact(Long.parseLong(((TextView) view.findViewById(R.id.id_contact)).getText().toString()));
                        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(c.getPublicKey(),  200);
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
                        sl.setFlag(MySimpleArrayAdapter.EDIT);
                        finish();
                    }
                });
                setContentView(lv);
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
