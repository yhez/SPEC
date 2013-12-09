package specular.systems.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import specular.systems.R;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetContact extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_contact);
        String widget = "widget-id-"+appWidgetId;
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(context);
        String name = srp.getString(widget, null);
        if(name!=null){
            views.setTextViewText(R.id.text_widget, name.split("-")[0]);
            Intent intent = new Intent(context, QuickMsg.class);
            intent.putExtra("contact_id",Long.parseLong(name.split("-")[1]));
            Log.w("id",Long.parseLong(name.split("-")[1])+"");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_ll,pendingIntent);
        }
        Bitmap b = BitmapFactory.decodeFile(context.getFilesDir() + "/"+widget);
        views.setImageViewBitmap(R.id.image_widget,b);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    @Override
    public void onDeleted(Context context,int[] appWidgetIds){
        super.onDeleted(context,appWidgetIds);
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edt = srp.edit();
        for(int id:appWidgetIds){
            String widget = "widget-id-"+id;
            edt.remove(widget);
            context.deleteFile(widget);
        }
        edt.commit();
    }
}


