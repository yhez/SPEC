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
import android.widget.RemoteViews;

import specular.systems.R;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetContact extends AppWidgetProvider {
    public static long getId(String sharedPrfString) {
        return Long.parseLong(sharedPrfString.split("-")[1]);
    }

    public static int getWidgetId(String srp) {
        return Integer.parseInt(srp.split("-")[2]);
    }

    public static String getContactName(String sharedPrfString) {
        return sharedPrfString.split("-")[0];
    }

    public static String getSRPName(int id) {
        return "widget-id-" + id;
    }

    public static String saveDetails(String name, long id) {
        return name + "-" + id;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
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
        String widget = getSRPName(appWidgetId);
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(context);
        String name = srp.getString(widget, null);

        //it all has to be inside the 'if' because it's possible that a widget is on the list,
        //and he's not actually on the screen
        if (name != null) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_contact);
            views.setTextViewText(R.id.text_widget, getContactName(name));
            Intent intent = new Intent(context, QuickMsg.class);
            intent.putExtra("widget", widget);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_ll, pendingIntent);
            Bitmap b = BitmapFactory.decodeFile(context.getFilesDir() + "/" + widget);
            views.setImageViewBitmap(R.id.image_widget, b);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } else {
            appWidgetManager.updateAppWidget(appWidgetId, new RemoteViews(context.getPackageName(), R.layout.widget_empty));
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edt = srp.edit();
        for (int id : appWidgetIds) {
            String widget = getSRPName(id);
            edt.remove(widget);
            context.deleteFile(widget);
        }
        edt.commit();
    }
}


