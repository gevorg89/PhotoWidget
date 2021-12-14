package com.gevorg89.photowidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Implementation of App Widget functionality.
 */
class PhotoWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    if (appWidgetId.toString().file(context).exists()) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.photo_widget)
            val widgetBitmap = appWidgetId.toString().file(context).getBitmap()
            views.setImageViewBitmap(R.id.appwidget_img, widgetBitmap)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}